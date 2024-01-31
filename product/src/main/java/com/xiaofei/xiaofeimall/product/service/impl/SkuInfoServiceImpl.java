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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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
    @Autowired
    ThreadPoolExecutor productGlobalThreadPool;
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
     * 根据search查出来的skuId库存量单位来获取单件商品详情,
     * 以及获取这个商品归属于哪个品牌;
     * 整体使用异步编排的方式;
     * @param skuId skuId,库存量单位
     * @return 详情
     */
    @Override
    public SkuItemVo item(Long skuId) {
        // 0．创建返回值vo
        SkuItemVo skuItemResultVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(()->{
            // 1．获取sku的基本信息库存量的基本信息->根据id查数据库表就行了
            SkuInfoEntity spuInfo = this.getById(skuId);
            skuItemResultVo.setInfo(spuInfo);
            return spuInfo;
        }, productGlobalThreadPool);
        // 2．获取sku的图片信息 pms_sku_images
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImages = skuImagesService.getSkuImagesListById(skuId);
            skuItemResultVo.setImages(skuImages);
        }, productGlobalThreadPool);
        // 3．获取spu的pms_spu_info_desc就是存的详情图片,需要skuId(上面已经查到了);
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((spuInfo) -> {
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuInfo.getSpuId());
            skuItemResultVo.setDesc(spuInfoDesc);
        }, productGlobalThreadPool);
        // 4. 获取spu的所有销售属性集合(页面下端的参数),以前有根据三级分类来查,现在要根据spu来查
        CompletableFuture<Void> groupAttrsFuture = infoFuture.thenAcceptAsync((spuInfo) -> {
            List<SpuItemAttrGroupVo> spuAttrGroupVos =
                    attrGroupService.getAttrGroupWithAttrsBySpuId(spuInfo.getSpuId(), spuInfo.getCatalogId());
            skuItemResultVo.setGroupAttrs(spuAttrGroupVos);
        }, productGlobalThreadPool);
        // 5．获取sku的销售参数(决定价格的销售属性);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((spuInfo) -> {
            List<SkuItemSaleAttrVo> saleAttrVo = skuSaleAttrValueService.getSaleAttrValueBySpuId(spuInfo.getSpuId());
            skuItemResultVo.setSaleAttr(saleAttrVo);
        }, productGlobalThreadPool);
        //TODO 6,设置有货无货;

        // 7. 等待所有异步线程执行完毕,如果有异常则打印异常信息;
        CompletableFuture.allOf(imageFuture, descFuture, groupAttrsFuture, saleAttrFuture)
                .exceptionally((e)->{
                    e.printStackTrace();
                    return null;
                });
        return skuItemResultVo;
    }

}