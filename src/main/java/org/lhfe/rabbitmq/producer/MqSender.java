package org.lhfe.rabbitmq.producer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.rabbitmq.client.ConnectionFactory;

public abstract class MqSender {
	public static ConnectionFactory cf = null;

	public static byte[] objectToByte(java.lang.Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bo = null;
		ObjectOutputStream oo = null;
		try {
			bo = new ByteArrayOutputStream();
			oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
			bo.close();
			oo.close();
		} catch (Exception e) {
			if (bo != null) {
				try {
					bo.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (oo != null) {
				try {
					oo.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return bytes;
	}
}
