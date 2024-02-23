package com.xiaofei.xiaofeimall.ware.feign;

import com.xiaofei.xiaofeimall.ware.vo.MemberReceiveAddressEntityVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/18 17:10
 */

@FeignClient("xiaofei-menber")
public interface MemberFeignService {
    /**
     * 根据地址主键查地址实体类;
     * @param id
     * @return
     */
    @GetMapping("/member/memberreceiveaddress/{id}/address")
    MemberReceiveAddressEntityVo getAddressesById(@PathVariable("id") Long id);

}
