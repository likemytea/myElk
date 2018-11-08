package org.lhfe.common;

public class Constants {
	/** ICE ***********/
	public static String server_trans_protocol = "tcp";
	public static String server_transfer_port = "10110";
	public static String server_outtime = "400000";
	public static String server_support_zip = "z";
	public static String adapter_name_sc = "sct";
	public static String interface_name_myice_CommonTrans = "ttttttttt";
	/**
	 * Ice.MessageSizeMax
	 */
	public static final String ice_mess_size_max = "224000000";
	/** ICE SERVER IP address */
	public static String server_ip = "localhost";
	/** ICE SERVER port */
	public static String server_port = "10110";
	/** rabbit MQ *******************************/
	public static String RABBIT_HOSTS = "op.jts.mq01.p2c.srv";
	public static int RABBIT_PORTS = 15601;
	public static String RABBIT_USER_NA = "rabbitmq";
	public static String RABBIT_USER_PWD = "Puv9pksm4Ekg";
	/** netty-server *******************************/
	public static int NETTY_SERVER_PORTS = 8992;
	public static int NETTY_IDLESTATE_READ = 60 * 60 * 5;
	public static int NETTY_IDLESTATE_WRITE = 0;
	public static int NETTY_IDLESTATE_RW = 0;
	/** netty-client *******************************/
	public static String NETTY_CLIENT_HOSTS = "192.168.40.66";
	public static int NETTY_CLIENT_PORTS = 8992;
	/** 具体业务定制：log监控 ****************************************/
	public static String RBT_QUE_LOG_MONITOR = null;
	public static boolean RBT_TRY_CONN = true;
	/** JOB开关,使用zookeeper设的置 ***********/
	public static boolean job_switch = false;
	/*** ZOOKEEPER ****************/
	public static String ZK_HOSTS_PORTS = null;
	public static String ZK_ROOT = null;
	public static String ZK_ROOT_JOBSERVER_PATH = null;
	public static String ZK_EPH_NODE = null;
	public static int ZK_SESSION_TIMEOUT = 10000;
}