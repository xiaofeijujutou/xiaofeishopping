package com.xiaofei.xiaofeimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseDoneVo implements Serializable {

    @NotNull
    private Long id;//采购单id

    private List<PurchaseItemDoneVo> items;
}
