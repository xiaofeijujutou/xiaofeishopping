package com.xiaofei.xiaofeimall.order.config;

import com.xiaofei.xiaofeimall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 会根据小驼峰命名自动生成MQ的命名规则;
 * 一个微服务对应一个交换机的话,就要设计成topic模式;
 * 队列路由键通常为queue前面部分,在绑定关系里面设置:order.delay.queue队列的路由键通常设置为order.delay;
 * 队列和交换机一旦创建,属性就不会覆盖;
 * @Author : 小肥居居头
 * @create 2024/2/22 16:25
 */

@Component
public class MyRabbitMQConfig {
    /**
     * 交换机,topic模式
     * @Bean 容器就会自动去MQ创建交换机,队列,绑定关系;(没有就创建,有就直接使用)
     * @return
     */
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange", true,false);
    }

    /**
     * 延迟队列,只存放消息,没有消费者,消息过期了就丢到指定交换机的路由键;
     * @return 延迟队列
     */
    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        //设置消息过期时间
        arguments.put("x-message-ttl",60000L);
        //设置消息过期之后,消息丢入哪个交换机
        arguments.put("x-dead-letter-exchange" ,"order-event-exchange");
        //设置消息过期之后,消息丢入哪个路由键
        arguments.put("x-dead-letter-routing-key","order.release.order");
        return new Queue("order.delay.queue",//队列名称,
                true,   //持久化
                false, //排他队列,一般是执行临时任务或者限制队列访问权限;
                false,//自动删除
                arguments);
    }

    /**
     * 释放订单队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue",//路由键:order.release.order
                true,
                false,
                false);
    }

    /**
     * 使用常规操作绑定交换机和队列的关系
     * @return
     */
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", //绑定的目标队列
                Binding.DestinationType.QUEUE, //绑定目标类型
                "order-event-exchange", //绑定的目标交换机
                "order.delay",  //绑定关系名称
                null);
    }

    /**
     * 使用建造者来绑定交换机和队列的关系
     * 这里绑定的是交换机和释放队列的关系
     * @param orderReleaseOrderQueue 队列
     * @param orderEventExchange 交换机
     * @return
     */
    @Bean
    public Binding orderReleaseOrderBinding(Queue orderReleaseOrderQueue,
                                            Exchange orderEventExchange){
        return BindingBuilder.bind(orderReleaseOrderQueue)
                .to(orderEventExchange)
                .with("order.release.order")
                .and(null);
    }


    /**
     * 订单交换机<==>库存释放队列;
     * 作用:订单超时后发送消息给库存让库存解锁;
     * 路由键order.release.ware
     * @return
     */
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("stock.release.stock.queue", //绑定库存释放队列
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.ware",
                null);
    }



}
