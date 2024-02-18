package com.xiaofei.xiaofeimall.cart.service;

import com.xiaofei.xiaofeimall.cart.vo.Cart;
import com.xiaofei.xiaofeimall.cart.vo.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/6 15:16
 */

@Service
public interface CartService {
    void addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getUserCart();

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    void allCheckItem(Boolean allCheckSigh);

    List<CartItem> getCheckedItem();
}
