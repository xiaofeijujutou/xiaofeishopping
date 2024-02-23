package com.xiaofei.xiaofeimall.product.exception;


import com.xiaofei.common.exception.BizCodeEnum;
import com.xiaofei.common.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


//@ResponseBody
//@ControllerAdvice
@RestControllerAdvice(basePackages = "com.xiaofei.xiaofeimall.product.app")
public class XiaofeiShoppingExceptionAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, })
    public R handleValidException(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "数据校验出现问题").put("data", errorMap);
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e){
        e.printStackTrace();
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
