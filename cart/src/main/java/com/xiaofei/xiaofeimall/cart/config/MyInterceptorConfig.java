package com.xiaofei.xiaofeimall.cart.config;

import com.xiaofei.xiaofeimall.cart.intercept.CartIntercept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/7 12:27
 */


@Configuration
public class MyInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    CartIntercept cartIntercept;
    /**
     * 实现接口之后重写添加拦截器方法,就替代了传统Mvc添加拦截器的方法;
     * @param registry 拦截器注册器;
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 这样写的话,new CartIntercept()一个对象对应一个路径,
         * 那么一个类里面最多写两个拦截方法(前置后置),也可以对一个路径进行多次拦截;
         */
        registry.addInterceptor(cartIntercept).addPathPatterns("/**");

    }
}
