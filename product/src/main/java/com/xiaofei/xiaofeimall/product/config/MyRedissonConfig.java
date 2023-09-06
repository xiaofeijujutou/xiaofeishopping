//package com.xiaofei.xiaofeimall.product.config;
//
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.IOException;
//
//@Configuration
//public class MyRedissonConfig {
//
//    @Bean(destroyMethod = "shutdown")
//    RedissonClient redisson() throws IOException{
//        Config config = new Config();
//        //集群模式
//        //config.useClusterServers().addNodeAddress("127.0.0.1:7004","127.0.0.1:7001");
//        //单节点模式
//        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
//        return Redisson.create(config);
//    }
//}
