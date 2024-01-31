package com.xiaofei.xiaofeimall.auth.service.impl;

import com.xiaofei.common.constant.AuthServerConstant;
import com.xiaofei.common.exception.BizCodeEnume;
import com.xiaofei.common.utils.R;
import com.xiaofei.xiaofeimall.auth.feign.ThirdPartFeignService;
import com.xiaofei.xiaofeimall.auth.service.SmsSendCodeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 17:20
 */

@Log4j2
@Service
public class SmsSendCodeServiceImpl implements SmsSendCodeService {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 验证码防刷,防止别人把验证码刷没钱;
     * 注册接口的获取验证码,调远程服务的验证码;
     * @param phone 手机号
     * @return 状态
     */
    @Override
    public R sendCode(String phone) {
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        //如果验证码存在,且间隔时间小于一分钟,则返回错误信息;
        if (!StringUtils.isEmpty(redisCode) &&
                System.currentTimeMillis() - Long.parseLong(redisCode.split("_")[1]) < 60 * 1000) {
            log.error("验证码发送频率过高");
            return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), "验证码发送频率过高");
        }
        //如果超过一分钟,可以重新发送;
        String code = Integer.toString(ThreadLocalRandom.current().nextInt(1000, 999999));
        R r = thirdPartFeignService.sendCode(phone, code);
        log.info(r.getCode());
        //code后面加上时间戳
        code = code + "_" + System.currentTimeMillis();
        //发送完了验证码之后要放入Redis缓存起来;
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        return R.ok();
    }
}
