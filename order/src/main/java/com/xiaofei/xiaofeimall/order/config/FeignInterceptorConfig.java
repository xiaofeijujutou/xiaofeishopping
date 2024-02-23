package com.xiaofei.xiaofeimall.order.config;

import cn.hutool.http.HttpRequest;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jdk.management.resource.ResourceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/17 22:01
 */

@Configuration
public class FeignInterceptorConfig {

    /**
     * 创建Feign发送请求之前的拦截器,这里和Service里面是同一个线程;
     * @return
     */
    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
                HttpServletRequest originRequest = attributes.getRequest();
                String cookie = originRequest.getHeader("Cookie");
                template.header("Cookie", cookie);
            }
        };
    }
}
