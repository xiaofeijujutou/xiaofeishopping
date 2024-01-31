package com.xiaofei.xiaofeimall.auth.service;

import com.xiaofei.xiaofeimall.auth.vo.UserRegisterVo;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/1/30 23:16
 */

@Service
public interface UserRegisterService {
    String register(UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes);
}
