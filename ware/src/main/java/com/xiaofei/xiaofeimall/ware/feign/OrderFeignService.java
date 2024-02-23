package com.xiaofei.xiaofeimall.ware.feign;

import com.xiaofei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/22 21:22
 */

@FeignClient("xiaofei-order")
public interface OrderFeignService {
    /**
     * 根据订单号orderSn查询实体类
     * @param orderSn
     * @return
     */
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
