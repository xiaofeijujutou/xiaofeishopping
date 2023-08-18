package com.xiaofei.xiaofeimall.member.feign;

import com.xiaofei.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("xiaofei-coupon")
public interface CouponFeignService {
    /**
     * 远程调用coupen
     * @return
     */
    @RequestMapping("/coupon/coupon/menber/list")
    public R menberCoupens();
}
