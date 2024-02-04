package com.xiaofei.common.constant;

/**
 * @Description: Created by IntelliJ IDEA.
 * 注册服务的常量;
 * @Author : 小肥居居头
 * @create 2024/1/30 16:54
 */


public class AuthServerConstant {
    /**
     * 注册服务的Redis前缀
     */
    public static final String SMS_CODE_CACHE_PREFIX = "sms:code";
    /**
     * 用户登录成功之后存放的全局session的前缀;
     */
    public static final String OAUTH_SESSION_PREFIX = "member";
}
