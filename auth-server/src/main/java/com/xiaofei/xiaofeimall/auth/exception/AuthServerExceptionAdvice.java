package com.xiaofei.xiaofeimall.auth.exception;


import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Description: Created by IntelliJ IDEA.
 * 注册登录页面异常统一处理
 * @Author : 小肥居居头
 * @create 2024/1/29 18:25
 */
@Log4j2
@RestControllerAdvice(basePackages = "com.xiaofei.xiaofeimall.auth.controller")
public class AuthServerExceptionAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R handleValidException(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        Map<String,String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), "数据校验出现问题").put("data", errorMap);
    }

    /**
     * 统一处理参数异常;
     * @param e @NotNull参数等
     * @return 错误信息
     */
    @ExceptionHandler(value = BindException.class)
    public R handleBindException(BindException e){
        BindingResult result = e.getBindingResult();
        Map<String, String> errors = null;
        if (result.hasErrors()) {
            errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (message1, message2) -> message1));
        }
        log.error(BizCodeEnume.VAILD_EXCEPTION.getMsg()+errors.toString());
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data", errors);
    }



    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e){
        e.printStackTrace();
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
