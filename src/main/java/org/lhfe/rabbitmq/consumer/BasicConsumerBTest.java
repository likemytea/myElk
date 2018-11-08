package org.lhfe.rabbitmq.consumer;

import org.lhfe.common.Constants;
import org.lhfe.rabbitmq.producer.PopObj;

import com.rabbitmq.client.ConnectionFactory;

public class BasicConsumerBTest extends BasicConsumer {
	/**
	 * listener start
	 * 
	 * */
	public static void main(String[] args) {
		BasicConsumerBTest ba = new BasicConsumerBTest();

		ConnectionFactory fc = new ConnectionFactory();
		// auto reconnect
		fc.setAutomaticRecoveryEnabled(true);
		fc.setHost(Constants.RABBIT_HOSTS);
		fc.setPort(Constants.RABBIT_PORTS);
		fc.setUsername(Constants.RABBIT_USER_NA);
		fc.setPassword(Constants.RABBIT_USER_PWD);
		BasicConsumer.cf = fc;
		try {
			// ba.receive("basicQueue1");
			// ba.receiveAndQueInMem("memqueue-noD");
			ba.receive("sendUserRedpaketMessageToUser", 10, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void doWork(byte[] message) throws Exception {
		// 如果打算放弃此次消息的处理并让此消息重新归入队列，直接throw Exception即可。
		/*** 业务逻辑 start ************/
		try {
			PopObj obj = (PopObj) ByteToObject(message);
			if (obj.getMp() != null) {
				System.out.println(obj.getMp().get("你好") + obj.getAnnualRate());
			} else {
				System.out.println(obj.getAnnualRate());

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new Exception();
		/*** 业务逻辑 end ************/
	}
}
