package com.xiaofei.xiaofeimall.product.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @Description: Created by IntelliJ IDEA.
 * 线程池
 * @Author : 小肥居居头
 * @create 2024/1/29 15:00
 */

@EnableConfigurationProperties(MyGlobalThreadConfigProperties.class)
@Configuration
public class MyGlobalThreadPoolConfig {
    /**
     * 商品模块全局线程池配置
     * @param pool 配置类
     * @return 全局线程池
     */
    @Bean
    public ThreadPoolExecutor productGlobalThreadPool(MyGlobalThreadConfigProperties pool){
        return new ThreadPoolExecutor(pool.getCoreSize(),
                pool.getMaxSize(),
                pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(pool.getQueueMaxCapacity()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

}
