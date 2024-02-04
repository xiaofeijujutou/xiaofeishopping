package com.xiaofei.xiaofeimall.auth.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/2 22:38
 */

@Service
public interface UserLoginService {

    String weiboLogin(String code, HttpSession session);
}
