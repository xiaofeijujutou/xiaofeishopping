package com.xiaofei.xiaofeimall.auth.service.impl;

import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.feign.MemberPartFeignService;
import com.xiaofei.xiaofeimall.auth.service.UserRegisterService;
import com.xiaofei.xiaofeimall.auth.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 23:16
 */

@Service
@Slf4j
public class UserRegisterServiceImpl implements UserRegisterService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MemberPartFeignService memberPartFeignService;
    @Autowired
    ThreadPoolExecutor authServerGlobalThreadPool;
    /**
     * 用户注册请求,发送表单,然后存入数据库;
     * @param vo 用户请求
     * @param result 返回结果
     * @param redirectAttributes 重定向数据;
     * @return 返回页面
     */
    @Override
    public String register(UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        //校验参数
        CompletableFuture<Map<String, String>> checkParamFeature = CompletableFuture.supplyAsync(() -> {
            Map<String, String> errors = null;
            if (result.hasErrors()) {
                errors = result.getFieldErrors().stream()
                        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (message1, message2) -> message1));
            }
            return errors;
        }, authServerGlobalThreadPool);
        //比对验证码
        CompletableFuture<Map<String, String>> checkCodeFeature = CompletableFuture.supplyAsync(() -> {
            Map<String, String> errors = null;
            String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
            if (StringUtils.isEmpty(redisCode) || !redisCode.split("_")[0].equals(vo.getCode())){
                errors = new HashMap<>();
                errors.put("code", "验证码错误");
            }
            return errors;
        }, authServerGlobalThreadPool);
        //发送手机或者用户名唯一验证请求
        CompletableFuture<Map<String, String>> checkUniquenessFeature = CompletableFuture.supplyAsync(() -> {
            Map<String, String> errors = null;
            CheckUniquenessVo checkUniquenessVo = new CheckUniquenessVo();
            BeanUtils.copyProperties(vo, checkUniquenessVo);
            R r = memberPartFeignService.checkUniqueness(checkUniquenessVo);
            if (BizCodeEnume.SUCCESS_CODE.getCode() != r.getCode()){
                errors = new HashMap<>();
                if (BizCodeEnume.USER_PHONE_EXIST_EXCEPTION.getCode() == r.getCode()){
                    errors.put("phone", r.getMsg());
                }
                if (BizCodeEnume.USER_NAME_EXIST_EXCEPTION.getCode() == r.getCode()){
                    errors.put("userName", r.getMsg());
                }
            }
            return errors;
        }, authServerGlobalThreadPool);
        //统一异常
        try {
            Map<String, String> finalErrorMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(checkParamFeature.get())) {
                finalErrorMap.putAll(checkParamFeature.get());
            }
            if (!CollectionUtils.isEmpty(checkCodeFeature.get())) {
                finalErrorMap.putAll(checkCodeFeature.get());
            }
            if (!CollectionUtils.isEmpty(checkUniquenessFeature.get())) {
                finalErrorMap.putAll(checkUniquenessFeature.get());
            }
            if (!CollectionUtils.isEmpty(finalErrorMap)) {
                //重定向携带数据; TODO 改成分布式session;
                redirectAttributes.addFlashAttribute("errors", finalErrorMap);
                log.error(finalErrorMap.toString());
                return "redirect:http://auth.xiaofeimall.com/reg.html";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送远程调用,验证码比对成功,删除验证码
        CompletableFuture.runAsync(()->{
            memberPartFeignService.register(vo);
            stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        }, authServerGlobalThreadPool);
        return "redirect:http://auth.xiaofeimall.com/login.html";
    }
}
