package com.xiaofei.xiaofeimall.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaofei.xiaofeimall.product.entity.BrandEntity;
import com.xiaofei.xiaofeimall.product.service.BrandService;
import org.junit.Test;
//import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.lang.invoke.LambdaMetafactory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Resource
    BrandService brandService;

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("居头手机,你值得拥有");
        brandEntity.setName("居头手机");
        brandService.save(brandEntity);
    }

    @Test
    public void get() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("居头手机,你值得拥有");
        brandEntity.setName("居头手机");
        LambdaQueryWrapper<BrandEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrandEntity::getDescript, "居头手机,你值得拥有");
        BrandEntity one = brandService.getOne(wrapper);
        System.out.println(one.toString());
    }

}
