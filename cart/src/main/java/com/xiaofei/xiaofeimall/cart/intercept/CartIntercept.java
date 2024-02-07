package com.xiaofei.xiaofeimall.cart.intercept;

import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.constant.CartConstant;
import com.xiaofei.xiaofeimall.cart.constants.ThreadLocalConstant;
import com.xiaofei.xiaofeimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.xiaofei.common.vo.MemberEntityVo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * @Description: Created by IntelliJ IDEA.
 * 在执行目标方法之前,判断用户登录状态;
 * @Author : 小肥居居头
 * @create 2024/2/6 15:30
 */

@Component
public class CartIntercept implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        //获取session,判断是否登录
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberEntityVo member = (MemberEntityVo) session.getAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX);
        if (member != null) {
            //登录了
            userInfoTo.setUserId(member.getId());
        }
        if (member == null) {
            //没登录有user-key->找出user-key放入线程存储
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_KEY)) {
                    userInfoTo.setUserKey(cookie.getValue());
                }
            }
            //没登录且没有user-key->创建临时用户;
            if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
                String userKey = UUID.randomUUID().toString().replace("-", "");
                userInfoTo.setUserKey(userKey);
                Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_KEY, userKey);
                cookie.setDomain("xiaofeimall.com");
                cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TTL);
                response.addCookie(cookie);
            }
        }

        //向ThreadLocal存入用户数据
        Map<String, Object> cartThreadLocal = ThreadLocalConstant.cartInterceptThreadLocal.get();
        if (CollectionUtils.isEmpty(cartThreadLocal)) {
            cartThreadLocal = new HashMap<>();
            ThreadLocalConstant.cartInterceptThreadLocal.set(cartThreadLocal);
        }
        cartThreadLocal.put(ThreadLocalConstant.KEY_OF_USERINFOTO, userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        ThreadLocalConstant.cartInterceptThreadLocal.remove();
    }
}
