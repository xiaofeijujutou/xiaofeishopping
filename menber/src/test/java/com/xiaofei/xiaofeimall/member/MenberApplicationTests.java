package com.xiaofei.xiaofeimall.member;

import com.xiaofei.xiaofeimall.member.entity.MemberEntity;
import com.xiaofei.xiaofeimall.member.service.MemberService;
import org.junit.Test;

import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MenberApplicationTests {
    @Resource
    private MemberService memberService;
    @Test
    public void contextLoads() {
        MemberEntity entity;
        try {
            entity = memberService.getById(1);
        }catch (NullPointerException nu){
            System.out.println("null");
            return;
        }

        System.out.println(entity);
    }

}
