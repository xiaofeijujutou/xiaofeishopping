package com.xiaofei.xiaofeimall.product.controller;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.xiaofei.xiaofeimall.product.entity.ProductAttrValueEntity;
import com.xiaofei.xiaofeimall.product.service.ProductAttrValueService;
import com.xiaofei.xiaofeimall.product.vo.AttrResponseVo;
import com.xiaofei.xiaofeimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaofei.xiaofeimall.product.entity.AttrEntity;
import com.xiaofei.xiaofeimall.product.service.AttrService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.R;



/**
 * 商品属性
 * 管理商品参数
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:53:41
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);
        return R.ok().put("data",entities);
    }
        /**
         * 列表
         */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrResponseVo attrVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrVo);
    }

    /**
     * 基本保存 -> 升级保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
        if (attr.getCatelogId() == 0){
            return R.error("请选择三级菜单分类属性");
        }
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateById(attr);
        return R.ok();
    }
    /**
     * 修改Spu
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities) {
        productAttrValueService.updateSpuAttr(spuId, entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }


    /**
     * 分页查询
     * @param params 分页查询参数, 到时候直接丢Utils里
     * @param categoryId
     * @return
     * "/base/list/{categoryId}"全部查询
     * "/sale/list/{categoryId}"销售属性
     */
    @GetMapping("/{attrType}/list/{categoryId}")
    //@RequiresPermissions("product:attr:list")
    public R paginationList(@RequestParam Map<String, Object> params,
                            @PathVariable("categoryId") Long categoryId,
                            @PathVariable("attrType") String attrType){
        PageUtils page = attrService.queryPage(params, categoryId, attrType);
        return R.ok().put("page", page);
    }
}
