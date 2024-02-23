package com.xiaofei.xiaofeimall.order.listener;

import com.rabbitmq.client.Channel;
import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import com.xiaofei.xiaofeimall.order.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/23 13:45
 */
@Log4j2
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    /**
     * 监听的是从延时队列过来的数据,也是就是已经结果30分钟沉淀的数据
     *
     * @param orderEntity
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) {
        try {
            log.info("收到过期订单,准备关单");
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


    }
}
