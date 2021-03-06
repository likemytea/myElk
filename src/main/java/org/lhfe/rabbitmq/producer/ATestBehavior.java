package org.lhfe.rabbitmq.producer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import org.lhfe.common.Constants;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.ConnectionFactory;

public class ATestBehavior {

	public static void main(String[] args) {
		ConnectionFactory fac = new ConnectionFactory();

		fac.setHost("192.168.110.178");
		fac.setPort(Constants.RABBIT_PORTS);
		fac.setUsername(Constants.RABBIT_USER_NA);
		fac.setPassword(Constants.RABBIT_USER_PWD);

		// 此值已经在rabbitmq底层程序中设置了，只不过每次调用客户端都
		// 要调用一下
		// Runtime.getRuntime()...所以我就把它拿出来了，显示的设置上，否则每个send都会重复调用这些方法，我们不让程序做重复的无用功，尤其是像这种频繁操作的处理。
		fac.setSharedExecutor(Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors() * 2, Executors.defaultThreadFactory()));
		// 客户端和服务端的握手时间，此时间设置的过小，则容易出现concurrentTimeOut，我默认设置500000
		fac.setConnectionTimeout(0);
		fac.setHandshakeTimeout(500000);
		MqSender.cf = fac;
		for (int i = 0; i < 1000000; i++) {

			boolean res = BasicSenderLongConn.sendAndQueInMem("behaviorLog",
					editHbaseTest(String.valueOf(i)), 1000 * 60 * 60, 1,
					Constants.RBT_TRY_CONN);
			if (!res) {
				System.out.println("向MQ insert数据异常。");
			}
		}
		System.out.println("--------finished");
		System.exit(0);
	}

	private static String editHbaseTest(String val) {
		// void org.lhfe.hbase.log.LogManager.addRowsRecord(
		// String tableName, String rowKey, String family,
		// String qualifier, String value) throws IOException
		// tblName=LOG_INFO&rowKey=1&cfFamily=MESSAGES&col=detailMess&val=runtimeException
		String[] xxx = new String[6];
		xxx[0] = "1";
		xxx[1] = "r6";
		xxx[2] = "1.11.11.1";
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String systime = df.format(new Date());
		xxx[3] = systime;
		xxx[4] = "bg001";
		xxx[5] = "2";
		String str = JSON.toJSONString(xxx, true);
		return str;
	}
}
