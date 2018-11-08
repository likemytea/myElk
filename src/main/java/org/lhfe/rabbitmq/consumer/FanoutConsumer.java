package org.lhfe.rabbitmq.consumer;


import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class FanoutConsumer extends MqConsumer{
  /**
   * @see the queue is default and temp queue, it's not durable
   * received message is not reliable to be reliabled
   * @param exchange name
   * @author liuxing 20151223
   * */	
  public  void receive(String exNa) throws Exception {
    Connection connection = cf.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(exNa, "fanout");
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, exNa, "");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
    	  doWork(new String(body, "UTF-8"));
      }
    };
    channel.basicConsume(queueName, true, consumer);
  }
  public abstract void doWork(String message);
}