package com.xiaofei.xiaofeimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.xiaofeimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:22:37
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

