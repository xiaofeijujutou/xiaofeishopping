package com.xiaofei.xiaofeimall.thirdpart.controller;

import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.thirdpart.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: Created by IntelliJ IDEA.
 * 短信验证码功能,用来发送短信验证码;
 * @Author : 小肥居居头
 * @create 2024/1/30 13:36
 */


@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;

    /**
     * 供其他服务调用,其他服务来指定手机号和发送的验证码,这里只管发送
     * @param phone 手机号
     * @param code 验证码4-6位
     * @return 状态码
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        String massage = null;
        try {
            massage = smsComponent.sendMassage(phone, code);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(massage);
        }
        return R.ok().put("massage", massage);
    }
}
