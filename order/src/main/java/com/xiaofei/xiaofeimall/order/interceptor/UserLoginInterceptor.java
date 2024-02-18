package com.xiaofei.xiaofeimall.order.interceptor;

import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.vo.MemberEntityVo;
import com.xiaofei.xiaofeimall.order.config.OrderThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


/**
 * @Description: Created by IntelliJ IDEA.
 * 在执行目标方法之前,判断用户登录状态;
 * @Author : 小肥居居头
 * @create 2024/2/17 15:30
 */

@Component
public class UserLoginInterceptor implements HandlerInterceptor {


    /**
     * 前置拦截,要判断用户是否登录了,登录才给放行
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        HttpSession session = request.getSession();
        MemberEntityVo member = (MemberEntityVo) session.getAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX);
        if (member == null){
            session.setAttribute("msg", "请先登录");
            try {
                response.sendRedirect("http://auth.xiaofeimall.com/login.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        //不为空,存入ThreadLocal;
        OrderThreadLocal.orderUserInfoThreadLocal.set(member);
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
        OrderThreadLocal.orderUserInfoThreadLocal.remove();
    }
}
