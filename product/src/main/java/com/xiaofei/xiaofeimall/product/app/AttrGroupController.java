package com.xiaofei.xiaofeimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.xiaofei.xiaofeimall.product.entity.AttrEntity;
import com.xiaofei.xiaofeimall.product.service.AttrAttrgroupRelationService;
import com.xiaofei.xiaofeimall.product.service.AttrService;
import com.xiaofei.xiaofeimall.product.service.CategoryService;
import com.xiaofei.xiaofeimall.product.vo.AttrGroupRelationVo;
import com.xiaofei.xiaofeimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaofei.xiaofeimall.product.entity.AttrGroupEntity;
import com.xiaofei.xiaofeimall.product.service.AttrGroupService;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.R;



/**
 * 属性分组
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 16:53:41
 */
//操作的表: pms_attr_group
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    AttrService attrService;
    @Autowired
    AttrAttrgroupRelationService AARService;


    /**
     * 新增商品的第二步;
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId")Long catelogId){
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", vos);
    }




        /**
         * 在group里面新建attr的关联关系,只保存俩id就行了,不用保存name
         *
         */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo>vos) {
        AARService.saveBatch(vos);

        return R.ok();

    }
        /**
         * /api/product/attrgroup/5/noattr/relation?t=1692972411184&page=1&limit=10&key="
         * /{attrgroupId}/noattr/relation
         */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getNobindAttr(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId")Long attrgroupId){
        PageUtils pageUtils = attrGroupService.getNobindAttr(params, attrgroupId);
        return R.ok().put("page", pageUtils);
    }



    /**
     * 删除关联关系
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrGroupService.deleteRelation(vos);

        return R.ok();
    }


    /**
     * 点击关联, 然后需要回显数据
     * 列表/product/attrgroup/{attrgroupId}/attr/relation
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrgroupId);

        return R.ok().put("data", attrEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("categoryId") Long categoryId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, categoryId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     * 查一个且或缺CategoryPath;
     */
    //@RequiresPermissions("product:attrgroup:info")
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        attrGroup.setCatelogPath(categoryService.getCategoryPath(attrGroup.getCatelogId()));
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
