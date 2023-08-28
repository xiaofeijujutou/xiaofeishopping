package com.xiaofei.xiaofeimall.ware.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.ware.dao.WareInfoDao;
import com.xiaofei.xiaofeimall.ware.entity.WareInfoEntity;
import com.xiaofei.xiaofeimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {


        LambdaQueryWrapper<WareInfoEntity> wrapper = new LambdaQueryWrapper<>();



        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq(WareInfoEntity::getId, key)
                .or().like(WareInfoEntity::getName, key)
                .or().like(WareInfoEntity::getAddress, key)
                .or().like(WareInfoEntity::getAreacode, key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}