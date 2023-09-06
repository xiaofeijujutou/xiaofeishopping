package com.xiaofei.xiaofeimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.xiaofeimall.product.entity.AttrEntity;
import com.xiaofei.xiaofeimall.product.vo.AttrResponseVo;
import com.xiaofei.xiaofeimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:02:51
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryPage(Map<String, Object> params, Long categoryId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateById(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

