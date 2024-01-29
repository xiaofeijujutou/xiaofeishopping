package com.xiaofei.xiaofeimall.product.vo;

import lombok.Data;

/**
 * @Description: Created by IntelliJ IDEA.
 * 作用:用来保存商品销售属性所对应的spuId,返回给前段求的交集来查询商品
 * @Author : 小肥居居头
 * @create 2024/1/29 12:57
 */

@Data
public class AttrValueWithSkuIdVo {
    /**
     *销售属性的值;
     */
    private String attrValue;
    /**
     * 销售属性的值所对应的skuId集合;
     */
    private String skuIds;

}
