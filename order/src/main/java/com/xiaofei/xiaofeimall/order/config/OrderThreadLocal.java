package com.xiaofei.xiaofeimall.order.config;

import com.xiaofei.common.vo.MemberEntityVo;



/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/17 20:08
 */


public class OrderThreadLocal {
    /**
     * 购物车共享的线程存储
     */
    public static final ThreadLocal<MemberEntityVo> orderUserInfoThreadLocal = new ThreadLocal<>();


}
