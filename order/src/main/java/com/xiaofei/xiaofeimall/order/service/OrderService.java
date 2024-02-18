package com.xiaofei.xiaofeimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import com.xiaofei.xiaofeimall.order.vo.OrderConfirmVo;

import java.util.Map;

/**
 * 订单
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:46:25
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo toTrade();

}

