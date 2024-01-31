package com.xiaofei.xiaofeimall.thirdpart.component;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.xiaofei.xiaofeimall.thirdpart.config.ThirdProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 13:44
 */

@Log4j2
@Component
public class SmsComponent {
    @Autowired
    ThirdProperties thirdProperties;
    @Autowired
    Client client;

    public String sendMassage(String phone, String code) throws Exception {
        Client client = this.client;
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                //设置手机号
                .setPhoneNumbers(phone)
                //设置签名
                .setSignName("居居头的验证码")
                //设置验证码
                .setTemplateCode(thirdProperties.getTemplateCode())
                .setTemplateParam("{\"code\":\"" + code + "\"}");
        RuntimeOptions runtime = new RuntimeOptions();
        log.info("手机号:" + phone + "\t验证码:" + code);
        SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);
        log.info("阿里云短信服务通知=>message:" + sendSmsResponse.getBody().message + "\tcode:" + sendSmsResponse.getBody().code);
        return sendSmsResponse.getBody().message;
    }

}
