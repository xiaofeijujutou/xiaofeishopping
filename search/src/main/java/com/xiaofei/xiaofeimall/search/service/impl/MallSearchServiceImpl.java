

package com.xiaofei.xiaofeimall.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.xiaofei.common.vo.es.SkuEsModel;
import com.xiaofei.xiaofeimall.search.config.ElasticsearchConfig;
import com.xiaofei.xiaofeimall.search.constant.EsConstant;
import com.xiaofei.xiaofeimall.search.service.MallSearchService;
import com.xiaofei.xiaofeimall.search.vo.SearchParamVo;
import com.xiaofei.xiaofeimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MallSearchServiceImpl class
 *
 * @author xiaofeijujutou
 * @date 2024/1/17
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    /**
     * 用户搜索商品的入口,负责调es接口,整理数据,返回给用户
     *
     * @param paramVo 参数
     * @return 商品
     */
    @Override
    public SearchResult search(SearchParamVo paramVo) {
        //临时变量 请求参数
        SearchRequest searchRequest;
        //返回结果
        SearchResult searchResult = null;
        try {
            //构建请求数据
            searchRequest = buildProductEsSearch(paramVo);
            //这个ES的SDK和MP的类似,可以动态拼接sql,但是执行查询的时候不仅要传入sql,还要传入ES的配置;
            //MP的请求叫query,ES的请求叫search
            SearchResponse result = restHighLevelClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
            searchResult = buildEsResult(result, paramVo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 构建结果数据,数据都是从json的hits和aggregation里面获取
     *
     * @param result ES查出来的数据
     * @return 返回给前端的数据
     */
    private SearchResult buildEsResult(SearchResponse result, SearchParamVo paramVo) {
        //发给前端的数据
        SearchResult resultVo = new SearchResult();
        //命中记录
        SearchHits hits = result.getHits();
        //1,设置产品详细信息
        List<SkuEsModel> products = null;
        if (hits != null && hits.getHits().length > 0) {
            //数组对象可以使用Arrays.stream来获取流对象;
            Stream<SearchHit> stream = Arrays.stream(hits.getHits());
            products = stream.map((hit) -> {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                return skuEsModel;
            }).collect(Collectors.toList());
        }
        resultVo.setProducts(products);

        //2,设置三级分类catalog_agg聚合信息
        List<SearchResult.CatalogVo> catalogVos = null;
        //获取聚合信息
        ParsedLongTerms catalogAgg = result.getAggregations().get("catalog_agg");
        if (!ObjectUtils.isEmpty(catalogAgg)) {
            List<? extends Terms.Bucket> catalogIdBuckets = catalogAgg.getBuckets();
            catalogVos = catalogIdBuckets.stream().map((bucket) -> {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
                //子聚合可以直接从桶里获得,这里的桶可以有很多个,但是这里只有一个,有多个再遍历就行了;
                ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
                List<? extends Terms.Bucket> catalogNameBuckets = catalogNameAgg.getBuckets();
                catalogVo.setCatalogName(catalogNameBuckets.get(0).getKeyAsString());
                return catalogVo;
            }).collect(Collectors.toList());
        }
        resultVo.setCatalogs(catalogVos);

        //3,设置attr嵌入聚合信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        //嵌入式的要用ParsedNested聚合,多获取一次聚合数据
        ParsedNested attrAgg = result.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        if (!ObjectUtils.isEmpty(attrIdAgg)) {
            List<? extends Terms.Bucket> attrIdAggsBuckets = attrIdAgg.getBuckets();
            attrVos = attrIdAggsBuckets.stream().map((bucket) -> {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                //设置id
                attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //设置attrName
                ParsedStringTerms attrNameAggr = bucket.getAggregations().get("attr_name_agg");
                List<? extends Terms.Bucket> attrNameAggrBuckets = attrNameAggr.getBuckets();
                attrVo.setAttrName(attrNameAggrBuckets.get(0).getKeyAsString());
                //设置attrValue
                ParsedStringTerms attrValueAggr = bucket.getAggregations().get("attr_value_agg");
                List<? extends Terms.Bucket> attrValueAggrBuckets = attrValueAggr.getBuckets();
                List<String> valueStringList = attrValueAggrBuckets.stream().map((s) -> s.getKeyAsString()).collect(Collectors.toList());
                attrVo.setAttrValue(valueStringList);
                return attrVo;
            }).collect(Collectors.toList());
        }
        resultVo.setAttrs(attrVos);
        //4,设置品牌图片对应信息
        List<SearchResult.BrandVo> brandVos = null;
        ParsedLongTerms brandAggs = result.getAggregations().get("brand_agg");
        if (!ObjectUtils.isEmpty(brandAggs)) {
            List<? extends Terms.Bucket> brandBuckets = brandAggs.getBuckets();
            brandVos = brandBuckets.stream().map((bucket) -> {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                //设置id
                brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
                //设置brandName
                ParsedStringTerms brandNameAggr = bucket.getAggregations().get("brand_name_agg");
                List<? extends Terms.Bucket> brandNameAggrBuckets = brandNameAggr.getBuckets();
                brandVo.setBrandName(brandNameAggrBuckets.get(0).getKeyAsString());
                //设置品牌图片
                ParsedStringTerms brandImgAggr = bucket.getAggregations().get("brand_img_agg");
                List<? extends Terms.Bucket> brandImgAggrBuckets = brandImgAggr.getBuckets();
                brandVo.setBrandName(brandImgAggrBuckets.get(0).getKeyAsString());
                return brandVo;
            }).collect(Collectors.toList());
        }
        resultVo.setBrands(brandVos);


        //,设置总记录数
        resultVo.setTotal(hits.getTotalHits().value);
        //设置分页信息(总记录数除以每一页大小)
        resultVo.setTotalPage((hits.getTotalHits().value + EsConstant.PRODUCT_PAGESIZE - 1) / EsConstant.PRODUCT_PAGESIZE);
        resultVo.setPageNum(paramVo.getPageNum());


        return resultVo;
    }

    /**
     * 构建商品查询的ES请求
     * 准备检索请求:
     * 模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存),排序，分类，高亮，聚合分析
     *
     * @param paramVo 参数
     * @return sql
     */
    private SearchRequest buildProductEsSearch(SearchParamVo paramVo) {
        //searchSourceBuilder这个是构建sql语句的,这个是最大的一个大括号,里面有query,sort,aggs
        //类写的是Builder,但是实习生只是一个json,变量名有点混乱;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建query模块
        searchSourceBuilder = componentQuery(searchSourceBuilder, paramVo);
        //构建排序模块
        searchSourceBuilder = componentSort(searchSourceBuilder, paramVo);
        //构建分页模块
        searchSourceBuilder = componentPage(searchSourceBuilder, paramVo);
        //构建高亮模块
        searchSourceBuilder = componentHighLight(searchSourceBuilder, paramVo);
        //聚合分析
        searchSourceBuilder = componentAgg(searchSourceBuilder, paramVo);
        //json+索引就是最终的请求数据;
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }

    /**
     * es商品搜索聚合
     *
     * @param searchSourceBuilder
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder componentAgg(SearchSourceBuilder searchSourceBuilder, SearchParamVo paramVo) {
        //品牌聚合,添加商品名字聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(10);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(10));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(10));
        searchSourceBuilder.aggregation(brandAgg);
        //添加三级分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(10);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(10));
        searchSourceBuilder.aggregation(catalogAgg);
        //添加attr的嵌入聚合
        NestedAggregationBuilder attrNested = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attrAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrNested.subAggregation(attrAgg);
        searchSourceBuilder.aggregation(attrNested);
        return searchSourceBuilder;
    }

    /**
     * ES构建上架商品的高亮json模块
     *
     * @param searchSourceBuilder 最终的查询json
     * @param paramVo             参数
     */
    private SearchSourceBuilder componentHighLight(SearchSourceBuilder searchSourceBuilder, SearchParamVo paramVo) {
        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
            HighlightBuilder highlight = new HighlightBuilder();
            highlight.field("skuTitle");
            highlight.preTags("<b style='color:red'>");
            highlight.postTags("<\b>");
            searchSourceBuilder.highlighter(highlight);
        }
        return searchSourceBuilder;
    }


    /**
     * ES构建上架商品的查询json模块
     *
     * @param searchSourceBuilder 最终的查询json
     * @param paramVo             参数
     */
    private SearchSourceBuilder componentQuery(SearchSourceBuilder searchSourceBuilder, SearchParamVo paramVo) {
        //BoolQueryBuilder是构建bool查询,也就是query
        BoolQueryBuilder finalQueryBuilder = QueryBuilders.boolQuery();
        //查询是否有库存,1有库存,0无库存;
        finalQueryBuilder.filter(QueryBuilders.termQuery("hasStock",
                paramVo.getHasStock() == 1 ? "true" : "false"));
        //构建must模糊匹配
        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
            finalQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", paramVo.getKeyword()));
        }
        //构建filter精确匹配catalogId
        if (!ObjectUtils.isEmpty(paramVo.getCatalog3Id())) {
            finalQueryBuilder.filter(QueryBuilders.termQuery("catalogId", paramVo.getCatalog3Id()));
        }
        //构建多个brandId查询
        if (!CollectionUtils.isEmpty(paramVo.getBrandId())) {
            finalQueryBuilder.filter(QueryBuilders.termsQuery("brandId", paramVo.getBrandId()));
        }
        //价格区间查询
        if (!StringUtils.isEmpty(paramVo.getSkuPrice())) {
            //*价格区间:skuPrice=1_500/_500/500_
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String skuPriceSplit = "_";
            String[] split = paramVo.getSkuPrice().split(skuPriceSplit);
            if (paramVo.getSkuPrice().startsWith(skuPriceSplit)) {
                rangeQueryBuilder.lte(split[split.length - 1]);
            }
            if (paramVo.getSkuPrice().endsWith(skuPriceSplit)) {
                rangeQueryBuilder.gte(split[0]);
            }
            if (!paramVo.getSkuPrice().startsWith(skuPriceSplit) && !paramVo.getSkuPrice().endsWith(skuPriceSplit)) {
                rangeQueryBuilder.lte(split[split.length - 1]);
                rangeQueryBuilder.gte(split[0]);
            }
            finalQueryBuilder.filter(rangeQueryBuilder);
        }
        //嵌入查询,里面的query和外面的query是一样的,只是加了个path;
        if (!CollectionUtils.isEmpty(paramVo.getAttrs())) {
            for (String attrStr : paramVo.getAttrs()) {
                //和最上面的大BoolQuery                                                                                                                                                          是一样的,加一个path就是嵌入式查询
                BoolQueryBuilder nestedQuery = QueryBuilders.boolQuery();
                String[] attrproperties = attrStr.split("_");
                //一个attrStr分解出的有用的属性就是attrId,attrValue
                String attrId = attrproperties[0];
                String[] attrValues = attrproperties[1].split(":");
                nestedQuery.filter(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedQuery.filter(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nested = QueryBuilders.nestedQuery("attrs", nestedQuery, ScoreMode.None);
                //添加到条件里面来;
                finalQueryBuilder.filter(nested);
            }
        }
        searchSourceBuilder.query(finalQueryBuilder);
        return searchSourceBuilder;
    }

    /**
     * ES构建排序模块
     *
     * @param searchSourceBuilder
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder componentSort(SearchSourceBuilder searchSourceBuilder, SearchParamVo paramVo) {
        //格式为skuPrice_asc/desc
        if (!StringUtils.isEmpty(paramVo.getSort())) {
            String[] sort = paramVo.getSort().split("_");
            searchSourceBuilder.sort(sort[0], SortOrder.fromString(sort[1]));
        }
        return searchSourceBuilder;
    }

    /**
     * ES构建分页模块
     *
     * @param searchSourceBuilder
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder componentPage(SearchSourceBuilder searchSourceBuilder, SearchParamVo paramVo) {
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.from((paramVo.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        return searchSourceBuilder;
    }
}


//package com.atguigu.gulimall.search.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//import com.atguigu.common.to.es.SkuEsModel;
//import com.atguigu.common.utils.R;
//import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
//import com.atguigu.gulimall.search.constant.EsConstant;
//import com.atguigu.gulimall.search.feign.ProductFeignService;
//import com.atguigu.gulimall.search.service.MallSearchService;
//import com.atguigu.gulimall.search.vo.AttrResponseVo;
//import com.atguigu.gulimall.search.vo.BrandVo;
//import com.atguigu.gulimall.search.vo.SearchParamVo;
//import com.atguigu.gulimall.search.vo.SearchResult;
//import org.apache.lucene.search.join.ScoreMode;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.NestedQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.RangeQueryBuilder;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
//import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
//import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
//import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
//import org.elasticsearch.search.sort.SortOrder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class MallSearchServiceImpl implements MallSearchService {
//    @Autowired
//    private RestHighLevelClient client;
//    @Autowired
//    ProductFeignService productFeignService;
//
//    @Override
//    public SearchResult search(SearchParamVo paramVo) {
//        //1.动态构建出查询需要的DSL语句
//        SearchResult result = null;
//        //准备检索请求
//        SearchRequest searchRequest = buildSearchRequest(paramVo);
//
//        try {
//            //执行检索请求
//            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
//
//            //分析封装响应数据
//            result = buildSearchResult(response, paramVo);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    /**
//     * 准备检索请求:
//     * 模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存),排序，分类，高亮，聚合分析
//     *
//     * @return
//     */
//    private SearchRequest buildSearchRequest(SearchParamVo paramVo) {
//        //构建DSL语句
//        SearchSourceBuilder builder = new SearchSourceBuilder();
//        //查询:模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存)
//        //1.构建bool - query
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        //1.1 must - 模糊匹配
//        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
//            boolQuery.must(QueryBuilders.matchQuery("skuTitle", paramVo.getKeyword()));
//        }
//        //1.2 bool - fitler 按照三级分类id查询
//        if (paramVo.getCatalog3Id() != null) {
//            boolQuery.filter(QueryBuilders.termQuery("catalogId", paramVo.getCatalog3Id()));
//        }
//        //1.2 bool - filter 按照品牌id查询
//        if (paramVo.getBrandId() != null && paramVo.getBrandId().size() > 0) {
//            boolQuery.filter(QueryBuilders.termsQuery("brandId", paramVo.getBrandId()));
//        }
//        //1.2 bool - filter 按照指定属性进行查询，嵌入式查询,ScoreMode相关性得分
//        if (paramVo.getAttrs() != null && paramVo.getAttrs().size() > 0) {
//            for (String attrStr : paramVo.getAttrs()) {
//                //attrs=1_5寸:8寸&attrs=2_16G:8G
//                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
//                String[] s = attrStr.split("_");
//                String attrId = s[0];//属性id
//                String[] attrValues = s[1].split(":");//检索的属性值
//                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
//                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
//                //每一个必须得生成一个nested查询
//                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
//                boolQuery.filter(nestedQuery);
//            }
//        }
//        //1.2 bool - filter 按照库存进行查询 todo
//        builder.query(QueryBuilders.termsQuery("hasStock", paramVo.getHasStock() == 1));
//        //1.2 bool - filter 按照价格区间
//        if (!StringUtils.isEmpty(paramVo.getSkuPrice())) {
//            //1_500/_500/500_
//            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
//            String[] s = paramVo.getSkuPrice().split("_");
//            BigDecimal bigDecimal1 = new BigDecimal(s[0]);
//            if (s.length == 2) {
//                //gte大于等于，lte小于等于，gt大于，lt小于
//                //区间
//                BigDecimal bigDecimal2 = new BigDecimal(s[1]);
//                rangeQuery.gte(bigDecimal1).lte(bigDecimal2);
//            } else if (s.length == 1) {
//                //大于
//                if (paramVo.getSkuPrice().startsWith("_")) {
//                    rangeQuery.lte(bigDecimal1);
//                }
//                //小于
//                if (paramVo.getSkuPrice().endsWith("_")) {
//                    rangeQuery.gte(bigDecimal1);
//                }
//            }
//            boolQuery.filter(rangeQuery);
//        }
//        builder.query(boolQuery);
//        //排序，分类，高亮
//        //2.1 排序
//        if (!StringUtils.isEmpty(paramVo.getSort())) {
//            //sort = skuPrice_asc/desc
//            String sort = paramVo.getSort();
//            String[] s = sort.split("_");
//            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
//            builder.sort(s[0], order);
//        }
//        //2.2 分页
//        //from = (pageNum-1)*pageSize
//        builder.from((paramVo.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
//        builder.size(EsConstant.PRODUCT_PAGESIZE);
//        //2.3 高亮
//        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
//            HighlightBuilder highlightBuilder = new HighlightBuilder();
//            highlightBuilder.field("skuTitle");
//            highlightBuilder.preTags("<b style='color:red'>");
//            highlightBuilder.postTags("</b>");
//            builder.highlighter(highlightBuilder);
//        }
//
//        //聚合分析
//        //1.品牌聚合
//        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
//        brand_agg.field("brandId").size(50);
//        //1.1品牌聚合的子聚合
//        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
//        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
//        builder.aggregation(brand_agg);
//        //2.分类聚合
//        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
//        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
//        builder.aggregation(catalog_agg);
//        //3.属性聚合(嵌入式聚合)
//        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
//        //聚合出当前所有的attr_id
//        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
//        //聚合分析当前attr_id对应的名字
//        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
//        //聚合分析当前attr_id对应的所有可能的属性值attrValue
//        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
//        attr_agg.subAggregation(attr_id_agg);
//        builder.aggregation(attr_agg);
//        String s = builder.toString();
//        System.out.println("DSL:" + s);
//        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
//        return searchRequest;
//    }
//
//    /**
//     * 分析封装检索结果
//     *
//     * @return
//     */
//    private SearchResult buildSearchResult(SearchResponse response, SearchParamVo paramVo) {
//        SearchResult result = new SearchResult();
//        //1.返回所有查询到的商品
//        SearchHits hits = response.getHits();
//        List<SkuEsModel> esModelList = new ArrayList<>();
//        if (hits.getHits() != null && hits.getHits().length > 0) {
//            for (SearchHit hit : hits.getHits()) {
//                String sourceAsString = hit.getSourceAsString();
//                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
//                if (!StringUtils.isEmpty(paramVo.getKeyword())) {
//                    //设置高亮内容
//                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
//                    String string = skuTitle.getFragments()[0].string();
//                    skuEsModel.setSkuTitle(string);
//                }
//                esModelList.add(skuEsModel);
//            }
//        }
//        result.setProducts(esModelList);
//
//        //2.当前所有商品涉及到的所有属性信息
//        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
//        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
//        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");//根据返回值确定数据类ParsedLongTerms，ParsedNested
//        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
//            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
//            //1.得到属性的id
//            long attrId = bucket.getKeyAsNumber().longValue();
//            //2.得到属性的名字
//            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
//            //3.得到属性的所有值
//            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
//                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
//                return keyAsString;
//            }).collect(Collectors.toList());
//            attrVo.setAttrId(attrId);
//            attrVo.setAttrName(attrName);
//            attrVo.setAttrValue(attrValues);
//            attrVos.add(attrVo);
//        }
//        result.setAttrs(attrVos);
//        //3.当前所有商品涉及到的所有品牌信息
//        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
//        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
//        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
//            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
//            //1.品牌id
//            long brandId = bucket.getKeyAsNumber().longValue();
//            //2.品牌名字
//            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
//            //3.品牌图片
//            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
//            brandVo.setBrandId(brandId);
//            brandVo.setBrandName(brandName);
//            brandVo.setBrandImg(brandImg);
//            brandVos.add(brandVo);
//        }
//        result.setBrands(brandVos);
//        //4.当前所有商品涉及到的所有分类信息
//        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
//        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
//        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
//        for (Terms.Bucket bucket : buckets) {
//            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
//            //得到分类id
//            String keyAsString = bucket.getKeyAsString();
//            catalogVo.setCatalogId(Long.parseLong(keyAsString));
//            //得到分类名
//            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
//            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
//            catalogVo.setCatalogName(catalog_name);
//            catalogVos.add(catalogVo);
//        }
//        result.setCatalogs(catalogVos);
//        //5.当前所有商品涉及到的所有分页信息
//        //页码
//        result.setPageNum(paramVo.getPageNum());
////        result.setPageNum();
//        //总计录数
//        long total = hits.getTotalHits().value;
//        result.setTotal(total);
//        //总页码
//        Long totalPages = total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : (total / EsConstant.PRODUCT_PAGESIZE) + 1;
//        result.setTotalPage(totalPages);
//
//        List<Integer> pageNavs = new ArrayList<>();
//        for (int i = 1; i <= totalPages; i++) {
//            pageNavs.add(i);
//        }
//        result.setPageNavs(pageNavs);
//
//        //6.构建面包屑导航
//        if (paramVo.getAttrs() != null && paramVo.getAttrs().size() > 0) {
//            List<SearchResult.NavVo> collect = paramVo.getAttrs().stream().map(attr -> {
//                SearchResult.NavVo navVo = new SearchResult.NavVo();
//                //分析每个attr的参数值
//                String[] s = attr.split("_");
//                navVo.setNavValue(s[1]);
//                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
//                result.getAttrIds().add(Long.parseLong(s[0]));
//                if (r.getCode() == 0) {
//                    AttrResponseVo attrs = r.getData2("attr", new TypeReference<AttrResponseVo>() {
//                    });
//                    navVo.setNavName(attrs.getAttrName());
//                } else {
//                    navVo.setNavName(s[0]);
//                }
//                String replace = replaceQueryString(paramVo, attr, "attrs");
//                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
//                return navVo;
//            }).collect(Collectors.toList());
//            result.setNavs(collect);
//        }
//        //品牌、分类面包屑导航
//        if (paramVo.getBrandId() != null && paramVo.getBrandId().size() > 0) {
//            List<SearchResult.NavVo> navs = result.getNavs();
//            SearchResult.NavVo navVo = new SearchResult.NavVo();
//            navVo.setNavName("品牌");
//            //远程查询所有品牌
//            R r = productFeignService.brandsInfo(paramVo.getBrandId());
//            if (r.getCode() == 0) {
//                List<BrandVo> brands = r.getData2("brands", new TypeReference<List<BrandVo>>() {
//                });
//                StringBuffer buffer = new StringBuffer();
//                String replace = "";
//                for (BrandVo brandVo : brands){
//                    buffer.append(brandVo.getName()+";");
//                    replace = replaceQueryString(paramVo,brandVo.getBrandId()+"","brandId");
//                }
//                navVo.setNavValue(buffer.toString());
//                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
//            }
//            navs.add(navVo);
//        }
//        return result;
//    }
//
//    //取消了面包屑之后,跳转的位置(将请求地址的url替换,置空)
//    private String replaceQueryString(SearchParamVo paramVo, String value, String key) {
//        String encode = null;
//        try {
//            //编码
//            encode = URLEncoder.encode(value, "UTF-8");
//            encode = encode.replace("+", "%20");//对空格特殊处理(将空格变为%20)
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return paramVo.get_queryString().replace("&" + key + "=" + encode, "");
//    }
//}
