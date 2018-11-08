package org.lhfe.rabbitmq.producer;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


public class FanoutSender extends MqSender{
    public static void send(String exName,String message) throws IOException, TimeoutException{
        Connection connection = cf.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exName, "fanout");
        //EXCHANGE_NAME:-->messages are routed to the queue with
        // the name specified by routingKey, if it exists.
        channel.basicPublish(exName, "", null, message.getBytes("UTF-8"));
        channel.close();
        connection.close();
    
    }
}