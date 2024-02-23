package com.xiaofei.common.constant;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/19 19:38
 */


public class OrderConstant {
    //Redis存防重令牌的key前缀
    public static final String USER_ORDER_TOKEN_PREFIX = "order:token:";
    //下单成功状态码
    public static final int ORDER_STATUS_SUCCESS = 0;


    public enum OrderStatusEnum {


        CREATE_NEW(0, "待付款"),
        PAYED(1, "已付款"),
        SENDED(2, "已发货"),
        RECIEVED(3, "已完成"),
        CANCLED(4, "已取消"),
        SERVICING(5, "售后中"),
        SERVICED(6, "售后完成");
        private Integer code;
        private String msg;

        OrderStatusEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
