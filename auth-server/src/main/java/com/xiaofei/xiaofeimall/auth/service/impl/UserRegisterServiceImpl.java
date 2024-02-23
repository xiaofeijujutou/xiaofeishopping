package com.xiaofei.xiaofeimall.auth.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.exception.BizCodeEnum;
import com.xiaofei.common.utils.R;
import com.xiaofei.common.vo.MemberEntityVo;
import com.xiaofei.xiaofeimall.auth.feign.MemberPartFeignService;
import com.xiaofei.xiaofeimall.auth.service.UserRegisterService;
import com.xiaofei.xiaofeimall.auth.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.auth.vo.UserLoginVo;
import com.xiaofei.xiaofeimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
            if (BizCodeEnum.SUCCESS_CODE.getCode() != r.getCode()){
                errors = new HashMap<>();
                if (BizCodeEnum.USER_PHONE_EXIST_EXCEPTION.getCode() == r.getCode()){
                    errors.put("phone", r.getMsg());
                }
                if (BizCodeEnum.USER_NAME_EXIST_EXCEPTION.getCode() == r.getCode()){
                    errors.put("userName", r.getMsg());
                }
            }
            return errors;
        }, authServerGlobalThreadPool);
        //统一处理参数异常
        try {
            Map<String, String> finalErrorMap = new HashMap<>();
            mergeMap(finalErrorMap, checkParamFeature.get(), checkCodeFeature.get(), checkUniquenessFeature.get());
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
        stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        memberPartFeignService.register(vo);
        return "redirect:http://auth.xiaofeimall.com/login.html";
    }
    /**
     * 把其他map里面的值merge到一个map
     * @param target 合并的结果map
     * @param sources 待合并的map
     */
    private void mergeMap(Map<String, String> target, Map<String, String>... sources) {
        for (Map<String, String> source : sources) {
            if (!CollectionUtils.isEmpty(source)) {
                target.putAll(source);
            }
        }
    }
    /**
     * 用户登录接口,成功之后转发到首页,然后携带上参数;
     * @param vo 账号密码
     * @param redirectAttributes 重定向
     * @param session 全局session
     * @return 主页
     */
    @Override
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R loginResult = memberPartFeignService.login(vo);
        if (loginResult.getCode() != BizCodeEnum.SUCCESS_CODE.getCode()){
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", loginResult.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.xiaofeimall.com/login.html";
        }
        //往session里面放数据
        session.setAttribute(AuthServerConstant.OAUTH_SESSION_PREFIX, loginResult.getDataByKey("member", new TypeReference<MemberEntityVo>() {}));
        return "redirect:http://xiaofeimall.com";
    }


}
