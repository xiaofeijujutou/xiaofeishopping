package com.xiaofei.xiaofeimall.ware.dao;

import com.xiaofei.xiaofeimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品库存
 * 
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:52:39
 */
@Mapper()
@Repository
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);

    /**
     * 查询这个商品在哪个仓库有库存
     * @param skuId 商品id
     * @return 有库存的仓库id
     */
    List<Long> getWareIdsWhereHasSkuStock(@Param("skuId") Long skuId);

    /**
     * 锁库存
     * @param skuId 商品id
     * @param wareId 仓库id
     * @param num 商品数量
     * @return
     */
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    /**
     * 解锁库存
     * @param skuId 商品id
     * @param wareId 仓库id
     * @param skuNum 商品数量
     * @return
     */
    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
