package com.xiaofei.xiaofeimall.cart.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * 购物车里的一条一条购物项
 * @Author : 小肥居居头
 * @create 2024/2/6 14:36
 */


public class CartItem {
    /**
     * 商品的可以售卖的id
     */
    @Getter @Setter
    private Long skuId;

    /**
     * 商品是否被选中结算
     */
    @Getter @Setter
    private Boolean check = true;

    /**
     * 商品标题
     */
    @Getter @Setter
    private String title;

    /**
     * 商品图片
     */
    @Getter @Setter
    private String image;

    /**
     * 商品套餐属性
     */
    @Getter @Setter
    private List<String> skuAttrValues;

    /**
     * 商品价格
     */
    @Getter @Setter
    private BigDecimal price;

    /**
     * 商品商品数量
     */
    @Getter @Setter
    private Integer count;

    /**
     * 数量x价格;
     */
    @Setter
    private BigDecimal totalPrice;

    /**
     * 计算当前购物项总价,需要动态获取,值为数量x价格
     * @return
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + this.count));
    }
}
