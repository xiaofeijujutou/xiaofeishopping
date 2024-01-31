package com.xiaofei.xiaofeimall.member.dao;

import com.xiaofei.xiaofeimall.member.entity.MemberEntity;
import com.xiaofei.xiaofeimall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 会员等级
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:22:37
 */
@Mapper
@Repository
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    /**
     * 调用这个接口,返回用户注册后分配的默认会员等级
     * @return 默认会员等级
     */
    MemberLevelEntity getDefaultLevel();

}
