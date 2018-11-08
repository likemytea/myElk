package org.lhfe.rabbitmq.consumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.rabbitmq.client.ConnectionFactory;

public abstract class MqConsumer {
	public static ConnectionFactory cf = null;

	protected static Object ByteToObject(byte[] bytes) {
		Object obj = null;
		ByteArrayInputStream bi = null;
		ObjectInputStream oi = null;
		try {
			// bytearray to object
			bi = new ByteArrayInputStream(bytes);
			oi = new ObjectInputStream(bi);
			obj = oi.readObject();
			bi.close();
			oi.close();
		} catch (Exception e) {
			if (bi != null) {
				try {
					bi.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (oi != null) {
				try {
					oi.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return obj;
	}
}
