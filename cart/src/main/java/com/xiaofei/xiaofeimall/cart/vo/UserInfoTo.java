package com.xiaofei.xiaofeimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/7 13:10
 */
@ToString
@Data
public class UserInfoTo {
    /**
     * 用户已经登录,就有id
     */
    private Long userId;
    /**
     * 用户没登录,给他加个userKey
     */
    private String userKey;
}
