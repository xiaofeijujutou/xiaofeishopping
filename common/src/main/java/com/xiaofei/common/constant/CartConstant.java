package com.xiaofei.common.constant;

/**
 * @Description: Created by IntelliJ IDEA.
 * 购物车常量
 * @Author : 小肥居居头
 * @create 2024/2/7 13:16
 */


public class CartConstant {
    public static final String TEMP_USER_COOKIE_KEY = "user-key";
    public static final int TEMP_USER_COOKIE_TTL = 30 * 24 * 60 * 60;
    /**
     * 操作RedisHash数据的key;
     */
    public static final String CART_REDIS_HASH_PREFIX = "xiaofeimall:cart:";

    /**
     * 表名用户临时购物车已经合并,需要删除对应cookie
     */
    public static final String COOKIE_DELETE_SIGH = "cookieDeleteSigh";

    public static final int CART_ITEM_CHECKED = 1;
    public static final int CART_ITEM_UNCHECKED = 0;

}
