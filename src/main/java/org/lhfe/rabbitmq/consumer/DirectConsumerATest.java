package org.lhfe.rabbitmq.consumer;

public class DirectConsumerATest extends DirectRoutingConsumer {
	/**
	 * listener start
	 * 
	 * */
	public static void main(String[] args) {
		DirectConsumerATest ba = new DirectConsumerATest();
		String[] bindingKey = { "routingKeyA", "bindingkey-b", "bindingkey-c" };
		try {
			ba.receive("exchange1", "direct-queueNa-a", bindingKey, 5, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void doWork(String message) {
		// 如果打算放弃此次消息的处理并让此消息重新归入队列，直接throw Exception即可。
		/*** 业务逻辑 start ************/
		if (!message.contains("hahaha")) {
			System.out.println("============");
		}
		System.out.println("directrouting-a:" + message);
		/*** 业务逻辑 end ************/
	}

}
