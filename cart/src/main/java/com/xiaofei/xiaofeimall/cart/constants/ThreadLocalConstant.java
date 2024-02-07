package com.xiaofei.xiaofeimall.cart.constants;

import java.util.Map;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/7 12:43
 */


public class ThreadLocalConstant {
    /**
     * 购物车共享的线程存储
     */
    public static final ThreadLocal<Map<String, Object>> cartInterceptThreadLocal = new ThreadLocal<>();

    /**
     * ThreadLocal的key,专门用来获取userInfoTo
     */
    public static final String KEY_OF_USERINFOTO = "userInfo";
}
