package org.lhfe.rabbitmq.consumer;

public class BasicConsumerATest extends BasicConsumer {
	/**
	 * listener start
	 * 
	 * */
	public static void main(String[] args) {
		BasicConsumerATest ba = new BasicConsumerATest();
		try {
			ba.receive("direct-queueNa-a", 10, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void doWork(byte[] message) {
		// 如果打算放弃此次消息的处理并让此消息重新归入队列，直接throw Exception即可。
		/*** 业务逻辑 start ************/
		System.out.println("basic-a:" + message);
		/*** 业务逻辑 end ************/
	}

}
