package com.xiaofei.xiaofeimall.member.dao;

import com.xiaofei.xiaofeimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaofei.xiaofeimall.member.vo.CheckUniquenessVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:22:37
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    /**
     * 校验vo数据是否唯一
     * @param vo 需要校验所有参数
     * @return 实体类
     */
    MemberEntity checkUniqueness(@Param("vo") CheckUniquenessVo vo);
}
