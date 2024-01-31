package com.xiaofei.xiaofeimall.member.controller;

import java.util.Arrays;
import java.util.Map;


import com.xiaofei.xiaofeimall.member.vo.CheckUniquenessVo;
import com.xiaofei.xiaofeimall.member.vo.MenberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaofei.xiaofeimall.member.entity.MemberEntity;
import com.xiaofei.xiaofeimall.member.service.MemberService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.R;

import javax.validation.Valid;


/**
 * 会员
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:22:37
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 检验用户名,手机号是否已经注册过了;
     * @param vo 需要校验唯一性的参数
     * @return ok/error
     */
    @PostMapping("/checkUniqueness")
    public R checkUniqueness(@RequestBody CheckUniquenessVo vo){
        return memberService.checkUniqueness(vo);
    }
    /**
     * 用户从注册页面发过来的注册请求
     * @param vo 参数
     * @return 状态
     */
    @PostMapping("/register")
    public R register(@RequestBody MenberRegisterVo vo){
        return memberService.register(vo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
