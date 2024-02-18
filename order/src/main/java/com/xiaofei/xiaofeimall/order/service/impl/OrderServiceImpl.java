package com.xiaofei.xiaofeimall.order.service.impl;

import com.xiaofei.common.vo.MemberEntityVo;
import com.xiaofei.xiaofeimall.order.config.OrderThreadLocal;
import com.xiaofei.xiaofeimall.order.feign.CartFeignService;
import com.xiaofei.xiaofeimall.order.feign.MemberFeignService;
import com.xiaofei.xiaofeimall.order.vo.MemberAddressVo;
import com.xiaofei.xiaofeimall.order.vo.OrderConfirmVo;
import com.xiaofei.xiaofeimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.order.dao.OrderDao;
import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import com.xiaofei.xiaofeimall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;

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

        //防重令牌
        return orderConfirmVo;
    }

}