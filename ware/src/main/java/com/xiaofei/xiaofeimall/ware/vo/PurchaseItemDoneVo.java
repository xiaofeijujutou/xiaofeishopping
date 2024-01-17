package com.xiaofei.xiaofeimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVo {
    //{itemId:1,status:4,reason:""}
    //采购单的id
    private Long itemId;
    //采购状态修改成什么样
//    CREATED(0, "新建"),
//    ASSIGNED(1, "已分配"),
//    BUYING(2, "正在采购"),
//    FINISH(3, "已完成采购"),
//    HASERROR(4, "采购失败");
    private Integer status;
    //成功或者失败的原因
    private String reason;
}
