package com.xiaofei.xiaofeimall.auth.feign;

import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.vo.CheckUniquenessVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 14:19
 */

@FeignClient("xiaofei-thirdpart")
public interface ThirdPartFeignService {
    /**
     * 调用第三工具,发送手机验证码请求
     * @param phone 手机号
     * @param code 参数
     * @return 不需要
     */
    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
