package org.lhfe.rabbitmq.producer;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class TopicSender extends MqSender{
    public static void send(String exNa,String routingKey,String message) throws IOException, TimeoutException{
        Connection connection = cf.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exNa, "topic");
        channel.basicPublish(exNa, routingKey, null, message.getBytes("UTF-8"));
        connection.close();
    }
}