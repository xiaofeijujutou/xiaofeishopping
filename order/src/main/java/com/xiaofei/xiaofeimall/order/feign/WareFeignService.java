package com.xiaofei.xiaofeimall.order.feign;


import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("xiaofei-ware")
public interface WareFeignService {
    /**
     * 查询是否有货
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    /**
     * 查询运费
     * @param addrId
     * @return FareVo data
     */
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);


    /**
     * 锁定指定id的库存
     * @param vo
     * @return
     */
    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
