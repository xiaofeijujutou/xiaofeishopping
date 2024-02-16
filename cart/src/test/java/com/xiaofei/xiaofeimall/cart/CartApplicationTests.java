package com.xiaofei.xiaofeimall.cart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartApplicationTests extends ThreadLocal {

    public static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Test
    public void contextLoads() {

        Thread t1 = new Thread(()->{
            threadLocal.set("t1");

            System.out.println(threadLocal.get());
        });
        t1.setName("t1");


        Thread t2 = new Thread(() -> {
            threadLocal.set("t2");
            System.out.println(threadLocal.get());
        });
        t2.setName("t2");



        t1.start();
        t2.start();

    }

}
