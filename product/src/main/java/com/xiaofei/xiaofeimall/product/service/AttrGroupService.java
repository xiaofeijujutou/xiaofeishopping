package com.xiaofei.xiaofeimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.xiaofeimall.product.entity.AttrGroupEntity;

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
}

