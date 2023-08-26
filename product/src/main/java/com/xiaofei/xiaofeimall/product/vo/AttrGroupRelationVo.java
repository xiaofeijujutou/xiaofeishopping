package com.xiaofei.xiaofeimall.product.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttrGroupRelationVo implements Serializable {
    private static final long serialVersionUID = 1L;
    //attrId: 4, attrGroupId: 5
    private Long attrId;
    private Long attrGroupId;
}
