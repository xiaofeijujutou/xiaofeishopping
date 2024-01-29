package com.xiaofei.xiaofeimall.product.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaofei.xiaofeimall.product.entity.SkuImagesEntity;
import com.xiaofei.xiaofeimall.product.entity.SpuInfoDescEntity;
import com.xiaofei.xiaofeimall.product.service.*;
import com.xiaofei.xiaofeimall.product.vo.SkuItemSaleAttrVo;
import com.xiaofei.xiaofeimall.product.vo.SkuItemVo;
import com.xiaofei.xiaofeimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.SkuInfoDao;
import com.xiaofei.xiaofeimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> wrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key);
            });
        }
        String brandId = (String) params. get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equals(brandId)) {
            wrapper.eq(SkuInfoEntity::getBrandId,brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq(SkuInfoEntity::getCatalogId,catelogId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge(SkuInfoEntity::getPrice, min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max) && Integer.parseInt(max) != 0) {
            wrapper.le(SkuInfoEntity::getPrice, max);
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBuyId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntityList = this.list(new LambdaQueryWrapper<SkuInfoEntity>().eq(SkuInfoEntity::getSpuId, spuId));
        return skuInfoEntityList;
    }

    /**
     * 根据search查出来的skuId库存量单位来获取单件商品详情
     * 以及获取这个商品归属于哪个品牌;
     * @param skuId skuId,库存量单位
     * @return 详情
     */
    @Override
    public SkuItemVo item(Long skuId) {
        // 0．创建返回值vo
        SkuItemVo skuItemResultVo = new SkuItemVo();
        // 1．获取sku的基本信息库存量的基本信息->根据id查数据库表就行了
        SkuInfoEntity spuInfo = this.getById(skuId);
        skuItemResultVo.setInfo(spuInfo);
        Long spuId = spuInfo.getSpuId();
        Long catalogId = spuInfo.getCatalogId();
        // 2．获取sku的图片信息 pms_sku_images
        List<SkuImagesEntity> skuImages = skuImagesService.getSkuImagesListById(skuId);
        skuItemResultVo.setImages(skuImages);
        // 3．获取spu的pms_spu_info_desc就是存的详情图片,需要skuId(上面已经查到了);
        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
        skuItemResultVo.setDesc(spuInfoDesc);
        // 4. 获取spu的所有销售属性集合(页面下端的参数),以前有根据三级分类来查,现在要根据spu来查
        List<SpuItemAttrGroupVo> spuAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        skuItemResultVo.setGroupAttrs(spuAttrGroupVos);
        // 5．获取sku的销售参数(决定价格的销售属性);
        List<SkuItemSaleAttrVo> saleAttrVo = skuSaleAttrValueService.getSaleAttrValueBySpuId(spuId);
        skuItemResultVo.setSaleAttr(saleAttrVo);
        // 6．TODO 设置是否有货
        return skuItemResultVo;
    }

}