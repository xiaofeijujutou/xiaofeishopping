package com.xiaofei.xiaofeimall.auth.controller;

import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.xiaofeimall.auth.service.SmsSendCodeService;
import com.xiaofei.xiaofeimall.auth.service.UserLoginService;
import com.xiaofei.xiaofeimall.auth.service.UserRegisterService;
import com.xiaofei.xiaofeimall.auth.vo.UserLoginVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.websocket.server.PathParam;


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
    @Autowired
    UserLoginService userLoginService;

    @GetMapping("/logout")
    public String redirectLogout(HttpSession session){
        if (session.getAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX) == null){
            return "login";
        }
        session.removeAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX);
        return "login";
    }
    /**
     * 用户访问登录页面,如果已经有浏览器和服务器的对话session了,就不用再跑去登录页面
     * 直接访问首页;cookie就是身份信息
     * @param session session
     * @return 重定向
     */
    @GetMapping("/login.html")
    public String redirectLogin(HttpSession session){
        if (session.getAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX) == null){
            return "login";
        }
        return "redirect:http://xiaofeimall.com";
    }
    /**
     * 用户登录接口,成功之后转发到首页,然后携带上参数;
     * @param vo 账号密码
     * @param redirectAttributes 重定向
     * @return 主页
     */
    @PostMapping("/login")
    public String login(@Valid UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        return userRegisterService.login(vo, redirectAttributes, session);
    }


    /**
     * 用户点击微博登录,输入账号密码成功之后,微博就会朝我们的接口发验证成功的code,
     * 我们要根据code再向微博发送请求,拿到用户的详细信息;
     * @param code 用户验证令牌
     * @return 我们项目自己的重定向地址
     */
    @GetMapping("/oauth2/weibo/success")
    public String weiboLogin(@PathParam("code") String code, HttpSession session){
        return userLoginService.weiboLogin(code, session);
    }
}
