package com.xiaofei.xiaofeimall.ware.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.rabbitmq.client.Channel;
import com.xiaofei.common.constant.OrderConstant;
import com.xiaofei.common.exception.BizCodeEnum;
import com.xiaofei.common.utils.R;
import com.xiaofei.common.vo.mq.StockDetailTo;
import com.xiaofei.common.vo.mq.StockLockedTo;
import com.xiaofei.xiaofeimall.ware.entity.WareOrderTaskDetailEntity;
import com.xiaofei.xiaofeimall.ware.entity.WareOrderTaskEntity;
import com.xiaofei.xiaofeimall.ware.feign.OrderFeignService;
import com.xiaofei.xiaofeimall.ware.feign.ProductFeignService;
import com.xiaofei.xiaofeimall.ware.service.WareOrderTaskDetailService;
import com.xiaofei.xiaofeimall.ware.service.WareOrderTaskService;
import com.xiaofei.xiaofeimall.ware.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.common.utils.PageUtils;
import com.xiaofei.common.utils.Query;

import com.xiaofei.xiaofeimall.ware.dao.WareSkuDao;
import com.xiaofei.xiaofeimall.ware.entity.WareSkuEntity;
import com.xiaofei.xiaofeimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    OrderFeignService orderFeignService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 解锁
     * 1、查询数据库关于这个订单锁定库存信息
     *   没有:数据库没有但是MQ有,说明事务回滚了,锁了的库存也自动解锁了;
     *   有：证明库存锁定成功了(需要看情况解锁)
     *      解锁：订单状况
     *          1、没有这个订单，必须解锁库存(说明order先调用锁库存,后面继续调用的时候出异常了,需要解锁)
     *          2、有这个订单，不一定解锁库存
     *              订单状态：已取消：解锁库存
     *                      已支付：不能解锁库存(表示商品已经卖了,不能解锁);
     * @param to
     * @param message
     */
    @Override
    public void stockLockedRelease(StockLockedTo to, Message message, Channel channel){
        log.info("收到解锁库存消息");
        Long lockDetailId = to.getDetailTo().getId();
        WareOrderTaskDetailEntity lockDetailEntity = wareOrderTaskDetailService.getById(lockDetailId);
        if (ObjectUtils.isEmpty(lockDetailEntity)){
            log.info("商品skuId:" +to.getDetailTo().getSkuId() + "事务回滚了,锁了的库存也自动解锁了,无需解锁");
            return;
        }
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(to.getId());
        String orderSn = taskEntity.getOrderSn();
        R orderEntityResult = orderFeignService.getOrderStatus(orderSn);
        try{
            if (orderEntityResult.getCode() == BizCodeEnum.SUCCESS_CODE.getCode()){
                OrderEntityVo orderEntityVo = orderEntityResult.getData(OrderEntityVo.class);
                //订单不存在 || 用户取消订单,解锁
                if (orderEntityVo == null ||
                        orderEntityVo.getStatus() == OrderConstant.OrderStatusEnum.CANCLED.getCode()) {
                    unLockStock(to.getDetailTo().getSkuId(), to.getDetailTo().getWareId(), to.getDetailTo().getSkuNum(), lockDetailId);
                    //手动ack;
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }else {
                //订单没有取消,可能支付了,不回滚,ack
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }catch (IOException e){
            log.error("解锁库存出现异常,重新放回数据");
            e.printStackTrace();
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * 商品解锁方法
     * @param skuId
     * @param wareId
     * @param skuNum
     * @param lockDetailId
     */
    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long lockDetailId) {
        wareSkuDao.unLockStock(skuId, wareId, skuNum);
        log.info("商品skuId:" + skuId +
                "\t数量:" + skuNum +
                "\t仓库id:"+ wareId +
                "解锁成功");
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 增加商品库存的方法
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId)
                .eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                //远程调用
                R rpcResultInfo = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) rpcResultInfo.get("skuInfo");
                if(rpcResultInfo.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    /**
     * 查询是否有库存
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            //查询当前总库存量
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            //count==null?false:count>0 = count==null?false:&&count>0
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 锁定指定id的库存
     * @param vo 订单号 + 用户下单的所有商品详情;
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //保存用户下单商品的详情,库存工作单详情信息(关联用户下单商品的详情),方便追溯
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //1、按照下单的收货地址，找到一个就近仓库，锁定库存
        //2、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> userLocksProduct = vo.getLocks();
        List<SkuWareHasStockVo> userSubmitAndSatisfyWare = userLocksProduct.stream().map((lockedItem) -> {
                //用户发来的数据,一个一个去查有没有满足可以发货的库存id,然后保存进去
                SkuWareHasStockVo stock = new SkuWareHasStockVo();
                Long skuId = lockedItem.getSkuId();
                stock.setSkuId(skuId);
                stock.setNum(lockedItem.getCount());
                //查询这个商品在哪个仓库有库存
                List<Long> wareIdList = wareSkuDao.getWareIdsWhereHasSkuStock(skuId);
                stock.setWareId(wareIdList);
                return stock;
            }).collect(Collectors.toList());
        //2、锁定库存
        for (SkuWareHasStockVo oneProductSubmitDetail : userSubmitAndSatisfyWare) {
            boolean skuStocked = false;
            Long skuId = oneProductSubmitDetail.getSkuId();
            List<Long> wareIds = oneProductSubmitDetail.getWareId();
            if (CollectionUtils.isEmpty(wareIds)) {
                //没有任何仓库有这个商品的库存直接报错
                throw new RuntimeException("没有任何仓库有这个商品的库存" + skuId);
            }
            //达到锁库存的条件接下来==>
            //1、当某一个商品满足锁定条件,就往MQ发送消息,信息内容为:当前商品id、商品锁定了几件、整个订单的工作单;
            //2、锁定失败,这个方法加了事务Transactional,所以前面保存的工作单信息在MySQL都会回滚。
            //3、发送出去的消息，到时间要解锁库存，由于事务回滚,在数据库查不到指定的id，所有就不用解锁
            //4、MQ的解锁逻辑:到时间之后,订单没解锁就解锁,订单没了,说明要么没下单成功,要么付款了等,不用解锁
            for (Long wareId : wareIds) {
                //执行锁定库存方法,锁定成功就返回1，失败就返回0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,oneProductSubmitDetail.getNum());
                if (count == 1) {
                    WareOrderTaskDetailEntity productLockDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("")
                            .skuNum(oneProductSubmitDetail.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1)
                            .build();
                    wareOrderTaskDetailService.save(productLockDetailEntity);
                    //执行1=>保存成功代表锁定成功,发送消息给MQ
                    StockLockedTo lockedMQTo = new StockLockedTo();
                    lockedMQTo.setId(wareOrderTaskEntity.getId());
                    //相当于锁库存实体类
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(productLockDetailEntity, stockDetailTo);
                    lockedMQTo.setDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedMQTo);
                    log.info("商品skuId:" + stockDetailTo.getSkuId() +
                            "\t数量:" + stockDetailTo.getSkuNum() +
                            "\t仓库id:"+ stockDetailTo.getWareId() +
                            "锁定,发送消息");
                    skuStocked = true;
//                    WareOrderTaskDetailEntity productLockDetailEntity = WareOrderTaskDetailEntity.builder()
//                            .skuId(skuId)
//                            .skuName("")
//                            .skuNum(oneProductSubmitDetail.getNum())
//                            .taskId(wareOrderTaskEntity.getId())
//                            .build();
//                    wareOrderTaskDetailService.save(productLockDetailEntity);
//                    //TODO 告诉MQ库存锁定成功
//                    StockLockedTo lockedTo = new StockLockedTo();
//                    lockedTo.setId(wareOrderTaskEntity.getId());
//                    StockDetailTo detailTo = new StockDetailTo();
//                    BeanUtils.copyProperties(productLockDetailEntity,detailTo);
//                    lockedTo.setDetailTo(detailTo);
//                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);
                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }
            }

            if (skuStocked == false) {
                //当前商品所有仓库都没有锁住
                throw new RuntimeException("没有任何仓库有这个商品的库存" + skuId);
            }
        }
        //3、肯定全部都是锁定成功的
        return true;
    }

}