package com.xiaofei.xiaofeimall.product.service.impl;

import com.xiaofei.xiaofeimall.product.vo.SkuItemSaleAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.SkuSaleAttrValueDao;
import com.xiaofei.xiaofeimall.product.entity.SkuSaleAttrValueEntity;
import com.xiaofei.xiaofeimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据spu同款商品的id来查出有多少销售属性可以选(苹果15),
     * 查出来的销售属性就可以定位到具体的sku(库存单元的商品15 8+256 黑色)
     * @param spuId 商品id
     * @return 销售组合Attr;
     */
    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrValueBySpuId(Long spuId) {
        return skuSaleAttrValueDao.getSaleAttrValueBySpuId(spuId);
    }

}