package com.xiaofei.xiaofeimall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.constant.MemberConstant;
import com.xiaofei.common.exception.BizCodeEnum;
import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.config.WeiboOauthProperties;
import com.xiaofei.xiaofeimall.auth.feign.MemberPartFeignService;
import com.xiaofei.xiaofeimall.auth.service.UserLoginService;
import com.xiaofei.common.vo.MemberEntityVo;
import com.xiaofei.xiaofeimall.auth.vo.WeiboAccessTokenVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.http.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/2 22:38
 */
@Log4j2
@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    MemberPartFeignService memberPartFeignService;
    @Autowired
    WeiboOauthProperties weiboOauthProperties;

    /**
     * 用户点击微博登录,输入账号密码成功之后,微博就会朝我们的接口发验证成功的code,
     * 我们要根据code再向微博发送请求,拿到用户的详细信息;
     * 详细信息有accessToken,code只能用一次,token可以复用;
     *
     * @param code    用户验证令牌
     * @param session
     * @return 我们项目自己的重定向地址
     */
    @Override
    public String weiboLogin(String code, HttpSession session) {
        //发送请求,post请求,但是参数都写在url里面,反人类;
        Map<String, Object> getTokenParam = Maps.newHashMap();
        getTokenParam.put("client_id", weiboOauthProperties.getClientId());
        getTokenParam.put("client_secret", weiboOauthProperties.getClientSecret());
        getTokenParam.put("response_type", "code");
        getTokenParam.put("redirect_uri", weiboOauthProperties.getRedirectUri());
        getTokenParam.put("code", code);
        HttpResponse tokenResponse = HttpRequest
                .post("https://api.weibo.com/oauth2/access_token")
                .form(getTokenParam)
                .execute();
        //微博登录失败,重定向到登录页面
        if (tokenResponse.getStatus() != HttpStatus.HTTP_OK) {
            return "redirect:http://auth.xiaofeimall.com";
        }
        //微博登录成功,如果是第一次登录,就是自动注册进来,远程调用member服务;
        WeiboAccessTokenVo weiboAccessTokenVo = JSON.parseObject(tokenResponse.body(), WeiboAccessTokenVo.class);
        //再次调用微博接口,查询用户在微博的名字,性别等; TODO:把过期时间保存到Redis,如果没过期,
        try {
            Map<String, Object> param = Maps.newHashMap();
            param.put("access_token", weiboAccessTokenVo.getAccess_token());
            param.put("uid", weiboAccessTokenVo.getUid());
            HttpResponse infoResponse = HttpRequest
                    .get("https://api.weibo.com/2/users/show.json")
                    .form(param)
                    .execute();
            JSONObject weiboUserInfo = JSON.parseObject(infoResponse.body());
            weiboAccessTokenVo.setName(weiboUserInfo.getString("name"));
            weiboAccessTokenVo.setGender("m".equals(weiboUserInfo.getString("gender")) ?
                    MemberConstant.SEX_MAN : MemberConstant.SEX_WOMAN);
            weiboAccessTokenVo.setLocation(weiboUserInfo.getString("location"));
        } catch (Exception e) {
            //即使获取详情失败了,也要给用户登录,只不过数据库会少一点东西
            log.error("微博获取用户详细信息失败");
        }
        //远程调用member
        R r = memberPartFeignService.weiboOauthLogin(weiboAccessTokenVo);
        //微博登录失败,重定向到登录页面
        if (r.getCode() != BizCodeEnum.SUCCESS_CODE.getCode()) {
            return "redirect:http://auth.xiaofeimall.com";
        }
        //登录成功,添加重定向参数,这里已经跨域了,所以这里要使用分布式Session;
        MemberEntityVo member = r.getDataByKey("member", new TypeReference<MemberEntityVo>() {});
        session.setAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX, member);
        return "redirect:http://xiaofeimall.com";
    }
}
