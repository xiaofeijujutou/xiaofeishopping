package com.xiaofei.xiaofeimall.auth.controller;

import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.service.SmsSendCodeService;
import com.xiaofei.xiaofeimall.auth.service.UserRegisterService;
import com.xiaofei.xiaofeimall.auth.vo.UserRegisterVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Description: Created by IntelliJ IDEA.
 * 注册页面Controller
 * @Author : 小肥居居头
 * @create 2024/1/29 18:25
 */
@Log4j2
@Controller
public class LoginController {
    @Autowired
    SmsSendCodeService smsSendCodeService;
    @Autowired
    UserRegisterService userRegisterService;
    /**
     * 验证码防刷,防止别人把验证码刷没钱;
     * 注册接口的获取验证码,调远程服务的验证码;
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        return smsSendCodeService.sendCode(phone);
    }


    /**
     * 用户注册请求,发送表单,然后存入数据库;
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        return userRegisterService.register(vo, result, redirectAttributes);
    }
}
