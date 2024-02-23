package com.xiaofei.xiaofeimall.ware.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaofei.xiaofeimall.ware.feign.MemberFeignService;
import com.xiaofei.xiaofeimall.ware.vo.FareVo;
import com.xiaofei.xiaofeimall.ware.vo.MemberReceiveAddressEntityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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

    @Autowired
    MemberFeignService memberFeignService;
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

    /**
     * 这里是仓库模块,获取运费之前要计算仓库到地址的距离;
     * @param addrId
     * @return 运费
     */
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        MemberReceiveAddressEntityVo addrInfo = memberFeignService.getAddressesById(addrId);
        if (addrInfo != null){
            //TODO 根据用户地址到仓库的距离远近来计算运费
            String province = addrInfo.getProvince();
            fareVo.setAddress(addrInfo);
            //计算运费
            fareVo.setFare(new BigDecimal(new Random().nextInt(50)));
            return fareVo;
        }
        return null;
    }

}