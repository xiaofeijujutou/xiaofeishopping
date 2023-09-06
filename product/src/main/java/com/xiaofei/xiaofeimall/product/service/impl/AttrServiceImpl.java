package com.xiaofei.xiaofeimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xiaofei.xiaofeimall.product.dao.AttrAttrgroupRelationDao;
import com.xiaofei.xiaofeimall.product.dao.AttrGroupDao;
import com.xiaofei.xiaofeimall.product.dao.CategoryDao;
import com.xiaofei.xiaofeimall.product.entity.AttrAttrgroupRelationEntity;
import com.xiaofei.xiaofeimall.product.entity.AttrGroupEntity;
import com.xiaofei.xiaofeimall.product.entity.CategoryEntity;
import com.xiaofei.xiaofeimall.product.service.CategoryService;
import com.xiaofei.xiaofeimall.product.vo.AttrResponseVo;
import com.xiaofei.xiaofeimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.AttrDao;
import com.xiaofei.xiaofeimall.product.entity.AttrEntity;
import com.xiaofei.xiaofeimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao AArelationDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryService categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    /**
     * 新增一条数据
     */
    public void saveAttr(AttrVo attr) {

        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //保存基本数据
        this.save(attrEntity);
        //保存关联数据,如果是销售属性就不需要往中介表添加数据;
        if (attr.getAttrType() == 1){//==1是普通属性;
            //if里面是给中介表添加关联数据, 普通参数<=>group
            AttrAttrgroupRelationEntity AARelation = new AttrAttrgroupRelationEntity();
            if(attr.getAttrGroupId() != null){//groupId不等于null才插入
                AARelation.setAttrGroupId(attr.getAttrGroupId());//无
                AARelation.setAttrId(attrEntity.getAttrId());//有
                AArelationDao.insert(AARelation);
            }

        }


    }

    /**
     * 根据三级分类(CategoryId), 或者AttrGroup查   -> List<Attr>
     * 这里的category是用来根据三级分类来查AttrEntity, 后面不需要是因为查出来的Entity根据自己的id查name
     * @param params 分页条件不用管
     * @param categoryId
     * @param attrType Attr类型, 影响不影响价格;
     * @return
     */

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        //添加是销售属性还是基础属性的条件;
        this.addCondition(wrapper, attrType);
        String key = (String) params.get("key");
        //没选中什么时返回全部;
        if(!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        if(categoryId == 0){
            //page是某种类型的数据库所有数据
            IPage<AttrEntity> page = this.page(
                    new Query<AttrEntity>().getPage(params),
                    wrapper
            );
            //page就是查询了所有的IPage
            PageUtils pageUtils = addData(page, attrType);
            return pageUtils;
        }

        //如果是sale,就多破解一串查询条件;sale;base
        wrapper.eq("catelog_id", categoryId);
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        return  this.addData(page,attrType);
    }
    /** 根据attr类型来添加查询条件*/
    private void addCondition (QueryWrapper wrapper,String attrType){
        if ("sale".equalsIgnoreCase(attrType)){
         //销售属性是0;
            wrapper.eq("attr_type", 0);
        }
        //普通属性是1
        if ("base".equalsIgnoreCase(attrType)){
            wrapper.eq("attr_type", 1);
        }

    }
    /**
     * 传入List<AttrEntity>,装载CategoryName+GroupName
     * 但是现在改了,销售属性不和Group关联;
     * @param page
     * @return
     */
    private PageUtils addData(IPage<AttrEntity> page, String attrType){
        List<AttrEntity> records = page.getRecords();
        //把List<AttrEntity>倒出来,换成List<attrResponseVo>
        List<AttrResponseVo> returnedVo = records.stream().map((attrEntity -> {
            /** 装载CategoryName*/
            //复制数据;
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);
            //设置categoryName
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            //不为空才设置就去
            if (categoryEntity != null && !StringUtils.isEmpty(categoryEntity.getName())){
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            /** 装载GroupName
            设置AttrGroupName,销售属性不需要设置,base是基础的
            现在只有AttrId*/
            if ("base".equalsIgnoreCase(attrType)){
                //空指针异常: 创建的时候根本就没往中介表里面加数据
                LambdaQueryWrapper<AttrAttrgroupRelationEntity> lambdaWrapper = new LambdaQueryWrapper<>();
                lambdaWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrResponseVo.getAttrId());//条件
                //TODO relationEntity == null 因为根本就没有关联
                AttrAttrgroupRelationEntity relationEntity = AArelationDao.selectOne(lambdaWrapper);//上面条件来查
                if (relationEntity != null){
                    String groupName = attrGroupDao.selectById(relationEntity.getAttrGroupId()).getAttrGroupName();
                    attrResponseVo.setGroupName(groupName);
                }
            }
            return attrResponseVo;
        })).collect(Collectors.toList());
        PageUtils IPage = new PageUtils(page);
        IPage.setList(returnedVo);
        return IPage;
    }

    /**
     * 查询方法
     * 高级版本的数据回显
     */
    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResponseVo);
        //设置分组信息(包括分组id和分组name)
        AttrAttrgroupRelationEntity AAREntity = AArelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if (AAREntity != null){
            AttrGroupEntity attrGroupEntitiy = attrGroupDao.selectById(AAREntity.getAttrGroupId());
            if (attrGroupEntitiy != null){
                attrResponseVo.setGroupName(attrGroupEntitiy.getAttrGroupName());
            }
        }
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null){
            attrResponseVo.setGroupName(categoryEntity.getName());
            //获取Path已经写好了方法,直接用就行了;
            Long[] categoryPath = categoryService.getCategoryPath(categoryEntity.getCatId());
            attrResponseVo.setCatelogPath(categoryPath);
        }


        return attrResponseVo;
    }

    /**
     * 更新修改的时候,如果有就修改,没有就保存一个
     * @param attr
     */
    @Transactional
    @Override
    public void updateById(AttrVo attr) {
        //保存基本属性
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        //通过AttrEntityid去修改GroupId;保存关系表;
        //如果关联表本来就没有,就需要添加,有就修改;
        Integer count = AArelationDao.selectCount( new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));

        AttrAttrgroupRelationEntity AAREntity = new AttrAttrgroupRelationEntity();
        AAREntity.setAttrGroupId(attr.getAttrGroupId());
        AAREntity.setAttrId(attr.getAttrId());
        if (count > 0){
            AArelationDao.update(AAREntity,
                    new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
        }else {
            //新增
            AArelationDao.insert(AAREntity);
        }
    }

    /**
     * 根据AttrGroup传过来的id值,查出关联的所有数据,
     * 需要根据中介表
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper =
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId);
        List<AttrAttrgroupRelationEntity> AAR = AArelationDao.selectList(wrapper);
        List<AttrEntity> attrEntities = null;
        if (AAR != null){
            attrEntities = AAR.stream().map((aar)->{
                Long attrId = aar.getAttrId();
                AttrEntity attrEntity = this.getById(attrId);
                return attrEntity;
            }).collect(Collectors.toList());
        }
        return attrEntities;
    }

    /**
     * 所有attr里面挑出指定的可以被检索的属性
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {

        return  baseMapper.selectSearchAttrIds(attrIds);
    }
}