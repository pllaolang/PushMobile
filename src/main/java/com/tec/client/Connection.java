package com.tec.client;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.tec.push.msg.Message;
import com.tec.push.msg.TransferMessageCodecFactory;
import com.tec.push.msg.TransferMessageDecoder;
import com.tec.push.msg.TransferMessageEncoder;
import com.tec.push.msg.TransferMsg;
import com.tec.server.ServiceDemo;

public class Connection {
	private static final Logger Log = Logger.getLogger(Connection.class);
	
	private IoSession session;
	private IoConnector connector;

	public final static int CONNSTATE_NOT_CONNECTED = 0;
	public final static int CONNSTATE_NOT_LOGIN = 1;
	public final static int CONNSTATE_WORK = 2;
	private int connState = 0;
	// public boolean isConnected = false;
	private MessageHandler messageHandler;

	public Connection(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
		connState = CONNSTATE_NOT_CONNECTED;
	}

	public void connect(String ip, int port, String user, String pwd) {
		stop();
		Log.info( ip + ":" + port);
		
		connector = new NioSocketConnector();

		// 设置链接超时时间 ms
		connector.setConnectTimeoutMillis(5000);

		KeepAliveFilter keepAliveFilter = new KeepAliveFilter(
				new KeepAliveMessageImpl(), IdleStatus.BOTH_IDLE);

		keepAliveFilter.setForwardEvent(false);
		// 2分钟发一次心跳
		keepAliveFilter.setRequestInterval(30);
		keepAliveFilter.setRequestTimeout(10);
		keepAliveFilter
				.setRequestTimeoutHandler(new KeepAliveRequestTimeoutHandler() {
					public void keepAliveRequestTimedOut(
							KeepAliveFilter filter, IoSession session)
							throws Exception {
						stop();
						Log.info("session closeed because keepAlive Timeout ( id="
										+ session.getId()
										+ " ,ip="
										+ session.getRemoteAddress() + " )");

					}

				});

		// 添加过滤器

		connector.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new TransferMessageCodecFactory(
						new TransferMessageDecoder(),
						new TransferMessageEncoder())));
		// 添加业务逻辑处理器类
		connector.getFilterChain().addLast("KeepAlive", keepAliveFilter);
		// connector.getFilterChain().addLast("executor", new ExecutorFilter());

		IoSessionConfig cfg = connector.getSessionConfig();
		// 读写通道10秒内无操作进入空闲状态
		cfg.setIdleTime(IdleStatus.BOTH_IDLE, 30);

		connector.setHandler(new myHandler(this));
		ConnectFuture future = connector
				.connect(new InetSocketAddress(ip, port));// 创建连接
		future.awaitUninterruptibly();// 等待连接创建完成
		if (future.isConnected()) {
			session = future.getSession();
			connState = CONNSTATE_NOT_LOGIN;
			// LogWriter.print("连接成功");
			Log.info("连接成功");
			login(user, pwd);
		} else {
			// LogWriter.print("连接失败");
			Log.info( "连接失败");
		}

	}

	/**
	 * 登陆
	 * 
	 * @param user
	 *            用户名
	 * @param pwd
	 *            密码
	 */
	public void login(String user, String pwd) {
		// JSONObject loginObject = new JSONObject();
		// try {
		// loginObject.put("login", user);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		Message msg = new Message(user, toMd5(pwd.getBytes()));
		TransferMsg transferMsg = new TransferMsg(msg, TransferMsg.CONNECT_REQ,
				TransferMsg.STATUS_OK);
		session.write(transferMsg);
		connState = CONNSTATE_NOT_LOGIN;
	};

	public void send(Message msg) {
		TransferMsg msg1 = new TransferMsg(msg, TransferMsg.MSG_REQ,
				TransferMsg.STATUS_NOUSE);
		msg.setId(msg1.getSequence_Id());
		session.write(msg1);
	}

	public void stop() {
		if (session != null) {
			session.close(true);
			session = null;
		}
		if (connector != null) {
			connector.dispose();
			connector = null;
		}
		connState = CONNSTATE_NOT_CONNECTED;
	}

	private byte[] toMd5(byte[] bytes) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			return algorithm.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	class KeepAliveMessageImpl implements KeepAliveMessageFactory {

		public Object getRequest(IoSession session) {
			TransferMsg heart_req = new TransferMsg(TransferMsg.HEART_REQ, 0);
			return heart_req;
		}

		public Object getResponse(IoSession session, Object request) {
			TransferMsg heart_req = (TransferMsg) request;
			TransferMsg heart_resp = new TransferMsg(TransferMsg.HEART_RESP,
					heart_req.getSequence_Id(), 0);
			return heart_resp;
		}

		public boolean isRequest(IoSession session, Object message) {
			if (message instanceof TransferMsg) {
				TransferMsg msg = (TransferMsg) message;
				if (msg.getCommand_Id() == TransferMsg.HEART_REQ) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}

		}

		public boolean isResponse(IoSession session, Object message) {
			if (message instanceof TransferMsg) {
				TransferMsg msg = (TransferMsg) message;
				if (msg.getCommand_Id() == TransferMsg.HEART_RESP) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public int getConnState() {
		return connState;
	}

	public void setConnState(int connState) {
		this.connState = connState;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
}
