package com.xiaofei.xiaofeimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.xiaofeimall.product.entity.AttrGroupEntity;
import com.xiaofei.xiaofeimall.product.vo.AttrGroupRelationVo;
import com.xiaofei.xiaofeimall.product.vo.AttrGroupWithAttrsVo;
import com.xiaofei.xiaofeimall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:02:51
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNobindAttr(Map<String, Object> params, Long attrgroupId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

