package com.xiaofei.xiaofeimall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/29 15:17
 */

@ConfigurationProperties(prefix = "xiaofeimall.thread")
@Component
@Data
@Primary
public class MyGlobalThreadConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
    private Integer queueMaxCapacity;
}
