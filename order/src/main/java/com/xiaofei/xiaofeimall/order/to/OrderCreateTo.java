package com.xiaofei.xiaofeimall.order.to;

import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import com.xiaofei.xiaofeimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/19 20:46
 */

@Data
public class OrderCreateTo {


    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     **/
    private BigDecimal payPrice;

    /**
     * 运费
     **/
    private BigDecimal fare;
}
