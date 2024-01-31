package com.xiaofei.xiaofeimall.thirdpart;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.oss.OSSClient;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import io.prometheus.client.Collector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.aliyun.tea.*;
import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ThirdpartApplicationTests {
    @Resource
    OSSClient ossClint;
    @Test
    public void contextLoads() {
    }


    @Test
    public void upload() throws Exception{
        InputStream inputStream = new FileInputStream(
                "D:\\JavaProject\\谷粒商城\\renren-fast-vue-master\\static\\plugins\\ueditor-1.4.3.3\\dialogs\\image\\images\\alignicon.jpg");
        ossClint.putObject("xiaofeitoushopping", "jujuju.jpg", inputStream);
        ossClint.shutdown();
        System.out.println("上传完成");
    }





    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = "dysmsapi.aliyuncs.com";
        config.regionId = "cn-shenzhen";
        return new Client(config);
    }

    @Test
    public void sendMassage() throws Exception{
        Client client = createClient("LTAI5t7z6mkfF5UFuPMcF9EZ", "YamClltNMdX0cknBwfoEFba2RNLJu1");
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                //设置手机号
                .setPhoneNumbers("17773982935")
                //设置签名
                .setSignName("阿里云短信测试")
                //设置验证码
                .setTemplateCode("SMS_154950909")
                .setTemplateParam("{\"code\":\"1234\"}");
        RuntimeOptions runtime = new RuntimeOptions();
        client.sendSmsWithOptions(sendSmsRequest, runtime);

    }


}
