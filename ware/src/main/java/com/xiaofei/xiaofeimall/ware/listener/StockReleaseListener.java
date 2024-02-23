package com.xiaofei.xiaofeimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.xiaofei.common.vo.mq.OrderEntityTo;
import com.xiaofei.common.vo.mq.StockLockedTo;
import com.xiaofei.xiaofeimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/23 19:32
 */

@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
@Service
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 1、库存自动解锁
     *  下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     *
     *  2、订单失败
     *      库存锁定失败
     *
     *   只要解锁库存的消息失败，一定要告诉服务解锁失败
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) {
        try {
            //解锁库存
            wareSkuService.stockLockedRelease(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 接收订单关闭的解锁库存的消息,一个队列可以接收多种类型的数据;
     * @param orderEntityTo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderEntityTo orderEntityTo, Message message, Channel channel){

        log.info("******收到订单关闭，准备解锁库存的信息******");
        try {
            wareSkuService.unlockStock(orderEntityTo);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}
