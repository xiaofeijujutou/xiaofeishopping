package com.xiaofei.xiaofeimall.order.dao;

import com.xiaofei.xiaofeimall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:46:25
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
