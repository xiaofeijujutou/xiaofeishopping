package com.xiaofei.xiaofeimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/19 22:49
 */


@Data
public class WareSkuLockVo {

    /**
     * 用户下单产生的订单id,也就是订单号
     */
    private String orderSn;

    /** 需要锁住的所有库存信息 **/
    private List<OrderItemVo> locks;



}
