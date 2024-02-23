package com.xiaofei.xiaofeimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

//@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = "com.xiaofei.xiaofeimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class MenberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenberApplication.class, args);
    }

}
