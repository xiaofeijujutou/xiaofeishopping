package com.xiaofei.common.constant;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/15 21:45
 */


public enum AppConstant {


    ;


    private int code;
    private String msg;
    public static final String FEIGN_DATA_PREFIX = "data";
    public static final String FEIGN_PAGE_PREFIX = "page";
    AppConstant(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
}
