package com.xiaofei.xiaofeimall.member.dao;

import com.xiaofei.xiaofeimall.member.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:22:37
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
