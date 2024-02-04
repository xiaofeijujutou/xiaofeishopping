package com.xiaofei.xiaofeimall.auth.feign;

import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.auth.vo.UserLoginVo;
import com.xiaofei.xiaofeimall.auth.vo.UserRegisterVo;
import com.xiaofei.xiaofeimall.auth.vo.WeiboAccessTokenVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/31 15:29
 */

@FeignClient("xiaofei-menber")
public interface MemberPartFeignService {
    @PostMapping("member/member/checkUniqueness")
    R checkUniqueness(@RequestBody CheckUniquenessVo vo);

    @PostMapping("member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    /**
     * 用户登录,返回用户是否登录成功;
     * @param vo 账号密码
     * @return 结果+实体类
     */
    @PostMapping("member/member/login")
    R login(@RequestBody UserLoginVo vo);

    /**
     * 用户微博社交登录,如果是第一次登录就是注册,已经注册过了就是登录;
     * @param vo 微博accessToken
     * @return 结果+实体类
     */
    @PostMapping("member/member/weiboOauth/login")
    R weiboOauthLogin(@RequestBody WeiboAccessTokenVo vo);
}
