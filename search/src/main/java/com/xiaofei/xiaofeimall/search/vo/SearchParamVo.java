package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递的页面
 */
@Data
public class SearchParamVo {
    //全文检索
    private String keyword;

    private Long catalog3Id;

    /**
     *排序条件:
     *销量：saleCount_asc/desc
     *价格: skuPrice_asc/desc
     *热度评分: hotScore_asc/desc
     */
    private String sort;

    /**
     * 过滤:
     *仅显示有货:hasStock=0/1
     *价格区间:skuPrice=1_500/_500/500_
     * 品牌(多选)
     * 商品属性(多选)
     */
    private Integer hasStock = 1;
    private String skuPrice;
    private Integer skuPrice1;
    private Integer skuPrice2;
    private List<Long> brandId;
    private List<String> attrs;
    //分页
    private Integer pageNum = 1;

    private String _queryString;//原生查询条件
}
