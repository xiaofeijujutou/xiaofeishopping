package com.xiaofei.xiaofeimall.cart.feign;

import com.xiaofei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/7 17:28
 */

@FeignClient("xiaofei-product")
public interface ProductFeignService {

    /**
     * 接收skuId,返回对应的一条实体类
     * 获取数据的key是skuInfo
     * @param skuId id
     * @return 实体类;
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuEntityInfoById(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/attrList/{skuId}")
    R getSkuSaleAttrValuesById(@PathVariable("skuId") Long skuId);
}
