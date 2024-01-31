package com.xiaofei.common.constant;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/31 16:35
 */


public enum MemberConstant {


    USER_NORMAL_STATUS(1,"启用状态"),
    USER_ABNORMAL_STATUS(0,"禁用状态"),;





    private int code;
    private String msg;

    MemberConstant(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
