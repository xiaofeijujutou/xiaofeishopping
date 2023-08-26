package com.xiaofei.xiaofeimall.product.service.impl;

import com.xiaofei.xiaofeimall.product.service.CategoryBrandRelationService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.product.dao.CategoryDao;
import com.xiaofei.xiaofeimall.product.entity.CategoryEntity;
import com.xiaofei.xiaofeimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
//    @Autowired
//    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 1.查出所有的类;
     * 2.组装成树形结构;
     * 3.查哪个表,就要用哪个dao,之前都是简单查询,不用dao,直接用service的方法;
     * 4.MP这里已经有泛型了,这里的BaseMapper就相当于CategoryDao;
     * @return 所有Category
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //能查出category表的所有数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        List<CategoryEntity> categoryTree = categoryEntities.stream().filter((oneCategory)->{
            return oneCategory.getParentCid() == 0;
            //上面return了一个oneCategory,下面又接一个map,就相当于then,返回值当成初始值;
            //由于上面==0,所以这是最上面的节点;
        }).map((parentNode)->{
            //这里也是一个循环,相当于把一级菜单都调用了一遍方法;
            List<CategoryEntity> scecondListAndthird = getChildrens(parentNode, categoryEntities);
            parentNode.setChildren(scecondListAndthird);
            return parentNode;
        }).sorted(Comparator.comparingInt(sort -> (sort.getSort() == null ? 0 : sort.getSort()))).collect(Collectors.toList());


        return categoryTree;
    }



    /**
     * 这个方法就是通过递归来将list转换成tree
     * first可以看成是firstList,因为所有的first都会进入这个方法;
     * 那么返回的就是一个first下面对应的数;
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity first, List<CategoryEntity> categoryEntities) {
        //listOfChildren就是one的子菜单的集合;
        //这里找到的只是二级菜单,我们还需要找到第三级;

        List<CategoryEntity> secondList = categoryEntities.stream().filter((oneOfAll) -> {
            //这里==和equals修改过
            return oneOfAll.getParentCid().equals(first.getCatId());
        }).map((second)->{
            List<CategoryEntity> thired = getChildrens(second, categoryEntities);
            second.setChildren(thired);
            return second;
        }).sorted((sort1,sort2)->{
            return (sort1.getSort()==null?0:sort1.getSort()) -(sort2.getSort()==null?0:sort2.getSort());
        }).collect(Collectors.toList());


        return secondList;
    }

    /**
     * 批量删除的方法:
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 批量删除之前需要检查对象是不是在其他地方被引用;
        //删除有逻辑删除和物理删除,逻辑删除就比如说改成01
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] getCategoryPath(Long thirdCategotyId) {
        List<Long> path = new ArrayList<>();
        path.add(thirdCategotyId);
        CategoryEntity categoryEntity = baseMapper.selectById(thirdCategotyId);
        Long parentCid = 0L;
        if(categoryEntity.getParentCid() != 0){
            parentCid = categoryEntity.getParentCid();
            path.add(parentCid);
            categoryEntity = baseMapper.selectById(parentCid);
        }

        if(categoryEntity.getParentCid() != 0){
            parentCid = categoryEntity.getParentCid();
            path.add(parentCid);
        }
        Collections.reverse(path);

        return path.toArray(new Long[path.size()]);
    }

    /**
     * 级联更新
     * @param category
     */
    @Override
    @Transactional
    public void updateCasede(CategoryEntity category) {
        this.updateById(category);
        if(!Strings.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }

    }
}