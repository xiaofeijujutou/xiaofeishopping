

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
import org.elasticsearch.common.recycler.Recycler;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
        log.info("请求参数:" + paramVo.toString());
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
        log.info("查找成功返回" + searchResult.getTotal() + "条数据");
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
                //source就是数据库里存的原装数据,没有_index什么别的数据;
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //设置高亮
                if (!StringUtils.isEmpty(paramVo.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String high = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(high);
                }
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
                brandVo.setBrandImg(brandImgAggrBuckets.get(0).getKeyAsString());
                return brandVo;
            }).collect(Collectors.toList());
        }
        resultVo.setBrands(brandVos);


        //5,设置总记录数
        resultVo.setTotal(hits.getTotalHits().value);
        //6,设置分页信息(总记录数除以每一页大小)残缺版 TODO 优化
        resultVo.setTotalPage((hits.getTotalHits().value + EsConstant.PRODUCT_PAGESIZE - 1) / EsConstant.PRODUCT_PAGESIZE);
        resultVo.setPageNum(paramVo.getPageNum());
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= resultVo.getTotalPage(); i++) {
            pageNavs.add(i);
        }
        resultVo.setPageNavs(pageNavs);



        //7,attr面包屑
        if (!CollectionUtils.isEmpty(paramVo.getAttrs())){
            /**面包屑是前段传来一个value, 要判断这个value在那个name里面;
            * 前端的path: &attrs=1_10700 &attrs=3_3060;3060ti  value
            *               cpu            GPT                name
            * ES的数据
            *  "attrs" : [
            *             {
            *               "attrId" : 4,
            *               "attrName" : "上市年份",
            *               "attrValue" : "2023年"
            *             },
            * @Data
            *     public static class AttrVo{
            *         private Long attrId;
            *         private String attrName;
            *         private List<String> attrValue;
            *     }
            * attrVos从list变成map
             ES的数据拿过来
            */
            Map<String, String> attrVoMap = attrVos.stream()
                    .flatMap(attrVo -> attrVo.getAttrValue().stream()
                            .map(attrValue -> new AbstractMap.SimpleEntry<>(attrValue, attrVo.getAttrName())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,   // KeyMapper：将attrValue作为键
                            Map.Entry::getValue    // ValueMapper：将attrName作为值
                    ));
            //从前端的getAttrs拿数据
            //前端的path: &attrs=1_10700 &attrs=3_3060;3060ti  value
            //              cpu            GPT                name
            //这里要根据前段的参数来循环
            List<SearchResult.NavVo> navVoResultPart = paramVo.getAttrs().stream().map((attr) -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] attrFiled = attr.split("_");
                navVo.setNavValue(attrFiled[1]);
                navVo.setNavName(attrVoMap.get(attrFiled[1]));
                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                navVo.setLink(replaceRequestPathParam(paramVo.get_queryString(),  "attrs", attr));
                return navVo;
            }).collect(Collectors.toList());
            List<SearchResult.NavVo> navs = resultVo.getNavs();
            navs.addAll(navVoResultPart);
        }

        //8,brandId面包屑
        if (!CollectionUtils.isEmpty(paramVo.getBrandId())){
            //从es里面获取brandId和brandName;
            Map<Long, String> resultMap = products.stream()
                    .filter(distinctByKey(SkuEsModel::getBrandId)) // 过滤掉重复的brandId
                    .collect(Collectors.toMap(
                            SkuEsModel::getBrandId, // 键为id
                            SkuEsModel::getBrandName // 值为brandName
                    ));

            List<SearchResult.NavVo> navVoResultPart = paramVo.getBrandId().stream().map((brandId) -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                navVo.setNavName(brandId + "");
                navVo.setNavValue(resultMap.get(brandId));
                navVo.setLink(replaceRequestPathParam(paramVo.get_queryString(), "brandId", brandId + ""));
                return navVo;
            }).collect(Collectors.toList());

            List<SearchResult.NavVo> navs = resultVo.getNavs();
            navs.addAll(navVoResultPart);
        }
        return resultVo;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * 替换请求url的参数,形成回调
     * @param queryString url?后面的参数
     * @param key url里面的key
     * @param value url里面的value
     * @return 替换好的url
     */
    private String replaceRequestPathParam(String queryString, String key, String value) {
        String encode = null;
        try {
            //请求体的value才需要转码,直接修改value的值
            encode = URLEncoder.encode(value,"UTF-8");
            encode.replace("+","%20");  //浏览器对空格的编码和Java不一样，差异化处理
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //把以前的参数给删掉  &attrs=1_10700
        String replace = queryString
                .replace("&"+key+"=" + value, "")
                .replace(key + "=" + value, "");
        return "http://search.xiaofeimall.com/list.html?" + replace;
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
            highlight.postTags("</b>");
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
        //查询是否有库存,1有库存,0无库存,参数为空就全查;
        if (!ObjectUtils.isEmpty(paramVo.getHasStock())){
            finalQueryBuilder.filter(QueryBuilders.termQuery("hasStock",
                    paramVo.getHasStock() == 1 ? "true" : "false"));
        }
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
                //一个attrStr分解出的有用的属性就是attrId,attrValue  id_a:b
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




