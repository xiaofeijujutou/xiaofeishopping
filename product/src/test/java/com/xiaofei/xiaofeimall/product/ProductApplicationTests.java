package com.xiaofei.xiaofeimall.product;

//import com.aliyun.oss.OSSClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaofei.xiaofeimall.product.dao.AttrGroupDao;
import com.xiaofei.xiaofeimall.product.dao.SkuSaleAttrValueDao;
import com.xiaofei.xiaofeimall.product.entity.BrandEntity;
import com.xiaofei.xiaofeimall.product.service.BrandService;
import com.xiaofei.xiaofeimall.product.vo.SkuItemSaleAttrVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Resource
    BrandService brandService;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    //    @Resource
//    OSSClient ossClint;
//    @Test
//    public void upload() throws Exception{
//        InputStream inputStream = new FileInputStream(
//                "D:\\JavaProject\\谷粒商城\\renren-fast-vue-master\\static\\plugins\\ueditor-1.4.3.3\\dialogs\\image\\images\\alignicon.jpg");
//        ossClint.putObject("xiaofeitoushopping", "alignicon.jpg", inputStream);
//        ossClint.shutdown();
//        System.out.println("上传完成");
//    }
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
    @Test
    public void ttttt() {
        List<SkuItemSaleAttrVo> saleAttrValueBySpuId = skuSaleAttrValueDao.getSaleAttrValueBySpuId(7L);
        saleAttrValueBySpuId.forEach(System.out::println);
    }

}
