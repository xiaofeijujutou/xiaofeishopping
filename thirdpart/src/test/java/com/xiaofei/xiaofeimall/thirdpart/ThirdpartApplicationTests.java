package com.xiaofei.xiaofeimall.thirdpart;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;

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

}
