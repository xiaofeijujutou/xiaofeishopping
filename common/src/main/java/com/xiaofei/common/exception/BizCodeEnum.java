package com.xiaofei.common.exception;

public enum BizCodeEnum {
    SUCCESS_CODE(0,"响应正常"),
    USER_PHONE_EXIST_EXCEPTION(8000, "手机已经注册"),
    USER_NAME_EXIST_EXCEPTION(8001, "用户名已经注册"),
    USER_LOGIN_ACCOUNT_NOT_EXIST_EXCEPTION(8002, "账号不存在"),
    USER_LOGIN_PASSWORD_EXCEPTION(8003, "密码错误"),
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"参数格式校验失败"),
    NO_STOCK_EXCEPTION(10003, "锁库存失败");






    private int code;
    private String msg;

    BizCodeEnum(int code, String msg) {
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