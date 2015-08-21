package com.tec.client;

import com.tec.push.msg.Message;

public interface MessageHandler {
	/**
	 * 接收到消息
	 * 
	 * @param message
	 */
	public void messageReceived(Message message);

	/**
	 * 接收到已发信息的resp
	 * 
	 * @param respId
	 */
	public void messageResp(int respId);

	/**
	 * 信息已发送
	 * 
	 * @param messageId
	 */
	public void messageSent(int messageId);
}
