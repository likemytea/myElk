package org.lhfe.rabbitmq.consumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * @author liuxing 2016.11.11
 * 
 * */
public abstract class DirectRoutingConsumer extends MqConsumer {
	/**
	 * @param 1、exchangename 2、queuename3、bindingkey array 4、预取数5、队列是否为镜像集群队列
	 * @author liuxing 20151222
	 * @throws TimeoutException
	 * @throws IOException
	 * 
	 * */
	public void receive(String exNa, String queNa, String[] bindingKey,
			int prefetch, boolean mirrorCluster) throws IOException,
			TimeoutException {
		final Connection connection = cf.newConnection();
		final Channel channel = connection.createChannel();

		channel.exchangeDeclare(exNa, "direct", true);
		// true 表示队列持久化
		// channel.queueDeclare(Constants.TASK_QUEUE_NAME_1, true, false, false,
		// null);
		if (mirrorCluster) {
			// 镜像集群模式
			Map<String, Object> poc = new HashMap<String, Object>();
			poc.put("x-ha-policy", "all");
			channel.queueDeclare(queNa, true, false, false, poc);
		} else {
			channel.queueDeclare(queNa, true, false, false, null);
		}

		// 获取随机队列
		// String queueName = channel.queueDeclare().getQueue();

		// 设置 bingding-key
		for (String severity : bindingKey) {
			channel.queueBind(queNa, exNa, severity);
		}
		channel.basicQos(prefetch);
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				try {
					doWork(new String(body, "UTF-8"));
					// channel.basicAck和后面的channel.basicConsume有直接联系
					channel.basicAck(envelope.getDeliveryTag(), false);
				} catch (Exception e) {
					e.printStackTrace();
					// 失败处理，bool型数据表示是否重新投入队列
					channel.basicReject(envelope.getDeliveryTag(), true);
				}
			}
		};
		// channel.basicConsume(Constants.TASK_QUEUE_NAME_1, false, consumer);
		if (mirrorCluster) {
			// 集群镜像模式
			Map<String, Object> policys = new HashMap<String, Object>();
			// 从mirrored
			// queue消费message的clients可能想要知道它们从中消费message的queue是否发生了fail
			// over。
			// 当mirrored queue发生了fail
			// over后，关于messages已被发送到哪个consumer的信息将丢失，因此所有未确认
			// 的messages会被设置redelivered
			// flag后重新发送。Consumers可能想要知道是否发生了这个过程。Consumer
			// 可以通过将x-cancel-on-ha-failover 设置为true来达到此目的。这样一来在failover发生时消费将取消，
			// consumer cancellation notification会被发出。接下来由consumer决定是否重新
			policys.put("x-cancel-on-ha-failover", true);
			channel.basicConsume(queNa, false, policys, consumer);
		} else {
			channel.basicConsume(queNa, false, consumer);
		}

	}

	public abstract void doWork(String message) throws Exception;
}