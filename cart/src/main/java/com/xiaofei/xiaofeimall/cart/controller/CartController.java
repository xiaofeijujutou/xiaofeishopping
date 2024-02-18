package com.xiaofei.xiaofeimall.cart.controller;


import com.xiaofei.xiaofeimall.cart.constants.ThreadLocalConstant;
import com.xiaofei.xiaofeimall.cart.service.CartService;
import com.xiaofei.xiaofeimall.cart.vo.Cart;
import com.xiaofei.xiaofeimall.cart.vo.CartItem;
import com.xiaofei.xiaofeimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/5 20:51
 */

@Slf4j
@Controller
public class CartController {
    @Autowired
    CartService cartService;

    /**
     * 返回用户购物车的所有被选中商品的数据
     * @return
     */
    @ResponseBody
    @GetMapping("/getCheckedItem")
    public List<CartItem> getCheckedItem(){
        return cartService.getCheckedItem();
    }



    /**
     * 展示用户购物车的所有数据
     * @return
     */
    @GetMapping("/cartList.html")
    public String cartListPage(Model model){
        Cart cart = cartService.getUserCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 用户添加商品到购物车;
     * @param skuId 购买单元的id
     * @param num 加入购物车数量
     * @param model 购物车展示数据
     * @return 成功页面
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes model){
        cartService.addToCart(skuId, num);
        model.addAttribute("skuId", skuId);
        return "redirect:http://cart.xiaofeimall.com/addToCartSuccess";
    }

    /**
     * 通过skuId来获取购物车详情数据,然后存放到model中
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }

    /**
     * 用户修改商品是否被选中的状态;
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check){
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.xiaofeimall.com/cartList.html";
    }

    /**
     * 修改购物车商品数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        cartService.countItem(skuId, num);
        return "redirect:http://cart.xiaofeimall.com/cartList.html";
    }

    /**
     * 删除购物车商品
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.xiaofeimall.com/cartList.html";
    }

    /**
     * 全选或者全不选;
     * @param allCheckSigh
     * @return
     */
    @GetMapping("/allCheckItem")
    public String allCheckItem(@RequestParam("allCheckSigh") Boolean allCheckSigh, RedirectAttributes ra){
        cartService.allCheckItem(allCheckSigh);
        ra.addAttribute("allCheck", allCheckSigh);
        return "redirect:http://cart.xiaofeimall.com/cartList.html";
    }
}
