package com.xiaofei.xiaofeimall.cart.controller;


import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.constant.CartConstant;
import com.xiaofei.xiaofeimall.cart.constants.ThreadLocalConstant;
import com.xiaofei.xiaofeimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/5 20:51
 */

@Slf4j
@Controller
public class CartController {

    @GetMapping("/cartList.html")
    public String cartListPage(){
        Map<String, Object> threadLocalMap = ThreadLocalConstant.cartInterceptThreadLocal.get();
        UserInfoTo userInfoTo = (UserInfoTo)threadLocalMap.get(ThreadLocalConstant.KEY_OF_USERINFOTO);
        log.info(userInfoTo.toString());
        return "cartList";
    }
}
