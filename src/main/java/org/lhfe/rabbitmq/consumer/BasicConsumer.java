package org.lhfe.rabbitmq.consumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 说明： channel.basicQos(1--prefetchCount); 设置为1，意味着该消费者每次只接收
 * 一条消息，换句话说：也就是在当前消息ack之前，不会从队列接收新消息
 * 
 * @author liuxing 2015.12.22
 * */
public abstract class BasicConsumer extends MqConsumer {
	/**
	 * @param 1、队列名 2、预取数 3、队列是否为镜像集群队列
	 * @param prefetchCount
	 *            在channel.basicReject的requeue为true的时候。如
	 *            果prefetchCount这个值设的过小，假设为m，当server的que中有n个consumer不能处理的消
	 *            息n>=m的时候，那这个consumer就会循环的消费这些消息，而不会处理server的que中的其它消息了，
	 *            除非有其它的consumer能处理这些消息，并且此consumer有充裕的时间来监听并获取server的que中的消息 no
	 *            ack
	 * */
	public void receive(String queNa, int prefetchCount, boolean mirrorCluster)
			throws Exception {
		final Connection connection = cf.newConnection();
		final Channel channel = connection.createChannel();
		if (mirrorCluster) {
			// 镜像集群模式
			Map<String, Object> poc = new HashMap<String, Object>();
			poc.put("x-ha-policy", "all");
			channel.queueDeclare(queNa, true, false, false, poc);
		} else {
			channel.queueDeclare(queNa, true, false, false, null);
		}
		// channel.basicQos(1)参数为1，这样RabbitMQ就会使得每个Consumer在同一个时间点最多处
		// 理一个Message。换句话说，在接收到该Consumer的
		// ack前，他它不会将新的Message分发给它,它和是否有回执（ack）没有必然联系。
		// 这个basicQos（x）实际含义是consumer一次请求中从broker的某个que获
		// 取消息的数量，获取x个就对x个加锁，其它的consumer不会获取到这x个消息。
		channel.basicQos(prefetchCount);
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				try {
					doWork(body);
					// channel.basicAck和后面的channel.basicConsume有直接联系
					channel.basicAck(envelope.getDeliveryTag(), false);
				} catch (Exception e) {
					e.printStackTrace();
					// 失败处理，bool型数据表示是否重新投入队列
					channel.basicReject(envelope.getDeliveryTag(), true);
				}
			}
		};
		// 如果第二个参数为false，表示接收到明确的确认才做ack
		// 如果第二个参数为true，表示接收消息后就自动ack
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

	/**
	 * @param queue
	 *            stored in memory only
	 * @param prefetchCount
	 *            在channel.basicReject的requeue为true的时候。如
	 *            果prefetchCount这个值设的过小，假设为m，当server的que中有n个consumer不能处理的消
	 *            息n>=m的时候，那这个consumer就会循环的消费这些消息，而不会处理server的que中的其它消息了，
	 *            除非有其它的consumer能处理这些消息，并且此consumer有充裕的时间来监听并获取server的que中的消息 no
	 *            ack
	 * */
	public void receiveAndQueInMem(String queNa, int prefetchCount)
			throws Exception {
		final Connection connection = cf.newConnection();
		final Channel channel = connection.createChannel();
		channel.queueDeclare(queNa, false, false, false, null);
		channel.basicQos(prefetchCount);
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				try {
					doWork(body);
					channel.basicAck(envelope.getDeliveryTag(), false);
				} catch (Exception e) {
					e.printStackTrace();
					// 失败处理，bool型数据表示是否重新投入队列
					channel.basicReject(envelope.getDeliveryTag(), true);
				}
			}
		};
		channel.basicConsume(queNa, false, consumer);
	}

	/**
	 * @param queue
	 *            stored in memory only no ack
	 * 
	 * */
	public void receiveAndQueInMemNoACK(String queNa, int prefetchCount)
			throws Exception {
		final Connection connection = cf.newConnection();
		final Channel channel = connection.createChannel();
		channel.queueDeclare(queNa, false, false, false, null);
		channel.basicQos(prefetchCount);
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				try {
					doWork(body);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		channel.basicConsume(queNa, true, consumer);
	}

	public abstract void doWork(byte[] message) throws Exception;
}