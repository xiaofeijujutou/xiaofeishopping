package com.xiaofei.xiaofeimall.member.service.impl;

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

import com.xiaofei.xiaofeimall.member.dao.MemberReceiveAddressDao;
import com.xiaofei.xiaofeimall.member.entity.MemberReceiveAddressEntity;
import com.xiaofei.xiaofeimall.member.service.MemberReceiveAddressService;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }
    /**
     * 根据用户id来查他所有的收货地址;
     * @param memberId
     * @return
     */
    @Override
    public List<MemberReceiveAddressEntity> getAddressesByMemberId(Long memberId) {
        return this.baseMapper.selectList(Wrappers.<MemberReceiveAddressEntity>lambdaQuery()
                .eq(MemberReceiveAddressEntity::getMemberId, memberId));
    }

    /**
     * 根据地址主键id来查对应实体类;
     * @param id
     * @return
     */
    @Override
    public MemberReceiveAddressEntity getAddressesById(Long id) {
        return this.getById(id);
    }

}