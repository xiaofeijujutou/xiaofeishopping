package com.xiaofei.common.exception;

public enum BizCodeEnume {
    SUCCESS_CODE(0,"响应正常"),
    USER_PHONE_EXIST_EXCEPTION(8000, "手机已经注册"),
    USER_NAME_EXIST_EXCEPTION(8001, "用户名已经注册"),
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"参数格式校验失败");





    private int code;
    private String msg;

    BizCodeEnume(int code, String msg) {
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