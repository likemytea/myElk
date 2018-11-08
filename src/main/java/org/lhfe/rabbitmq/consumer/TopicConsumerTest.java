package org.lhfe.rabbitmq.consumer;


public class TopicConsumerTest extends TopicConsumer{
	/**
	 * listener start
	 * 
	 * */
	public static void main(String[] args) {
		TopicConsumerTest ba = new TopicConsumerTest();
		String[] bindingKey = {"*.*.orange","lsdjf.sdf.*.asf"};
		try {
			ba.receive("exchangeTopic",bindingKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doWork(String message) {
	    //如果打算放弃此次消息的处理并让此消息重新归入队列，直接throw Exception即可。
		/***业务逻辑 start************/
		System.out.println("topic-a:"+message);
		/***业务逻辑   end************/
	}

}
