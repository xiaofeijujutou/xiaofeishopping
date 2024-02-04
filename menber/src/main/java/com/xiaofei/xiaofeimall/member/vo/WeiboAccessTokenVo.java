package com.xiaofei.xiaofeimall.member.vo;

import lombok.Data;

/**
 * @Description: Created by IntelliJ IDEA.
 * 用户登录成功之后,拿到令牌去找微博换取token
 * @Author : 小肥居居头
 * @create 2024/2/2 23:18
 */

@Data
public class WeiboAccessTokenVo {
    private String access_token;
    private String remind_in;
    private String expires_in;
    private String uid;
    private Boolean isRealName;
    /**
     * 用户微博名字
     */
    private String name;
    /**
     * 用户微博性别
     */
    private Integer gender;
    /**
     * 用户微博地区
     */
    private String location;
}
