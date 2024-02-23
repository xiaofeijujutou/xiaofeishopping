package com.xiaofei.xiaofeimall.ware.vo;

import lombok.Data;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/20 19:14
 */

@Data
public class LockStockResultVo {
    //id
    private Long skuId;
    //数量
    private Integer num;
    //是否锁定成功
    private Boolean locked;
}
