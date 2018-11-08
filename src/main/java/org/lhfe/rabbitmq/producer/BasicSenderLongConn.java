package org.lhfe.rabbitmq.producer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.lhfe.utils.ThreadPool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

/**
 * 所有例子均来源于rabbitMQ官方网站http://www.rabbitmq.com/，点击链接 “Easy to use”
 * RabbitMQ建议客户端线程之间不要共用Channel， 至少要保证共用Channel的线程发送消息必须是串行的，
 * 但是建议尽量共用Connection。
 * 
 * @author liuxing 2016.5.9
 * */
public class BasicSenderLongConn extends MqSender {
	private static Logger log = Logger.getLogger(BasicSenderLongConn.class);
	static boolean isStarted = false;
	static Connection connection;
	/**
	 * 累计出现三次异常，则api返回false，当距离首次出现异常的时间超过一小时，则累计的异常次数清零
	 * 
	 * */
	static int cumulativeCount = 0;
	static long disconnectTime = 0;
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public static void initialize() {
		if (lock.writeLock().tryLock()) {
			try {
				if (connection != null) {
					return;
				}
				connection = cf.newConnection();
				cumulativeCount = 0;
				disconnectTime = 0;
			} catch (Exception e) {
				cumulativeCount++;
				if (cumulativeCount == 1) {
					disconnectTime = System.currentTimeMillis();
				}
				connection = null;
				log.error(e.getMessage());
			} finally {
				lock.writeLock().unlock();
			}
		}

	}

	/**
	 * 服务状态诊断函数
	 * 
	 * @param crashCounter表示一段时间内累计出现异常次数的允许值
	 * @param setCrashTime表示距离首次出现异常的时间的允许值
	 * @return 如果状态为健康，返回true，否则返回false。
	 * 
	 * */
	private static boolean ifServerHealthy(int crashCounter, long setCrashTime) {
		if (lock.readLock().tryLock()) {
			try {
				// 异常累计次数大于异常次数的允许值
				if (cumulativeCount >= crashCounter) {
					// 如果已经过了这个阶段时间，则异常累计次数归零，即：重新对异常计数
					if (System.currentTimeMillis() - disconnectTime > setCrashTime) {
						cumulativeCount = 0;
					}
					return false;
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			} finally {
				lock.readLock().unlock();
			}
		} else {
			// 如果有锁存在，说明服务正在尝试重连，所以返回异常。
			return false;
		}
		return true;
	}

	/**
	 * queue sotred in memory only
	 * 累计出现crashCounter次异常，则api返回false，当距离首次出现异常的时间超过setCrashTime，则累计的异常次数清零
	 * 
	 * @param 1、队列名2、消息3、时间4、次数5、是否重连
	 * @author liuxing 2016.5.9
	 * */
	public static boolean sendAndQueInMem(String queNa, String message,
			boolean tryToConn) {
		return sendAndQueInMem(queNa, message, 1000 * 60 * 10, 2, tryToConn);
	}

	/**
	 * @param 1、队列名 2、消息 3、消息时间段 4、允许失败的次数 5、是否重连
	 * @return 发送结果-boolean
	 * @author liuxing
	 * */
	public static boolean send(String queNa, byte[] message, boolean tryToConn) {
		return send(queNa, message, 1000 * 60 * 10, 2, tryToConn);
	}

	/**
	 * @param 队列名
	 *            消息时间段 允许失败的次数 是否重连
	 * @return 1、-1为网络连接类型异常2、 其它为消息个数
	 * @author liuxing
	 * */
	public static int getMessageCounts(String queNa, boolean tryToConn) {
		return getMessageCounts(queNa, 1000 * 60 * 10, 2, tryToConn);
	}

	/**
	 * @param 1队列名 2消息 3消息时间段 4允许失败的次数 5是否重连 6是否失败重新发消息
	 * @return 发送结果-boolean
	 * @author liuxing
	 * */
	public static boolean send(String queNa, byte[] message, long setCrashTime,
			int crashCounter, boolean tryToConn, boolean ifFailedResend) {
		if (ifFailedResend) {
			if (!send(queNa, message, setCrashTime, crashCounter, tryToConn)) {
				// send返回的false含义为连接异常
				try {
					TimeUnit.MILLISECONDS.sleep(1 * 1000L);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				return send(queNa, message, setCrashTime, crashCounter,
						tryToConn, ifFailedResend);
			} else {
				return true;
			}

		} else {
			return send(queNa, message, setCrashTime, crashCounter, tryToConn);
		}

	}

	/**
	 * @param 1、队列名 2、消息 3、消息时间段 4、允许失败的次数 5、是否重连
	 * @return 发送结果-boolean
	 * @author liuxing
	 * */
	public static boolean send(String queNa, byte[] message, long setCrashTime,
			int crashCounter, boolean tryToConn) {
		// 健康诊断
		if (!ifServerHealthy(crashCounter, setCrashTime)) {
			// 如果不健康，则返回false
			return false;
		}
		Channel channel = null;
		try {
			if (connection == null && tryToConn) {
				initialize();
			}
			channel = connection.createChannel();
			// 如果想要保证消息不丢，需要保证队列和消息都是durable
			// 对于声明了durable=true后， 这个队列就不能再被重新声明了
			// 这个队列声明的改变需要在product和consumer里边都指定才生效
			// 消息持久化声明--》MessageProperties.PERSISTENT_TEXT_PLAIN
			channel.queueDeclare(queNa, true, false, false, null);
			channel.basicPublish("", queNa,
					MessageProperties.PERSISTENT_TEXT_PLAIN, message);
			channel.close();
			return true;
		} catch (Exception e) {
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e2) {
					log.error(e2.getMessage());
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e1) {
					log.error(e1.getMessage());
				} finally {
					connection = null;
				}
			}
			log.error(e.getMessage());
			return false;
		}
	}

	/**
	 * @param 队列名
	 *            消息时间段 允许失败的次数 是否重连
	 * @return 1、-1为异常2、 其它为消息个数
	 * @author liuxing
	 * */
	public static int getMessageCounts(String queNa, long setCrashTime,
			int crashCounter, boolean tryToConn) {
		int counts = 0;
		// 健康诊断
		if (!ifServerHealthy(crashCounter, setCrashTime)) {
			// 如果不健康，则返回false
			return -1;
		}
		Channel channel = null;
		try {
			if (connection == null && tryToConn) {
				initialize();
			}
			channel = connection.createChannel();
			counts = channel.queueDeclarePassive(queNa).getMessageCount();
			channel.close();
			return counts;
		} catch (Exception e) {
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e2) {
					log.error(e2.getMessage());
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e1) {
					log.error(e1.getMessage());
				} finally {
					connection = null;
				}
			}
			log.error(e.getMessage());
			return -1;
		}
	}

	/**
	 * queue sotred in memory only
	 * 累计出现crashCounter次异常，则api返回false，当距离首次出现异常的时间超过setCrashTime，则累计的异常次数清零
	 * 
	 * @param 1、队列名2、消息3、时间4、次数5、是否重连
	 * @author liuxing 2016.5.9
	 * */
	public static boolean sendAndQueInMem(String queNa, String message,
			long setCrashTime, int crashCounter, boolean tryToConn) {
		// 健康诊断
		if (!ifServerHealthy(crashCounter, setCrashTime)) {
			// 如果不健康，则返回false
			return false;
		}
		Channel channel = null;
		try {
			if (connection == null && tryToConn) {
				initialize();
			}
			channel = connection.createChannel();
			// 如果想要保证消息不丢，需要保证队列和消息都是durable
			// 对于声明了durable=true后， 这个队列就不能再被重新声明了
			// 这个队列声明的改变需要在product和consumer里边都指定才生效
			// 消息持久化声明--》MessageProperties.PERSISTENT_TEXT_PLAIN
			channel.queueDeclare(queNa, false, false, false, null);
			channel.basicPublish("", queNa, null, message.getBytes("UTF-8"));
			channel.close();
			return true;
		} catch (Exception e) {
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e2) {
					log.error(e2.getMessage());
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e1) {
					log.error(e1.getMessage());
				} finally {
					connection = null;
				}
			}
			log.error(e.getMessage());
			return false;
		}
	}

	public static void closeConn() {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			} finally {
				connection = null;
			}
		}
	}

	/**
	 * 此方法为准备废弃的local策略。2016.05.31 把消息先存储到本地缓存que里边，启用单独线程消费这个que，发送到rabbitmq。
	 * 强烈不建议使用此方案，因为测试发现：线程轮询que时，特别慢。
	 * 
	 * @author liuxing 2016.5.10
	 * */
	public static boolean sendToLocalQue(String queNa, String message) {
		StringBuffer sb = new StringBuffer(queNa);
		sb.append("&");
		sb.append(message);
		try {
			LocalQueRunner._messQue.add(sb.toString());
			return true;
		} catch (NullPointerException ex) {
			LocalQueRunner._messQue = new ConcurrentLinkedQueue<String>();
			LocalQueRunner._messQue.add(sb.toString());
			lock.writeLock().lock();
			try {
				if (!isStarted) {
					LocalQueRunner x = new LocalQueRunner();
					ThreadPool.geteService().execute(x);
					isStarted = true;
				}
			} catch (Exception m) {
				log.error(m.getMessage());
			} finally {
				lock.writeLock().unlock();
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
	}

	/**
	 * 监控队列中消息内容是否消费完毕
	 * 
	 * */
	public static boolean ifQueConsumerEnd(String queNa) {
		for (;;) {
			int mc = getMessageCounts(queNa, true);
			// -1为网络连接类异常，出现类似异常继续定时轮询
			if (mc == -1) {
				try {
					TimeUnit.MILLISECONDS.sleep(3 * 1000L);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				continue;
			}

			if (mc != 0) {
				try {
					if (mc > 100000) {
						TimeUnit.MILLISECONDS.sleep(60 * 1000L);
					} else if (mc > 10000) {
						TimeUnit.MILLISECONDS.sleep(30 * 1000L);
					} else if (mc > 1000) {
						TimeUnit.MILLISECONDS.sleep(10 * 1000L);
					} else if (mc > 400) {
						TimeUnit.MILLISECONDS.sleep(8 * 1000L);
					} else if (mc > 200) {
						TimeUnit.MILLISECONDS.sleep(6 * 1000L);
					} else if (mc > 50) {
						TimeUnit.MILLISECONDS.sleep(3 * 1000L);
					} else if (mc <= 50 && mc > 0) {
						TimeUnit.MILLISECONDS.sleep(2 * 1000L);
					} else {
						System.out.println("ERROR-fatal exception");
						System.exit(0);
					}

				} catch (InterruptedException e) {
					log.error(e.getMessage());
					continue;
				}
			} else {
				break;
			}
		}
		return true;
	}
}