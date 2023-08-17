package com.xiaofei.xiaofeimall.product.dao;

import com.xiaofei.xiaofeimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:02:51
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
