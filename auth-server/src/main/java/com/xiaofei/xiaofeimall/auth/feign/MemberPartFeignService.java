package com.xiaofei.xiaofeimall.auth.feign;

import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/31 15:29
 */

@FeignClient("xiaofei-menber")
public interface MemberPartFeignService {
    @PostMapping("member/member/checkUniqueness")
    R checkUniqueness(@RequestBody CheckUniquenessVo vo);

    @PostMapping("member/member/register")
    R register(@RequestBody UserRegisterVo vo);
}
