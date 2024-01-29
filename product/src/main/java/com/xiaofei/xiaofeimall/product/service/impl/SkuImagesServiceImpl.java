package com.xiaofei.xiaofeimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.SkuImagesDao;
import com.xiaofei.xiaofeimall.product.entity.SkuImagesEntity;
import com.xiaofei.xiaofeimall.product.service.SkuImagesService;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据skuId来查出对应的所有sku的图片，
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImagesEntity> getSkuImagesListById(Long skuId) {
        return this.getBaseMapper().selectList(Wrappers.<SkuImagesEntity>lambdaQuery()
                .eq(SkuImagesEntity::getSkuId, skuId));
    }

}