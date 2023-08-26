package com.xiaofei.xiaofeimall.product.exception;


import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@ResponseBody
//@ControllerAdvice
@RestControllerAdvice(basePackages = "com.xiaofei.xiaofeimall.product.controller")
public class XiaofeiShoppingExceptionAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, })
    public R handleValidException(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), "数据校验出现问题").put("data", errorMap);
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e){
        System.out.println(e);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
