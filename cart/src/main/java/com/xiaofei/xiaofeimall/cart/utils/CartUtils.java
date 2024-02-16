package com.xiaofei.xiaofeimall.cart.utils;

import com.xiaofei.xiaofeimall.cart.constants.ThreadLocalConstant;
import com.xiaofei.xiaofeimall.cart.vo.UserInfoTo;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/16 19:44
 */


public class CartUtils {
    public static UserInfoTo getUserLoginInfo(){
        return (UserInfoTo) ThreadLocalConstant.cartInterceptThreadLocal.get()
                .get(ThreadLocalConstant.KEY_OF_USER_INFO_TO);
    }
}
