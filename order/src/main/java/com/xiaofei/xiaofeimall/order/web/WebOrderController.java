package com.xiaofei.xiaofeimall.order.web;

import com.xiaofei.common.constant.OrderConstant;
import com.xiaofei.xiaofeimall.order.service.OrderService;
import com.xiaofei.xiaofeimall.order.vo.OrderConfirmVo;
import com.xiaofei.xiaofeimall.order.vo.SubmitOrderResponseVo;
import com.xiaofei.xiaofeimall.order.vo.SubmitOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * 用户下单操作,创建订单,验证令牌,验证价格,锁库存;
     * @param submitOrderVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(SubmitOrderVo submitOrderVo, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderResponseVo responseVo = orderService.submitOrder(submitOrderVo);
        if (responseVo.getCode() == OrderConstant.ORDER_STATUS_SUCCESS){
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            String msg ="下单失败;";
            switch (responseVo.getCode()){
            case 1: msg +="订单信息过期，请刷新再次提交";break;
            case 2: msg+="订单商品价格发生变化，请确认后再次提交";break;
            case 3: msg+="库存锁定失败，商品库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.xiaofeimall.com/toTrade";
        }
    }
}
