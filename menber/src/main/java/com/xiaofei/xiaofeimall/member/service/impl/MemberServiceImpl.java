package com.xiaofei.xiaofeimall.member.service.impl;

import com.xiaofei.common.constant.MemberConstant;
import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.member.dao.MemberLevelDao;
import com.xiaofei.xiaofeimall.member.entity.MemberLevelEntity;
import com.xiaofei.xiaofeimall.member.service.MemberLevelService;
import com.xiaofei.xiaofeimall.member.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.member.vo.MenberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.member.dao.MemberDao;
import com.xiaofei.xiaofeimall.member.entity.MemberEntity;
import com.xiaofei.xiaofeimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );
        return new PageUtils(page);
    }

    /**
     * 用户从注册页面发过来的注册请求
     * @param vo 参数
     * @return 状态
     */
    @Override
    public R register(MenberRegisterVo vo) {
        MemberEntity member = new MemberEntity();
        //隔壁服务传过来的参数(已经验证唯一性了)
        member.setPassword(vo.getPassword());
        member.setUsername(vo.getUserName());
        member.setMobile(vo.getPhone());
        //还要设置一些其他参数,比如会员等级;
        MemberLevelEntity defaultLevel = memberLevelDao.getDefaultLevel();
        member.setLevelId(defaultLevel.getId());
        //密码加盐存入数据库
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        member.setPassword(passwordEncoder.encode(vo.getPassword()));
        member.setCreateTime(new Date());
        member.setStatus(MemberConstant.USER_NORMAL_STATUS.getCode());
        this.baseMapper.insert(member);
        return R.ok();
    }

    /**
     * 检验用户名,手机号是否已经注册过了;
     * @param vo 需要校验唯一性的参数
     * @return ok/error
     */
    @Override
    public R checkUniqueness(CheckUniquenessVo vo) {
        MemberEntity memberEntity = this.baseMapper.checkUniqueness(vo);
        if (memberEntity == null){
            return R.ok();
        }
        if (vo.getPhone() != null && memberEntity.getMobile().equals(vo.getPhone())){
            return R.error(BizCodeEnume.USER_PHONE_EXIST_EXCEPTION.getCode(),
                    BizCodeEnume.USER_PHONE_EXIST_EXCEPTION.getMsg());
        }
        if (vo.getUserName() != null && memberEntity.getUsername().equals(vo.getUserName())){
            return R.error(BizCodeEnume.USER_NAME_EXIST_EXCEPTION.getCode(),
                    BizCodeEnume.USER_NAME_EXIST_EXCEPTION.getMsg());
        }
        return R.error("参数不能为空");
    }

}