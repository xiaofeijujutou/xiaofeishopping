package com.xiaofei.xiaofeimall.order.feign;

import com.xiaofei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/19 22:13
 */

@FeignClient("xiaofei-product")
public interface ProductFeignService {
    /**
     * 根据id获取sku详情
     */
    @GetMapping("product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
