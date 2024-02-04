package com.xiaofei.xiaofeimall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @Description: Created by IntelliJ IDEA.
 * 微博社交登录的配置
 * @Author : 小肥居居头
 * @create 2024/1/29 15:17
 */

@ConfigurationProperties(prefix = "xiaofeimall.weibo")
@Component
@Data
@Primary
public class WeiboOauthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;

}
