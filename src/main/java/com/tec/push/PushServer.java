package com.tec.push;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.tec.push.msg.Message;
import com.tec.push.msg.TransferMessageCodecFactory;
import com.tec.push.msg.TransferMessageDecoder;
import com.tec.push.msg.TransferMessageEncoder;
import com.tec.push.msg.TransferMsg;

/**
 * 
 * 
 * <p>
 * 说明:push服务器
 * </p>
 * <p>
 * 创建日期 2015年8月20日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class PushServer {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(PushServer.class);

	private IoAcceptor acceptor;
	// 下面2个map数据一致，一个从socket会话找用户数据，一个按用户名（电话号码）找用户数据
	private ConcurrentMap<Long, UserInfo> userConnectMap;
	private ConcurrentMap<String, UserInfo> userPhoneMap;

	private boolean isRuning = false;
	private MessageHandler messageHandler;

	public void start(int port) {

		try {
			userConnectMap = new ConcurrentHashMap<Long, UserInfo>();
			userPhoneMap = new ConcurrentHashMap<String, UserInfo>();

			acceptor = new NioSocketAcceptor();

			PushHandler handler = new PushHandler(this);

			KeepAliveFilter keepAliveFilter = new KeepAliveFilter(
					new KeepAliveMessageImpl(), IdleStatus.BOTH_IDLE);

			keepAliveFilter.setForwardEvent(false);
			keepAliveFilter.setRequestInterval(40);
			keepAliveFilter.setRequestTimeout(10);
			keepAliveFilter
					.setRequestTimeoutHandler(new KeepAliveRequestTimeoutHandler() {
						public void keepAliveRequestTimedOut(
								KeepAliveFilter filter, IoSession session)
								throws Exception {
							closeSession(session);
							logger.warn("session closeed because keepAlive Timeout ( id="
									+ session.getId()
									+ " ,ip="
									+ session.getRemoteAddress() + " )");

						}

					});

			acceptor.getFilterChain().addLast(
					"codec",
					new ProtocolCodecFilter(new TransferMessageCodecFactory(
							new TransferMessageDecoder(),
							new TransferMessageEncoder())));
			// 添加业务逻辑处理器类
			acceptor.getFilterChain().addLast("KeepAlive", keepAliveFilter);
			acceptor.getFilterChain().addLast("executor", new ExecutorFilter());
			IoSessionConfig cfg = acceptor.getSessionConfig();
			// 读写通道10秒内无操作进入空闲状态
			cfg.setIdleTime(IdleStatus.BOTH_IDLE, 60);
			// 绑定逻辑处理器
			acceptor.setHandler(handler); // 添加业务处理
			// 绑定端口

			while (!isRuning) {
				try {
					acceptor.bind(new InetSocketAddress(port));
					logger.info(" start listen " + port);
					isRuning = true;

				} catch (IOException e) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						logger.error("exception", e);
					}
					isRuning = false;
					logger.error(" bind " + port + " false. try again...", e);
				}
			}

			logger.info("gateway service start(port=" + port
					+ ") ,beging accept command...  ");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isRuning() {
		return isRuning;
	}

	/**
	 * 给用户user push一条消息下去
	 * 
	 * @param msg
	 *            消息
	 * @param user
	 *            用户
	 * @return 是否发送
	 */
	public boolean sendMessage(Message msg, String user) {

		if (!isRuning) {
			logger.info("pushServer not run");
			return false;
		}
		UserInfo userInfo = userPhoneMap.get(user.trim());
		if (userInfo == null) {
			logger.info("userInfo is null user=" + user
					+ "  userPhoneMap size=" + userPhoneMap.size() + " "
					+ userPhoneMap.toString());
			return false;
		}
		IoSession session = acceptor.getManagedSessions().get(
				userInfo.sessionId);
		if (session == null || session.isClosing())
			return false;
		TransferMsg msg1 = new TransferMsg(msg, TransferMsg.MSG_REQ,
				TransferMsg.STATUS_NOUSE);
		msg.setId(msg1.getSequence_Id());
		session.write(msg1);
		return true;

	}

	public void stop() {
		isRuning = false;
		acceptor.unbind();
		acceptor.dispose(true);
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

	public void removeUser(String user) {
		if (user != null) {
			UserInfo userInfo = userPhoneMap.remove(user);
			if (userInfo != null)
				userInfo = userConnectMap.remove(userInfo.sessionId);
		}

	}

	public void closeSession(IoSession ioSession) {
		if (ioSession != null) {
			UserInfo userInfo = userConnectMap.remove(ioSession.getId());
			if (userInfo != null) {
				userInfo = userPhoneMap.remove(userInfo.user);
			}
			ioSession.close(true);
			ioSession = null;
		}

	}

	public boolean isExist(String user) {
		return userPhoneMap.containsKey(user);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PushServer gateWay = new PushServer();
		gateWay.start(6018);
	}

	public IoAcceptor getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(IoAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	public ConcurrentMap<String, UserInfo> getUserPhoneMap() {
		return userPhoneMap;
	}

	public void setUserPhoneMap(ConcurrentMap<String, UserInfo> userPhoneMap) {
		this.userPhoneMap = userPhoneMap;
	}

	public ConcurrentMap<Long, UserInfo> getUserConnectMap() {
		return userConnectMap;
	}

	public void setUserConnectMap(ConcurrentMap<Long, UserInfo> userConnectMap) {
		this.userConnectMap = userConnectMap;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

}
