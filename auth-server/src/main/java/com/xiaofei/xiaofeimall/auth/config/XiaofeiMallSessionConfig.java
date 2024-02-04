package com.xiaofei.xiaofeimall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @Description: Created by IntelliJ IDEA.
 * 往容器中注入bean,启动的时候就会自动注入就去
 * 这里配置的是RedisSession
 * @Author : 小肥居居头
 * @create 2024/2/3 19:57
 */

@Configuration
public class XiaofeiMallSessionConfig {
    /**
     * 配置Session作用域等Session常规配置
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        //放大作用域
        cookieSerializer.setDomainName("xiaofeimall.com");
        //自定义Cookie名字
        cookieSerializer.setCookieName("FEISESSION");
        return cookieSerializer;
    }


    /**
     * 配置RedisSession的序列化方式,这里是json的方式;
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}

