package com.xiaofei.xiaofeimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 17:28
 */


@Data
public class UserRegisterVo {
    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6, max = 18, message = "用户名长度必须是6-18位")
    private String userName;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码长度必须是6-18位")
    private String password;
    /**
     * 手机号
     */
    @NotEmpty(message = "手机号不能为空")
    //@Pattern(regexp = "/^1[3-9]\\d{9}$/", message = "手机号格式不正确")暂时改成11位长度
    @Length(min = 11, max = 11, message = "手机号格式不正确")
    private String phone;
    /**
     * 短信验证码
     */
    @NotEmpty(message = "验证码不能为空")
    private String code;
}
