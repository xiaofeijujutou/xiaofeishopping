package com.xiaofei.xiaofeimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * 属于电商管理员设置的attr属性;
 * 查询出来应该是List<Map<K,List<Map<K,V>>>> ==> List<Map<主体,List<Map<键,值>>>>
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/20 16:38
 */

@Data
public class SpuItemAttrGroupVo {
    //主体
    private String groupName;
    private List<SpuBaseAttrVo> attrs;

    //Map<键,值>
    @Data
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }
}
