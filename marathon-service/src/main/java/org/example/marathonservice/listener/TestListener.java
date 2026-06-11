package org.example.marathonservice.listener;

import org.example.marathondal.entity.UserDO;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener {

//    @RabbitListener(queues = "work.queue")
//    public void listenWork1(String in) throws InterruptedException {
//        System.out.println("消费者1收到来自work队列的消息 : " + in);
//        Thread.sleep(20);
//    }
//
//    @RabbitListener(queues = "work.queue")
//    public void listenWork2(String in) throws InterruptedException {
//        System.err.println("消费者2 : " + in);
//        Thread.sleep(200);
//    }
//
//    @RabbitListener(queues = "fanout.queue1")
//    public void listenFanout1(String in) throws InterruptedException {
//        System.out.println("服务1收到来自fanout队列的消息 : " + in);
//    }
//
//    @RabbitListener(queues = "fanout.queue2")
//    public void listenFanout2(String in) throws InterruptedException {
//        System.err.println("服务2 : " + in);
//    }
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "direct.queue1",durable = "true"), //队列名 是否持久化
//            exchange = @Exchange(name = "test.direct",type = ExchangeTypes.DIRECT), //交换机名 类型
//            key = {"red","blue"} //路由键
//    ))
//    public void listenDirect1(String in) throws InterruptedException {
//        System.out.println("服务1 : " + in);
//    }
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "direct.queue2",durable = "true"), //队列名 是否持久化
//            exchange = @Exchange(name = "test.direct",type = ExchangeTypes.DIRECT), //交换机名 类型
//            key = {"red"} //路由键
//    ))
//    public void listenDirect2(String in) throws InterruptedException {
//        System.err.println("服务2 : " + in);
//    }
//
//    @RabbitListener(queues = "object.queue")
//    public void listenObject(UserDO in) {
//        System.out.println("用户姓名" + in.getName());
//        System.out.println("收到对象消息 : " + in);
//    }

}
