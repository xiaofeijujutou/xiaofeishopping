package com.xiaofei.xiaofeimall.member.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaofei.common.constant.MemberConstant;
import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.member.constant.OauthConstant;
import com.xiaofei.xiaofeimall.member.dao.MemberLevelDao;
import com.xiaofei.xiaofeimall.member.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.member.vo.MenberRegisterVo;
import com.xiaofei.xiaofeimall.member.vo.UserLoginVo;
import com.xiaofei.xiaofeimall.member.vo.WeiboAccessTokenVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

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
        member.setLevelId(memberLevelDao.getDefaultLevel().getId());
        //密码加盐存入数据库
        member.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));
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

    /**
     * 用户登录,返回用户是否登录成功;
     * @param vo 账号密码
     * @return 结果
     */
    @Override
    public R login(UserLoginVo vo) {
        MemberEntity memberEntity = this.baseMapper.selectOne(Wrappers.<MemberEntity>query()
                .eq("username", vo.getLoginAccount())
                .or()
                .eq("mobile", vo.getLoginAccount()));
        //账号不存在
        if (memberEntity == null){
            return R.error(BizCodeEnume.USER_LOGIN_ACCOUNT_NOT_EXIST_EXCEPTION.getCode(),
                    BizCodeEnume.USER_LOGIN_ACCOUNT_NOT_EXIST_EXCEPTION.getMsg());
        }
        //密码错误
        if (!bCryptPasswordEncoder.matches(vo.getLoginPassword(), memberEntity.getPassword())){
            return R.error(BizCodeEnume.USER_LOGIN_PASSWORD_EXCEPTION.getCode(),
                    BizCodeEnume.USER_LOGIN_PASSWORD_EXCEPTION.getMsg());
        }
        //登录成功
        return R.ok().put("member", memberEntity);
    }

    /**
     * 用户微博社交登录,如果是第一次登录就是注册,已经注册过了就是登录;
     * @param vo 微博accessToken
     * @return 结果+实体类
     */
    @Override
    public R weiboOauthLogin(WeiboAccessTokenVo vo) {
        MemberEntity memberEntity = this.baseMapper.selectOne(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getSocialUid, vo.getUid())
                .eq(MemberEntity::getSocialType, OauthConstant.WEIBO));
        //注册逻辑
        if (memberEntity == null){
            memberEntity = new MemberEntity();
            //设置社交登录属性
            memberEntity.setSocialUid(vo.getUid());
            memberEntity.setSocialType(OauthConstant.WEIBO);
            //这三个可能为空:
            memberEntity.setUsername(vo.getName() == null ? "" : vo.getName());
            memberEntity.setGender(vo.getGender() == null ? MemberConstant.SEX_UNKNOWN : vo.getGender());
            memberEntity.setCity(vo.getLocation() == null ? "" : vo.getLocation());
            //设置常规参数
            memberEntity.setLevelId(memberLevelDao.getDefaultLevel().getId());
            memberEntity.setCreateTime(new Date());
            memberEntity.setStatus(MemberConstant.USER_NORMAL_STATUS.getCode());
            this.baseMapper.insert(memberEntity);
        }
        //登录逻辑,还要更新一下从微博来的数据;
        memberEntity.setUsername(vo.getName() == null ? "" : vo.getName());
        memberEntity.setGender(vo.getGender() == null ? MemberConstant.SEX_UNKNOWN : vo.getGender());
        memberEntity.setCity(vo.getLocation() == null ? "" : vo.getLocation());
        this.baseMapper.updateById(memberEntity);
        return R.ok().put("member", memberEntity);
    }

}