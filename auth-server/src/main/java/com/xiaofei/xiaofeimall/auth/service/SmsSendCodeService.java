package com.xiaofei.xiaofeimall.auth.service;

import com.xiaofei.common.utils.R;
import org.springframework.stereotype.Service;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 17:20
 */


@Service
public interface SmsSendCodeService {

    R sendCode(String phone);
}
