package com.xiaofei.xiaofeimall.product.service.impl;

import com.xiaofei.xiaofeimall.product.service.CategoryBrandRelationService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.BrandDao;
import com.xiaofei.xiaofeimall.product.entity.BrandEntity;
import com.xiaofei.xiaofeimall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");

        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();


        //  and (attr_group_id=key or attr_group_name Like key);
        if (!Strings.isEmpty(key)){
            wrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);
        return new PageUtils(page);




//        IPage<BrandEntity> page = this.page(
//                new Query<BrandEntity>().getPage(params),
//                new QueryWrapper<BrandEntity>()
//        );

        //return new PageUtils(page);
    }

    /**
     * 解决表同步;
     * @param brand
     */
    @Override
    public void updateDetails(BrandEntity brand) {
        this.updateById(brand);
        if (!Strings.isEmpty(brand.getName())){
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
        }
    }

}