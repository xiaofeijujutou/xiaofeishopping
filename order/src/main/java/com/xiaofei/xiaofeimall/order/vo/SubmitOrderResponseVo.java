package com.xiaofei.xiaofeimall.order.vo;


import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/19 20:22
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /** 错误状态码 0是成功, 其他是失败**/
    private Integer code;


}
