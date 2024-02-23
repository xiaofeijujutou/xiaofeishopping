package com.xiaofei.xiaofeimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaofei.common.constant.OrderConstant;
import com.xiaofei.common.utils.R;
import com.xiaofei.common.vo.MemberEntityVo;
import com.xiaofei.xiaofeimall.order.config.OrderThreadLocal;
import com.xiaofei.xiaofeimall.order.entity.OrderItemEntity;
import com.xiaofei.xiaofeimall.order.feign.CartFeignService;
import com.xiaofei.xiaofeimall.order.feign.MemberFeignService;
import com.xiaofei.xiaofeimall.order.feign.ProductFeignService;
import com.xiaofei.xiaofeimall.order.feign.WareFeignService;
import com.xiaofei.xiaofeimall.order.service.OrderItemService;
import com.xiaofei.xiaofeimall.order.to.OrderCreateTo;
import com.xiaofei.xiaofeimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.order.dao.OrderDao;
import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import com.xiaofei.xiaofeimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 返回页面数据
     * @return
     */
    @Override
    public OrderConfirmVo toTrade() {
        MemberEntityVo member = OrderThreadLocal.orderUserInfoThreadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        //远程查询用户购物车地址
        List<MemberAddressVo> addresses = memberFeignService.getAddressesByMemberId(member.getId());
        orderConfirmVo.setAddress(addresses);
        //远程查询用户已经选中的购物车
        List<OrderItemVo> cartItemVos = cartFeignService.getCheckedItem();
        orderConfirmVo.setItems(cartItemVos);
        //自动计算总价

        //防重令牌,后端生成,返回给前段(通过Vo),发送给Redis,下次再来提交的时候比对就行了
        //令牌相同说明是重复提交,不同说明是不同订单,(只能防止用户在同一个页面多次点击);
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId(),
                token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        return orderConfirmVo;
    }

    /**
     * 用户下单操作,创建订单,验证令牌,验证价格,锁库存;
     *
     * 使用MQ完成事务:
     *  1=>下订单过期没支付要解锁库存;
     *  2=>用户手动取消订单要解锁库存;
     *  3=>事务失败抛异常要解锁库存
     *
     *
     * @param submitOrderVo
     * @return
     */
    //@GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(SubmitOrderVo submitOrderVo) {
        MemberEntityVo member = OrderThreadLocal.orderUserInfoThreadLocal.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        //1===>验证用户是否重复下单
        if (isDuplicateSubmit(submitOrderVo, member, responseVo)) {
            return responseVo;
        }
        //重新查询商品订单
        OrderCreateTo orderCreateTo = createOrder(submitOrderVo);
        //2===>验证价格
        BigDecimal payAmount = orderCreateTo.getOrder().getPayAmount();
        BigDecimal payPrice = submitOrderVo.getPayPrice();
        //金额对比,验证价格失败
        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) > 60) {
            responseVo.setCode(2);
            return responseVo;
        }

        //3===>保存订单
        saveOrder(orderCreateTo);

        //4===>库存锁定,只要有异常，回滚订单数据
        //组装参数:订单号、所有订单项信息(skuId,skuNum,skuName)
        WareSkuLockVo lockVo = new WareSkuLockVo();
        lockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
        //获取出要锁定的商品数据信息
        List<OrderItemVo> orderLockItemsVo = orderCreateTo.getOrderItems().stream().map((item) -> {
            //设置锁库存远程调用的数据;
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(item.getSkuId());
            orderItemVo.setCount(item.getSkuQuantity());
            orderItemVo.setTitle(item.getSkuName());
            return orderItemVo;
        }).collect(Collectors.toList());
        lockVo.setLocks(orderLockItemsVo);
        //TODO 调用远程锁定库存的方法
        //出现的问题：扣减库存成功了，但是由于网络原因超时，出现异常，导致订单事务回滚，库存事务不回滚(解决方案：seata)
        //为了保证高并发，不推荐使用seata，因为是加锁，并行化，提升不了效率,可以发消息给库存服务
        R r = wareFeignService.orderLockStock(lockVo);
        if (r.getCode() == 0) {
            //锁定成功
            responseVo.setOrder(orderCreateTo.getOrder());
             int i = 10/0;

//            //TODO 订单创建成功，发送消息给MQ
//            rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderCreateTo.getOrder());
//            //删除购物车里的数据
//            stringRedisTemplate.delete(CART_PREFIX + memberResponseVo.getId());
            return responseVo;
        } else {
            //锁定失败
            String msg = (String) r.get("msg");
            responseVo.setCode(3);
            return responseVo;
            // responseVo.setCode(3);
            // return responseVo;
        }

    }

    /**
     * 根据订单号orderSn查询实体类
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        return this.baseMapper.selectOne(Wrappers.<OrderEntity>lambdaQuery().eq(OrderEntity::getOrderSn, orderSn));
    }

    /**
     * 验证唯一性,防止重复下单,没有重复下单返回false
     * @param submitOrderVo
     * @param member
     * @param responseVo
     * @return
     */
    private boolean isDuplicateSubmit(SubmitOrderVo submitOrderVo, MemberEntityVo member, SubmitOrderResponseVo responseVo) {
        responseVo.setCode(OrderConstant.ORDER_STATUS_SUCCESS);
        //Redis执行脚本,如果有就返回值然后删除,没有就返回空;返回0说明没有这个key,1说明有这个key且删除成功;
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //方法参数:默认脚本类型,keys数组,argv数组, 然后脚本就根据keys和args来执行脚本;
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()),
                submitOrderVo.getOrderToken());
        //1===>验证唯一性
        if (0L == result){
            //验证失败
            responseVo.setCode(1);
            return true;
        }
        //验证成功
        return false;
    }

    /**
     * 生成概要订单
     * @return
     */
    private OrderCreateTo createOrder(SubmitOrderVo submitOrderVo){
        MemberEntityVo member = OrderThreadLocal.orderUserInfoThreadLocal.get();
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(member.getId());
        orderEntity.setMemberUsername(member.getUsername());
        R fare = wareFeignService.getFare(submitOrderVo.getAddrId());
        FareVo fareVo = fare.getData(FareVo.class);
        //设置实体类数据
        orderEntity.setFreightAmount(fareVo.getFare());
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        //购物车数据要存到这里来;
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        //查购物车获取订单项
        List<OrderItemVo> checkedItem = cartFeignService.getCheckedItem();
        if (checkedItem != null && checkedItem.size() > 0) {
            orderItemEntities = checkedItem.stream().map((items) -> {
                //TODO 优化循环远程调用构建订单项数据
                OrderItemEntity orderItemEntity = builderOrderItem(items);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        computePrice(orderEntity, orderItemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);
        return orderCreateTo;

    }


    /**
     * 构建某一个订单项的数据
     * @param items
     * @return
     */
    private OrderItemEntity builderOrderItem(OrderItemVo items) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1、商品的spu信息
        Long skuId = items.getSkuId();
        //获取spu的信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoData = spuInfo.getData(SpuInfoVo.class);
        orderItemEntity.setSpuId(spuInfoData.getId());
        orderItemEntity.setSpuName(spuInfoData.getSpuName());
        orderItemEntity.setCategoryId(spuInfoData.getCatalogId());
        //2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(items.getTitle());
        orderItemEntity.setSkuPic(items.getImage());
        orderItemEntity.setSkuPrice(items.getPrice());
        orderItemEntity.setSkuQuantity(items.getCount());
        //使用StringUtils.collectionToDelimitedString将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(items.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);
        //3、商品的优惠信息

        //4、商品的积分信息
        orderItemEntity.setGiftGrowth(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());
        orderItemEntity.setGiftIntegration(items.getPrice().multiply(new BigDecimal(items.getCount())).intValue());

        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //6当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }


    /**
     * 计算价格的方法
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 保存订单所有数据
     *
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {

        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        //保存订单
        this.baseMapper.insert(order);

        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
//        //批量保存订单项数据,TODO 低版本是Seata不能批量保存
//        orderItems.stream().peek(item ->{
//            orderItemService.save(item);
//        });
        orderItemService.saveBatch(orderItems);
    }


}