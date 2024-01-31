package com.xiaofei.xiaofeimall.auth.vo;

import lombok.Data;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/31 15:28
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
