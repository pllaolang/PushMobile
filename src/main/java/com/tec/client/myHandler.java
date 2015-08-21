package com.tec.client;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.tec.push.msg.Message;
import com.tec.push.msg.TransferMsg;

public class myHandler extends IoHandlerAdapter {
	private Connection connection;
	private static final Logger Log = Logger.getLogger(myHandler.class);
	public myHandler(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if (cause instanceof IOException) {
			connection.stop();
			// connection.setConnState(Connection.CONNSTATE_NOT_CONNECTED);
			Log.info("session closeed because exception:"
					+ cause.getMessage() + " ( id=" + session.getId() + " ,ip="
					+ session.getRemoteAddress() + " )");
		} else {
			Log.info( " exception:" + cause.getMessage() + " ( id="
					+ session.getId() + " ,ip=" + session.getRemoteAddress()
					+ " )", cause);
//			LogWriter.print(" exception:" + cause.getMessage() + " ( id="
//					+ session.getId() + " ,ip=" + session.getRemoteAddress()
//					+ " )");
		}

	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		TransferMsg msg = (TransferMsg) message;
		
		if (msg.getCommand_Id() == TransferMsg.CONNECT_RESP) {
			if (msg.getStatus() == TransferMsg.STATUS_OK) {
				connection.setConnState(Connection.CONNSTATE_WORK);
				Log.info("login success (" + session.getRemoteAddress()
						+ ")");
			} else {
				Log.info("login false (" + session.getRemoteAddress()
						+ ")");
			}
		} else if (msg.getCommand_Id() == TransferMsg.MSG_REQ) {
			TransferMsg respMsg = new TransferMsg(TransferMsg.MSG_RESP,
					msg.getSequence_Id(), TransferMsg.STATUS_OK);
			session.write(respMsg);
			if (connection.getMessageHandler() != null) {
				Message msg1 = new Message(
						new String(msg.getMsgByte(), "UTF-8"),
						msg.getAttachedByte());
				connection.getMessageHandler().messageReceived(msg1);
			}
		} else if (msg.getCommand_Id() == TransferMsg.MSG_RESP) {
			if (connection.getMessageHandler() != null)
				connection.getMessageHandler()
						.messageResp(msg.getSequence_Id());
		}

		// connection.getMessageHandler().messageReceived(message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		// LogWriter.print("send remote=" + session.getRemoteAddress() + "  id="
		// + session.getId() + "  msg=" + message);
		TransferMsg msg = (TransferMsg) message;
		if (msg.getCommand_Id() == TransferMsg.MSG_REQ
				&& connection.getMessageHandler() != null) {
			connection.getMessageHandler().messageSent(msg.getSequence_Id());
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		// connection.setConnState(Connection.CONNSTATE_NOT_CONNECTED);
		// if (session != null && !session.isClosing()) {
		// session.close(true);
		// }
		connection.stop();
		// connection..connect( session.getRemoteAddress(), this);
		Log.info("sessionClosed");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		Log.info("remote=" + session.getRemoteAddress() + "  id="
				+ session.getId() + "  " + "sessionCreated");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		// TODO Auto-generated method stub
		Log.info("remote=" + session.getRemoteAddress() + "  id="
				+ session.getId() + "  " + "sessionIdle");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		Log.info("remote=" + session.getRemoteAddress() + "  id="
				+ session.getId() + "  " + "sessionOpened");
	}

}
