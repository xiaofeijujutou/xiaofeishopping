package com.xiaofei.xiaofeimall.member.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * @Description: Created by IntelliJ IDEA.
 * 检验用户名或手机号唯一性;
 * @Author : 小肥居居头
 * @create 2024/1/31 13:55
 */

@Data
public class CheckUniquenessVo {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号
     */
    private String phone;

}
