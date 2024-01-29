package com.xiaofei.xiaofeimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * 封装一个商品有多少种销售属性,商城管理员设置的属性
 * @Author : 小肥居居头
 * @create 2024/1/20 15:56
 */

@Data
public class SkuItemSaleAttrVo {
    /**
     * 属性id
     */
    private Long attrId;
    /**
     * 属性名字
     */
    private String attrName;
    /**
     * 属性值,以及值所对应的skuIds
     */
    private List<AttrValueWithSkuIdVo> attrValues;

}
