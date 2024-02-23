package com.xiaofei.xiaofeimall.order.feign;

import com.xiaofei.xiaofeimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/17 21:20
 */

@FeignClient("xiaofei-cart")
public interface CartFeignService {
    //获取用户选中的购物车物品;
    @GetMapping("/getCheckedItem")
    List<OrderItemVo> getCheckedItem();
}
