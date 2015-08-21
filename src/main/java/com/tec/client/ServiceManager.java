package com.tec.client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSON;
import com.tec.push.msg.Constants;
import com.tec.push.msg.Message;
import com.tec.push.msg.PushMessage;

public class ServiceManager extends Thread implements MessageHandler {
	private static final Logger Log = Logger.getLogger(ServiceManager.class);

	private boolean shutdownFlag = false;
	private Connection connection;
	private String ip = "113.240.253.188";
	private int port = 6018;
	private String user;
	private String pwd;

	public ServiceManager(String ip, int port, String user, String pwd) {
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.pwd = pwd;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.setName("后台线程");
		shutdownFlag = false;
		connection = new Connection(this);
		connection.connect(ip, port, user, pwd);
		while (!shutdownFlag) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (connection.getConnState() == Connection.CONNSTATE_NOT_CONNECTED) {
				connection.connect(ip, port, user, pwd);
			} else if (connection.getConnState() == Connection.CONNSTATE_NOT_LOGIN) {
				connection.login(user, pwd);
			}

		}
		connection.stop();
		shutdownFlag = true;

	}

	public void shutdown() {
		this.shutdownFlag = true;
		this.interrupt();
	}

	public boolean sendMsg(Message msg) {
		Log.info(msg.getMsg());
		connection.send(msg);
		return true;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("log4j.properties");
		ServiceManager serviceDemo = new ServiceManager("121.15.15.190", 7788,
				"cody", "tecblazer");
		serviceDemo.start();

	}
	
	

	public void messageReceived(Message message) {
		// TODO Auto-generated method stub

		Log.info(message.getMsg());
	}

	public void messageResp(int respId) {
		// TODO Auto-generated method stub

	}

	public void messageSent(int messageId) {
		// TODO Auto-generated method stub

	}

}
