package com.xiaofei.xiaofeimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/18 19:49
 */

@Data
public class FareVo {
    private MemberReceiveAddressEntityVo address;
    private BigDecimal fare;
}
