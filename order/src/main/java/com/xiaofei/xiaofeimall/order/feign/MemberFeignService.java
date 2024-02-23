package com.xiaofei.xiaofeimall.order.feign;

import com.xiaofei.xiaofeimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/17 20:38
 */


@FeignClient("xiaofei-menber")
public interface MemberFeignService {

    /**
     * 根据用户id来查他所有的收货地址;
     * @param memberId
     * @return
     */
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddressesByMemberId(@PathVariable("memberId") Long memberId);
}
