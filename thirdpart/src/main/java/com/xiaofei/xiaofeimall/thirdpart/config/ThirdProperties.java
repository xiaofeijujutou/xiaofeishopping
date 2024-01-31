package com.xiaofei.xiaofeimall.thirdpart.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 13:00
 */

@Component
@Data
@Primary
@ConfigurationProperties(prefix = "xiaofeimall.third")
@PropertySource(value = "classpath:bootstrap.properties", encoding = "UTF-8")
public class ThirdProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
    private String signName;
    private String templateCode;
    private String region;



}
