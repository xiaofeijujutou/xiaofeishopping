package com.xiaofei.xiaofeimall.cart.intercept;

import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.constant.CartConstant;
import com.xiaofei.xiaofeimall.cart.constants.ThreadLocalConstant;
import com.xiaofei.xiaofeimall.cart.utils.CartUtils;
import com.xiaofei.xiaofeimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
                             Object handler) {
        //获取session,判断是否登录
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberEntityVo member = (MemberEntityVo) session.getAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX);
        //查找用户是否有临时凭证,如果有就加到UserInfoTo里面去,这里并不会创建新的cookie,只是查找cookie;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_KEY)) {
                userInfoTo.setUserKey(cookie.getValue());
            }
        }

        if (member != null) {
            //登录了,还是要判断user-key,来判断是否清空购物车;
            userInfoTo.setUserId(member.getId());
        }
        if (member == null) {
            //没登录有user-key->找出user-key放入线程存储
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
        cartThreadLocal.put(ThreadLocalConstant.KEY_OF_USER_INFO_TO, userInfoTo);
        return true;
    }


    /**
     * 后置拦截器,清空ThreadLocal防止内存泄漏;
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        try{
            //删除cookie的信号
            if(CartConstant.COOKIE_DELETE_SIGH.equals(CartUtils.getUserLoginInfo().getUserKey())){
                Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_KEY, null); // 创建同名的 Cookie，并将值设为 null
                cookie.setMaxAge(0); // 设置最大存活时间为 0，即立即删除该 Cookie
                cookie.setDomain("xiaofeimall.com");
                response.addCookie(cookie); //
            }
        }finally {
            ThreadLocalConstant.cartInterceptThreadLocal.remove();

        }
    }
}
