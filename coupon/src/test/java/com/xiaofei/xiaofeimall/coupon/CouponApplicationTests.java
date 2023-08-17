package com.xiaofei.xiaofeimall.coupon;

import com.xiaofei.xiaofeimall.coupon.entity.CouponEntity;
import com.xiaofei.xiaofeimall.coupon.service.CouponService;
import org.junit.Test;

import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class CouponApplicationTests {

    @Resource
    private CouponService couponService;
    @Test
    public void contextLoads() {
        CouponEntity byId;
        try {
             byId = couponService.getById(1);
        }catch (NullPointerException nu){
            System.out.println("null");
            return;
        }

        System.out.println(byId);
    }

}
