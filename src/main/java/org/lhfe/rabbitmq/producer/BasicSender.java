package org.lhfe.rabbitmq.producer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

/**
 * 所有例子均来源于rabbitMQ官方网站http://www.rabbitmq.com/，点击链接 “Easy to use”
 * RabbitMQ建议客户端线程之间不要共用Channel， 至少要保证共用Channel的线程发送消息必须是串行的，
 * 但是建议尽量共用Connection。
 * 
 * @author liuxingt450
 * */
public class BasicSender extends MqSender {
	/**
	 * @param queNa
	 *            queuename sended message.
	 * @return 0 success 1 IOException 2 timeoutexception
	 * @author liuxing
	 * */
	public static int send(String queNa, String message) {

		Connection connection = null;
		Channel channel = null;
		try {
			connection = cf.newConnection();
			channel = connection.createChannel();
			boolean durable = true;
			// 如果想要保证消息不丢，需要保证队列和消息都是durable
			// 对于声明了durable=true后， 这个队列就不能再被重新声明了
			// 这个队列声明的改变需要在product和consumer里边都指定才生效
			// 消息持久化声明--》MessageProperties.PERSISTENT_TEXT_PLAIN
			channel.queueDeclare(queNa, durable, false, false, null);

			channel.basicPublish("", queNa,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					message.getBytes("UTF-8"));
			return 0;
		} catch (IOException e1) {
			try {
				if (channel != null)
					channel.close();
				if (connection != null)
					connection.close();
			} catch (IOException | TimeoutException e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
			return 1;
		} catch (TimeoutException e2) {
			e2.printStackTrace();
			return 2;
		}
	}

	/**
	 * @param queNa
	 *            queuename sended message.
	 * @return 0 success 1 IOException 2 timeoutexception
	 * @author liuxing
	 * */
	public static int sendAndQueInMem(String queNa, String message) {

		Connection connection = null;
		Channel channel = null;
		try {
			connection = cf.newConnection();
			channel = connection.createChannel();
			// 如果想要保证消息不丢，需要保证队列和消息都是durable
			// 对于声明了durable=true后， 这个队列就不能再被重新声明了
			// 这个队列声明的改变需要在product和consumer里边都指定才生效
			// 消息持久化声明--》MessageProperties.PERSISTENT_TEXT_PLAIN
			channel.queueDeclare(queNa, false, false, false, null);
			channel.basicPublish("", queNa, null, message.getBytes("UTF-8"));
			return 0;
		} catch (IOException e1) {
			try {
				if (channel != null)
					channel.close();
				if (connection != null)
					connection.close();
			} catch (IOException | TimeoutException e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
			return 1;
		} catch (TimeoutException e2) {
			e2.printStackTrace();
			return 2;
		}

	}
}