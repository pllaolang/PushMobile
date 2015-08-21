package com.tec.push;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.tec.push.msg.Message;
import com.tec.push.msg.TransferMsg;
/**
 * 
 * 
 * <p>
 * 说明:消息处理
 * </p>
 * <p>
 * 创建日期 2015年8月20日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class PushHandler extends IoHandlerAdapter {
	private static final Logger log = Logger.getLogger(PushHandler.class);

	private PushServer pushServer;

	public PushHandler(PushServer pushServer) {
		this.pushServer = pushServer;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if (cause instanceof IOException) {
			pushServer.closeSession(session);
			log.error(
					"session closeed because exception:" + cause.getMessage()
							+ " ( id=" + session.getId() + " ,ip="
							+ session.getRemoteAddress() + " )", cause);
		} else {
			log.error("session  exception:" + cause.getMessage() + " ( id="
					+ session.getId() + " ,ip=" + session.getRemoteAddress()
					+ " )", cause);
		}

		// session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		TransferMsg msg = (TransferMsg) message;
		if (msg.getCommand_Id() == TransferMsg.CONNECT_REQ) {// 连接请求

			UserInfo userInfo = new UserInfo();
			userInfo.clientIp = ((InetSocketAddress) session.getRemoteAddress())
					.getAddress().getHostAddress();
			userInfo.clientPort = ((InetSocketAddress) session
					.getRemoteAddress()).getPort();
			userInfo.loginDate = new Date();
			userInfo.pwd = " ";
			userInfo.sessionId = session.getId();
			userInfo.user = new String(msg.getMsgByte(), "utf-8");
			userInfo.pwd = TransferMsg.toHex(msg.getAttachedByte(), "");
			// TODO 数据库比对用户名和密码
			if (pushServer.getMessageHandler() != null) {
				if (pushServer.getMessageHandler().login(userInfo.user,
						userInfo.pwd)) {
					TransferMsg connect_respMsg = new TransferMsg(
							TransferMsg.CONNECT_RESP, msg.getSequence_Id(),
							TransferMsg.STATUS_OK);
					session.write(connect_respMsg);
					
					pushServer.getUserConnectMap().put(session.getId(),
							userInfo);
					pushServer.getUserPhoneMap().put(userInfo.user, userInfo);
				} else {
					TransferMsg connect_respMsg = new TransferMsg(
							TransferMsg.CONNECT_RESP, msg.getSequence_Id(),
							TransferMsg.STATUS_ERR);
					session.write(connect_respMsg);
				}
			} else {
				TransferMsg connect_respMsg = new TransferMsg(
						TransferMsg.CONNECT_RESP, msg.getSequence_Id(),
						TransferMsg.STATUS_OK);
				session.write(connect_respMsg);
				
				pushServer.getUserConnectMap().put(session.getId(), userInfo);
				pushServer.getUserPhoneMap().put(userInfo.user, userInfo);
			}

		} else if (msg.getCommand_Id() == TransferMsg.MSG_REQ) {// 消息请求
			TransferMsg respMsg = new TransferMsg(TransferMsg.MSG_RESP,
					msg.getSequence_Id(), TransferMsg.STATUS_OK);
			session.write(respMsg);
			if (pushServer.getMessageHandler() != null) {
				Message msg1 = new Message(
						new String(msg.getMsgByte(), "UTF-8"),
						msg.getAttachedByte());
				UserInfo userInfo = pushServer.getUserConnectMap().get(
						session.getId());
				if (userInfo != null && userInfo.user != null) {
					pushServer.getMessageHandler().messageReceived(msg1,
							userInfo.user);
				} else {
					pushServer.getMessageHandler().messageReceived(msg1, null);
				}
			}
		} else if (msg.getCommand_Id() == TransferMsg.MSG_RESP) {// 消息回复
			if (pushServer.getMessageHandler() != null) {
				UserInfo userInfo = pushServer.getUserConnectMap().get(
						session.getId());
				if (userInfo != null && userInfo.user != null) {
					pushServer.getMessageHandler().messageResp(
							msg.getSequence_Id(), userInfo.user);
				} else {
					pushServer.getMessageHandler().messageResp(
							msg.getSequence_Id(), null);
				}
			}

		}
		log.info("receive " + session.getRemoteAddress() + "  id="
				+ session.getId() + "  msg=" + message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		TransferMsg msg = (TransferMsg) message;
		if (msg.getCommand_Id() == TransferMsg.MSG_REQ
				&& pushServer.getMessageHandler() != null) {
			UserInfo userInfo = pushServer.getUserConnectMap().get(
					session.getId());
			if (userInfo != null && userInfo.user != null) {
				pushServer.getMessageHandler().messageSent(
						msg.getSequence_Id(), userInfo.user);
			} else {
				pushServer.getMessageHandler().messageSent(
						msg.getSequence_Id(), null);
			}

		}
		log.info("send " + session.getRemoteAddress() + "  id="
				+ session.getId() + "  msg=" + message);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		pushServer.closeSession(session);
		log.info("sessionClosed");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		log.info("remote=" + session.getRemoteAddress() + "  id="
				+ session.getId() + "  " + "sessionCreated");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		// TODO Auto-generated method stub
		log.info("remote=" + session.getRemoteAddress() + "  id="
				+ session.getId() + "  " + "sessionIdle");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		log.info("remote=" + session.getRemoteAddress() + "  id="
				+ session.getId() + "  " + "sessionOpened");
	}

}
