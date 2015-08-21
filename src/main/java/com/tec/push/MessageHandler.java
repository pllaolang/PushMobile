package com.tec.push;

import com.tec.push.msg.Message;

/**
 * 
 * 
 * <p>
 * 说明:消息调用接口
 * </p>
 * <p>
 * 创建日期 2015年8月20日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public interface MessageHandler {
	/**
	 * push消息
	 * 
	 * @param message
	 *            消息
	 * @param user
	 *            用户
	 */
	public void messageReceived(Message message, String user);

	/**
	 * 接收消息回应
	 * 
	 * @param respId
	 *            消息ID
	 * @param user
	 *            用户
	 */
	public void messageResp(int respId, String user);

	/**
	 * 消息发送完成
	 * 
	 * @param messageId
	 *            消息
	 * @param user
	 *            用户
	 */
	public void messageSent(int messageId, String user);

	/**
	 * 登录认证，成功则返回true,失败返回false;
	 * 
	 * @param user
	 * @param pwd
	 * @return
	 */
	public boolean login(String user, String pwd);
}
