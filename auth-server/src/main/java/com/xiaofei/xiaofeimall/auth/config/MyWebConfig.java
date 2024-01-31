package com.xiaofei.xiaofeimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/29 18:56
 */

@Configuration
public class MyWebConfig implements WebMvcConfigurer {
    /**
     * MVC自带的视图控制,用于省略编写Controller的业务逻辑,直接实现页面跳转;
     * 相当于前置controller;
     * @param registry 注册器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //前面是Mapping里面的url,后面是return待返回的视图
        /**
         * 用户登录小肥商城的界面
         */
        registry.addViewController("/login.html").setViewName("login");
        /**
         * 用户注册小肥商城的界面
         */
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
