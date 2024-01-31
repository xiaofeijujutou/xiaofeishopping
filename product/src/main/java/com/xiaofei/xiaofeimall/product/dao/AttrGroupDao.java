package com.xiaofei.xiaofeimall.product.dao;

import com.xiaofei.xiaofeimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.xiaofeimall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 属性分组
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:02:51
 */
@Mapper
@Repository
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    /**
     *   这个方法的作用是: 根据spuId, catalogId来查出一个spu商品最下面的Attr介绍
     * @param spuId 商品id
     * @param catalogId 三级分类id
     * @return 所有的Attr
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId")Long catalogId);
}
