package org.lhfe.rabbitmq.producer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class DirectRoutingSender extends MqSender {
	/**
	 * @param exchange
	 *            name, routingKey, sended message.
	 * @return 0 success 1 IOException 2 timeoutexception
	 * @author liuxing
	 * */
	public static int send(String exNa, String routingKey, String message) {
		Connection connection = null;
		Channel channel = null;
		try {
			connection = cf.newConnection();
			channel = connection.createChannel();
			// true表示对exchange进行持久化,另外注意：消费者的代码里边对
			// exchangeDeclare()方法的调用中必须也指定为true，否则运行过程中会报错。
			channel.exchangeDeclare(exNa, "direct", true);
			// channel.basicPublish(EXCHANGE_NAME, severity, null,
			// message.getBytes());
			channel.basicPublish(exNa, routingKey,
					MessageProperties.PERSISTENT_TEXT_PLAIN,
					message.getBytes("UTF-8"));
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if (channel != null)
					channel.close();
				if (connection != null)
					connection.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (TimeoutException e1) {
				e1.printStackTrace();
			}
			return 1;
		} catch (TimeoutException e) {
			e.printStackTrace();
			return 2;
		}
	}
}