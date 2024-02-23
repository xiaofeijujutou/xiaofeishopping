package com.xiaofei.xiaofeimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * 给出对应商品的skuId和用户下单数量num,来查能发货的仓库id
 * @Author : 小肥居居头
 * @create 2024/2/20 20:02
 */


@Data
public class SkuWareHasStockVo {
    //商品id
    private Long skuId;
    //商品数量
    private Integer num;
    //能发货的仓库id
    private List<Long> wareId;
}
