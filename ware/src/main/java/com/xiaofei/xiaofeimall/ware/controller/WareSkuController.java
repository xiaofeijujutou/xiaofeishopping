package com.xiaofei.xiaofeimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.xiaofei.common.exception.BizCodeEnum;
import com.xiaofei.xiaofeimall.ware.vo.SkuHasStockVo;
import com.xiaofei.xiaofeimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaofei.xiaofeimall.ware.entity.WareSkuEntity;
import com.xiaofei.xiaofeimall.ware.service.WareSkuService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.R;


/**
 * 商品库存
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:52:39
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 锁定指定id的库存
     * @param vo
     * @return
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try {
            Boolean stockResults = wareSkuService.orderLockStock(vo);
            return R.ok().setData(stockResults);
        }catch (Exception e){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }

    }


    /**
     * 查看商品是否有库存
     */
    @PostMapping("/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds) {
        // sku_id, stock
        List<SkuHasStockVo> vos = wareSkuService.getSkusHasStock(skuIds);
        return R.ok().put("data", vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
