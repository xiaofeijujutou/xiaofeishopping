package com.xiaofei.xiaofeimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbitmq.client.Channel;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.vo.mq.StockLockedTo;
import com.xiaofei.xiaofeimall.ware.entity.WareSkuEntity;
import com.xiaofei.xiaofeimall.ware.vo.LockStockResultVo;
import com.xiaofei.xiaofeimall.ware.vo.SkuHasStockVo;
import com.xiaofei.xiaofeimall.ware.vo.WareSkuLockVo;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xiaofei
 * @email xiaofei@gmail.com
 * @date 2023-08-17 19:52:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void stockLockedRelease(StockLockedTo to, Message message, Channel channel);
}

