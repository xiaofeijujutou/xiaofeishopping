package com.xiaofei.xiaofeimall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
//@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRabbit
@EnableFeignClients(basePackages = "com.xiaofei.xiaofeimall.ware.feign")
@EnableTransactionManagement
@MapperScan(basePackages = "com.xiaofei.xiaofeimall.ware.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class WareApplication {

    public static void main(String[] args) {
        SpringApplication.run(WareApplication.class, args);
    }

}
