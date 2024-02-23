package com.xiaofei.xiaofeimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xiaofei.common.constant.AppConstant;
import com.xiaofei.common.constant.CartConstant;
import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.cart.feign.ProductFeignService;
import com.xiaofei.xiaofeimall.cart.service.CartService;
import com.xiaofei.xiaofeimall.cart.to.SkuInfoTo;
import com.xiaofei.xiaofeimall.cart.utils.CartUtils;
import com.xiaofei.xiaofeimall.cart.vo.Cart;
import com.xiaofei.xiaofeimall.cart.vo.CartItem;
import com.xiaofei.xiaofeimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/6 15:17
 */

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor cartGlobalThreadPool;

    /**
     * 用户添加商品到购物车;
     * @param skuId 购买单元的id
     * @param num 加入购物车数量
     */
    @Override
    public void addToCart(Long skuId, Integer num) {
        //根据用户是否登录来创建操作Redis的客户端
        BoundHashOperations<String, Object, Object> redisOperation = getHashOperations();
        String item = (String) redisOperation.get(skuId.toString());
        if (!StringUtils.isEmpty(item)){
            //购物车已经有数据了
            changeCartItemNum(redisOperation, num, item);
        }
        else {
            //购物车还没有数据
            addNewCartItem(redisOperation, skuId, num);
        }
    }

    /**
     * 添加购物车,但是购物车已经有这个商品了,就只需要修改里面的数据
     * @param num
     * @param item
     */
    private void changeCartItemNum(BoundHashOperations<String, Object, Object> redisOperation,
                                   Integer num, String item) {
        CartItem cartItem = JSON.parseObject(item, CartItem.class);
        cartItem.setCount(cartItem.getCount() + num);
        putItem(cartItem, redisOperation);
    }

    /**
     * 添加新商品到购物车
     * @param redisOperation 购物车
     * @param skuId id
     * @param num 数量
     */
    private void addNewCartItem(BoundHashOperations<String, Object, Object> redisOperation,
                                Long skuId, Integer num) {
        //购物车实体类总数据;
        CartItem cartItem = new CartItem();
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            //根据id远程查询出详情Entity,因为前段直接传的话可能是缓存;
            R skuInfoResult = productFeignService.getSkuEntityInfoById(skuId);
            SkuInfoTo skuInfo = skuInfoResult.getDataByKey("skuInfo", SkuInfoTo.class);
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(skuInfo.getSkuDefaultImg());
            cartItem.setPrice(skuInfo.getPrice());
            cartItem.setTitle(skuInfo.getSkuTitle());
            cartItem.setSkuId(skuId);
        }, cartGlobalThreadPool);

        //再次远程调用获取attr属性;
        CompletableFuture<Void> attrsFuture = CompletableFuture.runAsync(() -> {
            R attrResult = productFeignService.getSkuSaleAttrValuesById(skuId);
            List<String> attrValue = attrResult.getDataByKey(AppConstant.FEIGN_DATA_PREFIX, new TypeReference<List<String>>() {});
            cartItem.setSkuAttrValues(attrValue);
        },cartGlobalThreadPool);
        try {
            CompletableFuture.allOf(skuInfoFuture, attrsFuture).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //往购物车Redis里面存实体类的信息,直接放是jdk的序列化,最好是用json序列化;
        putItem(cartItem, redisOperation);
    }

    /**
     * 根据ThreadLocal来获取哪个用户的购物车;
     * 可以是临时用户也可以是登录用户;
     * 如果用户登录了就返回登录用户的购物车,用户之前没登录,后面登录了还是登录购物车;
     *
     * @return 用户购物车的Redis操作
     */
    private BoundHashOperations<String, Object, Object> getHashOperations() {
        UserInfoTo userInfoTo = CartUtils.getUserLoginInfo();
        String redisKey;
        if (!ObjectUtils.isEmpty(userInfoTo.getUserId())) {
            redisKey = CartConstant.CART_REDIS_HASH_PREFIX + userInfoTo.getUserId();
        } else {
            redisKey = CartConstant.CART_REDIS_HASH_PREFIX + userInfoTo.getUserKey();
        }
        return stringRedisTemplate.boundHashOps(redisKey);
    }

    /**
     * 通过skuId来获取购物车详情数据,然后存放到model中
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        return getCartItem(skuId, getHashOperations());
    }


    /**
     * 展示用户购物车的所有数据
     * @return
     */
    @Override
    public Cart getUserCart() {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartUtils.getUserLoginInfo();
        //优先登录,其次临时,如果用户从来没加过购物车,那么就要判空
        BoundHashOperations<String, Object, Object> userOrTempBound = getHashOperations();
        List<CartItem> cartItems = getAllCartItem(userOrTempBound);
        //如果临时购物车里面还有数据就合并过来,只有用户id和临时id都存在才合并;
        if (userInfoTo.getUserId() != null && userInfoTo.getUserKey() != null){
            mergeTempCartToUserCart(userInfoTo, userOrTempBound, cartItems);
        }
        cart.setItems(cartItems);
        return cart;
    }


    /**
     * 执行合并购物车操作
     * @param userInfoTo
     * @param userOrTempBound 已经登录的用户购物车的Redis操作实体类
     * @param cartItems 用户端购物车实体类
     */
    private void mergeTempCartToUserCart(UserInfoTo userInfoTo,
                                         BoundHashOperations<String, Object, Object> userOrTempBound,
                                         List<CartItem> cartItems) {
        BoundHashOperations<String, Object, Object> tempUserHashOperations = getTempUserHashOperations();
        //要检查临时购物车是不是空的,以及有没有临时key;
        List<CartItem> tempUserCartItems = null;
        if (null != tempUserHashOperations) {
            tempUserCartItems = getAllCartItem(tempUserHashOperations);
            if (!CollectionUtils.isEmpty(tempUserCartItems) && tempUserCartItems.size() > 0) {
                //合并方法
                doMergeTempCartToUserCart(tempUserCartItems, cartItems);
                //合并完了之后还要put到redis里面去;
                putAllCartItem(cartItems, userOrTempBound);
                stringRedisTemplate.delete(tempUserHashOperations.getKey());
            }
        }
        //合并临时购物车数据和临时用户凭证,无论购物车有没有数据都要删除cookie;
        userInfoTo.setUserKey(CartConstant.COOKIE_DELETE_SIGH);
    }

    /**
     * 合并购物车
     * @param source 临时购物车
     * @param target 登录购物车
     */
    private void doMergeTempCartToUserCart(List<CartItem> source, List<CartItem> target) {
        for (CartItem sourceItem : source) {
            boolean merged = false;
            for (CartItem targetItem : target) {
                if (sourceItem.getSkuId().equals(targetItem.getSkuId())) {
                    // 商品在用户购物车中已存在，更新数量
                    targetItem.setCount(targetItem.getCount() + sourceItem.getCount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                // 商品在用户购物车中不存在，添加到用户购物车
                target.add(sourceItem);
            }
        }
    }

    /**
     * 获取临时用户的购物车所有信息
     * @return
     */
    private BoundHashOperations<String, Object, Object> getTempUserHashOperations() {
        //线程存储中获取用户详情;
        UserInfoTo userInfoTo = CartUtils.getUserLoginInfo();
        //只返回临时的,如果是空的,可能已经合并过了,直接return null;
        if (StringUtils.isEmpty(userInfoTo.getUserKey())){
            return null;
        }
        //否则返回绑定Redis;
        String redisKey = CartConstant.CART_REDIS_HASH_PREFIX + userInfoTo.getUserKey();
        return stringRedisTemplate.boundHashOps(redisKey);
    }

    /**
     * 用户修改商品是否被选中的状态;
     * @param skuId
     * @param check
     * @return
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        CartItem cartItem = getCartItem(skuId, hashOperations);
        cartItem.setCheck(check == CartConstant.CART_ITEM_CHECKED);
        putItem(cartItem, hashOperations);
    }



    /**
     * 更新购物车,减少IO版
     * @param cartItem
     * @param hashOperations
     */
    private void putItem(CartItem cartItem, BoundHashOperations<String, Object, Object> hashOperations) {
        hashOperations.put(cartItem.getSkuId().toString(), JSON.toJSONString(cartItem));
    }

    /**
     * 更新购物车
     * @param cartItem
     */
    private void putItem(CartItem cartItem) {
        putItem(cartItem, getHashOperations());
    }
    /**
     * 给出Redis对象,减少网络IO
     * @param skuId
     * @param hashOperations
     * @return
     */
    private CartItem getCartItem(Long skuId, BoundHashOperations<String, Object, Object> hashOperations) {
        String itemString = (String)hashOperations.get(skuId.toString());
        return JSON.parseObject(itemString, CartItem.class);
    }

    /**
     * 修改购物车商品数量
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public void countItem(Long skuId, Integer num) throws NullPointerException{
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        CartItem cartItem = getCartItem(skuId, hashOperations);
        cartItem.setCount(num);
        putItem(cartItem, hashOperations);
    }
    /**
     * 删除购物车商品
     * @param skuId
     * @return
     */
    @Override
    public void deleteItem(Long skuId) {
        deleteItem(skuId, getHashOperations());
    }



    /**
     * 删除购物车商品,节省IO版;
     * @param skuId
     * @param hashOperations
     */
    public void deleteItem(Long skuId, BoundHashOperations<String, Object, Object> hashOperations) {
        hashOperations.delete(skuId.toString());
    }

    /**
     * 全选或者全不选;
     * @param allCheckSigh true为全选,false为全不选
     * @return
     */
    @Override
    public void allCheckItem(Boolean allCheckSigh) {
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        List<CartItem> cartItems = getAllCartItem(hashOperations);
        if (null != cartItems){
            cartItems.forEach(item -> item.setCheck(allCheckSigh));
            putAllCartItem(cartItems, hashOperations);
        }

    }



    /**
     * 更新用户购物车所有信息;减少IO版;
     * @param cartItems
     * @param hashOperations
     */
    private void putAllCartItem(List<CartItem> cartItems, BoundHashOperations<String, Object, Object> hashOperations) {
        hashOperations.putAll(cartItems.stream()
                .collect(Collectors.toMap(cartItem -> String.valueOf(cartItem.getSkuId()), JSON::toJSONString)));
    }
    /**
     * 更新用户购物车所有信息;
     * @param cartItems
     * @return
     */
    private void putAllCartItem(List<CartItem> cartItems) {
        putAllCartItem(cartItems, getHashOperations());
    }

    /**
     * 获取用户购物车所有信息,节省IO版;
     * @return
     */
    private List<CartItem> getAllCartItem(){
        return getAllCartItem(getHashOperations());
    }

    /**
     * 获取用户购物车所有信息;
     * @param hashOperations
     * @return
     */
    private List<CartItem> getAllCartItem(BoundHashOperations<String, Object, Object> hashOperations){
        List<Object> values = hashOperations.values();
        if (CollectionUtils.isEmpty(values)) {
            return new ArrayList<>();
        }
        //合并购物车,如果已经有相同商品就只能修改数量
        return values.stream()
                .map((cartItemObj) -> JSON.parseObject((String) cartItemObj, CartItem.class))
                .collect(Collectors.toList());
    }

    /**
     * 返回用户购物车的所有被选中商品的数据
     * @return
     */
    @Override
    public List<CartItem> getCheckedItem() {
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        List<CartItem> allCartItem = getAllCartItem(hashOperations);
        List<CartItem> checkedCartItem = null;
        if (!CollectionUtils.isEmpty(allCartItem)){
            //TODO 结算的时候要调用商品服务查实际价格
            checkedCartItem = allCartItem.stream().filter(CartItem::getCheck).collect(Collectors.toList());
        }
        return checkedCartItem;
    }
}
