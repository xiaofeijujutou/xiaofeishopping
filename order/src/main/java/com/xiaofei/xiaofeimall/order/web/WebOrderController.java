package com.xiaofei.xiaofeimall.order.web;

import com.xiaofei.xiaofeimall.order.service.OrderService;
import com.xiaofei.xiaofeimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/17 14:59
 */

@Controller
public class WebOrderController {

    @Autowired
    OrderService orderService;
    /**
     * 回调页面
     * @param page
     * @return
     */
    @GetMapping("/{page}")
    public String redirectPage(@PathVariable("page") String  page){
        return page;
    }

    /**
     * 跳转到结算页面,并且把数据返回回去;
     * @param model
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo orderConfirmVo = orderService.toTrade();
        model.addAttribute("order", orderConfirmVo);
        return "confirm";
    }
}
