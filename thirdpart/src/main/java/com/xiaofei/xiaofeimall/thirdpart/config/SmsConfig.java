package com.xiaofei.xiaofeimall.thirdpart.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 13:56
 */

@Configuration
public class SmsConfig {

    @Bean
    public Client client(ThirdProperties thirdProperties){
        Config config = new Config()
                .setAccessKeyId(thirdProperties.getAccessKeyId())
                .setAccessKeySecret(thirdProperties.getAccessKeySecret());
        config.endpoint = thirdProperties.getEndpoint();
        Client clientBean = null;
        try {
            clientBean = new Client(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientBean;
    }
}
