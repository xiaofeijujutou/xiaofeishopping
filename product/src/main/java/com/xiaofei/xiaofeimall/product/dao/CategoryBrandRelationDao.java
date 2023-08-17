package com.xiaofei.xiaofeimall.product.dao;

import com.xiaofei.xiaofeimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌分类关联
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:02:51
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {
	
}
