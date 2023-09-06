package com.xiaofei.xiaofeimall.search;

import com.alibaba.fastjson.JSON;
import com.xiaofei.xiaofeimall.search.config.ElasticsearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() throws Exception {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        String json = JSON.toJSONString(user);
        indexRequest.source(json, XContentType.JSON);

        IndexResponse response = client.index(indexRequest, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(response);
    }
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }

}
