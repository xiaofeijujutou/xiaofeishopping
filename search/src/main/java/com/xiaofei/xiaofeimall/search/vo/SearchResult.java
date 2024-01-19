package com.xiaofei.xiaofeimall.search.vo;


import com.xiaofei.common.vo.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {

    private List<SkuEsModel> products;

    /**
     * 分页信息
     */
    private Integer pageNum;//当前页
    private Long total;//总记录数
    private Long totalPage;//总页码
    private List<Integer> pageNavs;

    private List<BrandVo> brands;//品牌信息
    private List<AttrVo> attrs;//属性信息
    private List<CatalogVo> catalogs;//分类信息
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();
    /**
     *面包屑导航静态内部类
     */
    @Data
    public static class NavVo{
        //请求参数的key
        private String navName;
        //请求参数的value
        private String navValue;
        //跳转连接
        private String link;
    }
    /**
     * 品牌内部类
     */
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    /**
     * 属性内部类
     */
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
    /**
     * 分类内部类
     */
    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}

