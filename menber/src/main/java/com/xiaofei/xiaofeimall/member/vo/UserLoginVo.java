package com.xiaofei.xiaofeimall.member.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Description: Created by IntelliJ IDEA.
 * 用户登录VO
 * @Author : 小肥居居头
 * @create 2024/1/31 20:49
 */

@Data
public class UserLoginVo {
    /**
     * 账号
     */
    @NotEmpty(message = "账号不能为空")
    private String loginAccount;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    private String loginPassword;
}
