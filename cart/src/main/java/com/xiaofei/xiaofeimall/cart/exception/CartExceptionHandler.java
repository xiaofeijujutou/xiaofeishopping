package com.xiaofei.xiaofeimall.cart.exception;

import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/16 18:02
 */

@RestControllerAdvice(basePackages = "com.xiaofei.xiaofeimall.cart.controller")
public class CartExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e){
        e.printStackTrace();
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
