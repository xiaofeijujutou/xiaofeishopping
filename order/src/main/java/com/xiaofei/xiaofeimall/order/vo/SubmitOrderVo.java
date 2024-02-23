package com.xiaofei.xiaofeimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: Created by IntelliJ IDEA.
 * 提交订单vo
 * @Author : 小肥居居头
 * @create 2024/2/19 19:47
 */

@Data
public class SubmitOrderVo {
    /** 收获地址的id **/
    private Long addrId;

    /** 支付方式 **/
    private Integer payType;
    //无需提交要购买的商品，去购物车再获取一遍
    //优惠、发票

    /** 防重令牌 **/
    private String orderToken;

    /** 应付价格 **/
    private BigDecimal payPrice;

    /** 订单备注 **/
    private String remarks;

    //用户相关的信息，直接去session中取出即可

}
