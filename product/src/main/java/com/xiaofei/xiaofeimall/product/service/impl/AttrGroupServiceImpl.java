package com.xiaofei.xiaofeimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaofei.xiaofeimall.product.dao.AttrAttrgroupRelationDao;
import com.xiaofei.xiaofeimall.product.entity.AttrAttrgroupRelationEntity;
import com.xiaofei.xiaofeimall.product.entity.AttrEntity;
import com.xiaofei.xiaofeimall.product.service.AttrAttrgroupRelationService;
import com.xiaofei.xiaofeimall.product.service.AttrService;
import com.xiaofei.xiaofeimall.product.vo.AttrGroupRelationVo;
import com.xiaofei.xiaofeimall.product.vo.AttrGroupWithAttrsVo;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.AttrGroupDao;
import com.xiaofei.xiaofeimall.product.entity.AttrGroupEntity;
import com.xiaofei.xiaofeimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;
    @Autowired
    AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                //new Query只是一个Query, 然后调用getPage,就会调用吧map封装成page的方法
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        //select * from pms_attr_group
        //  where category_id=?
        //  and (attr_group_id=key or attr_group_name Like key);

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();

        if (catelogId == 0){//查询全部且模糊查询
            if (!Strings.isEmpty(key)){
                wrapper.and((lambdaWrapper)->{
                    lambdaWrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper);
            return new PageUtils(page);
        }else {
            wrapper.eq("catelog_id", catelogId);
            //  and (attr_group_id=key or attr_group_name Like key);
            if (!Strings.isEmpty(key)){
                wrapper.and((lambdaWrapper)->{
                    lambdaWrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 删除Group和Attr的关联关系
     * 先把VO的数据取出来放到实体表对应的Java类里面, 然后集中删除可以减少数据库的压力
     * @param vos
     */
    @Transactional
    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> AARList = null;
        AARList = Arrays.asList(vos).stream().map(vo->{
            AttrAttrgroupRelationEntity AAR = new AttrAttrgroupRelationEntity();
            AAR.setAttrId(vo.getAttrId());
            AAR.setAttrGroupId(vo.getAttrGroupId());
            return AAR;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(AARList);

    }

    /**
     *
     * @param params 分页参数
     * @param attrgroupId  寻找Attr的Group的id
     * @return
     */
    @Override
    public PageUtils getNobindAttr(Map<String, Object> params, Long attrgroupId) {
        AttrGroupEntity groupEntity = this.getById(attrgroupId);
        Long groupEntityCatelogId = groupEntity.getCatelogId();
        //attrList是所有的满足categoryId的AttrEntity;
        List<AttrEntity> attrList = attrService.list(new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getCatelogId, groupEntityCatelogId));

        //countNotAttrId是记录下来的已经被绑定的Attr
        List<Long> countNotAttrId = new ArrayList<>();
        countNotAttrId = attrList.stream().map((attr)->{
            //根据attrId去表里面查,如果为空,就是没有被关联过的;
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper =
                    new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            AttrAttrgroupRelationEntity AARentity = relationDao.selectOne(wrapper);
            if(AARentity == null){
                return attr.getAttrId();
            }
            return -1L;
        }).collect(Collectors.toList());

        List<Long> nu = new ArrayList<Long>();
        for (int i = 0; i < countNotAttrId.size(); i++) {
            if(countNotAttrId.get(i) != -1){
                nu.add(countNotAttrId.get(i));
            }
        }
        LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (!Strings.isEmpty(key)){
            wrapper.and((lambdaWrapper)->{
                lambdaWrapper.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }
        wrapper.in(AttrEntity::getAttrId, nu);

        IPage<AttrEntity> page = attrService.page(
                new Query<AttrEntity>().getPage(params),
                wrapper);

        return new PageUtils(page);


    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        // 2、查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream( ) .map(group -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group,attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());
        return collect;

    }

}