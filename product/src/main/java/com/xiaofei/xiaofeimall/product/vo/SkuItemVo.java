package com.xiaofei.xiaofeimall.product.vo;

import com.xiaofei.xiaofeimall.product.entity.SkuImagesEntity;
import com.xiaofei.xiaofeimall.product.entity.SkuInfoEntity;
import com.xiaofei.xiaofeimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * item页面返回的页面模型,包含了一个销售商品的所有属性
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/20 15:35
 */

@ToString
@Data
public class SkuItemVo {


    //1、sku基本信息的获取  pms_sku_info
    private SkuInfoEntity info;

    //是否有货
    private boolean hasStock = true;

    //2、sku的图片信息    pms_sku_images
    private List<SkuImagesEntity> images;

    //3、获取spu的介绍
    private SpuInfoDescEntity desc;

    //4、获取商品的销售属性组合,属于卖家设置的attr属性;
    private List<SkuItemSaleAttrVo> saleAttr;

    //5、获取spu的规格参数信息,属于电商管理员设置的attr属性;
    private List<SpuItemAttrGroupVo> groupAttrs;

    //6、秒杀商品的优惠信息
    //private SeckillSkuVo seckillSkuVo;


}
