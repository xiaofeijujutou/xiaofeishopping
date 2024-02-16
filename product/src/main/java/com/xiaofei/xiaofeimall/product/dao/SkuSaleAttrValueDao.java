package com.xiaofei.xiaofeimall.product.dao;

import com.xiaofei.xiaofeimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.xiaofeimall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:02:51
 */
@Mapper
@Repository
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    /**
     * 根据大类同款商品的spuId来查出底下归属的sku的销售选项;
     * 还有每个attr属性对应的skuIds
     * @param spuId 同款商品的id
     * @return
     */
    List<SkuItemSaleAttrVo> getSaleAttrValueBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesById(@Param("skuId") Long skuId);

}
