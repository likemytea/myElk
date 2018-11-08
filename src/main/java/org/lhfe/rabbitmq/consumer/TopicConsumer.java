package org.lhfe.rabbitmq.consumer;


import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class TopicConsumer extends MqConsumer{
  /**
   * temp queue used 
   * @param exchange name , binding key
   * @author liuxing 20151223
   * */
  public void receive(String exNa,String[] bk) throws Exception {
    Connection connection = cf.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(exNa, "topic");
    String queueName = channel.queueDeclare().getQueue();

    //String[] bk = {"*.orange.*","*.*.orange","orange.#"};
    //String[] bk = {"*.*.orange"};
    for (String bindingKey : bk) {
      channel.queueBind(queueName, exNa, bindingKey);
    }
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