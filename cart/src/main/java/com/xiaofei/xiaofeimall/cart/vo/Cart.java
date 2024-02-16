package com.xiaofei.xiaofeimall.cart.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * 一个用户的购物车页面的所有数据
 * @Author : 小肥居居头
 * @create 2024/2/6 14:36
 */


public class Cart {

    /**
     * 购物车子项信息
     */
    @Getter @Setter
    private List<CartItem> items;

    /**
     * 结算的商品总数量
     */
    @Setter
    private Integer countNum;

    /**
     * 结算的商品类型数量
     */
    @Setter
    private Integer countType;

    /**
     * 商品总价
     */
    @Setter
    private BigDecimal totalAmount;

    /**
     * 优惠多少价格
     */
    @Getter @Setter
    private BigDecimal reduce = new BigDecimal("0.00");

    /**
     * 根据用户购物车有多少个item算出总数量
     * @return
     */
    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 根据用户购物车有多少个item算出类型总数量
     * @return
     */
    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }


    /**
     * 计算购物车总价
     * @return
     */
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 计算购物项总价
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItem cartItem : items) {
                if (cartItem.getCheck()) {
                    amount = amount.add(cartItem.getTotalPrice());
                }
            }
        }
        // 计算优惠后的价格
        return amount.subtract(getReduce());
    }
}
