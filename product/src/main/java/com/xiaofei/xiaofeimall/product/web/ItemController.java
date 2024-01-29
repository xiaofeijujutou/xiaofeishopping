package com.xiaofei.xiaofeimall.product.web;

import com.xiaofei.xiaofeimall.product.service.SkuInfoService;
import com.xiaofei.xiaofeimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.server.PathParam;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/20 15:13
 */

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 用户发送商品id来跳转到指定商品的详情页;
     * @param skuId 商品id
     * @return 渲染好的页面
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
