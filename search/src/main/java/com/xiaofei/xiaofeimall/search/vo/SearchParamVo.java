package com.xiaofei.xiaofeimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递的页面
 */
@Data
public class SearchParamVo {
    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     *排序条件: (下面三个是可能出现的情况)
     *销量：saleCount_asc/desc
     *价格: skuPrice_asc/desc
     *热度评分: hotScore_asc/desc
     */
    private String sort;

    /**
     *仅显示有货:hasStock=0/1
     */
    private Integer hasStock = 1;
    /**
     *价格区间:skuPrice=1_500/_500/500_
     */
    private String skuPrice;
    private Integer skuPrice1;
    private Integer skuPrice2;
    /**
     * 品牌id(多选)
     * 商品属性id(多选)
     */
    private List<Long> brandId;
    /**
     * attr扁平化处理了,用_来拼接
     */
    private List<String> attrs;
    //分页
    private Integer pageNum = 1;

    private String _queryString;//原生查询条件
}
