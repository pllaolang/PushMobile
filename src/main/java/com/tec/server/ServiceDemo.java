package com.tec.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSON;
import com.tec.push.MessageHandler;
import com.tec.push.PushServer;
import com.tec.push.Util;
import com.tec.push.msg.Constants;
import com.tec.push.msg.Message;
import com.tec.push.msg.PushMessage;
import com.tec.push.msg.TransferMsg;

public class ServiceDemo implements MessageHandler {
	private static final Logger log = Logger.getLogger(ServiceDemo.class);
	private PushServer pushServer;

	private String defaultPwd = TransferMsg.toHex(
			Util.toMd5("tecblazer".getBytes()), "");
	private Map<String, List<Message>> falseSendMsgMap;

	public void init() {

		falseSendMsgMap = new HashMap<String, List<Message>>();
	}

	public void start() {
		init();
		pushServer = new PushServer();
		pushServer.setMessageHandler(this);
		pushServer.start(7788);
		// startSendData();
		// pushServer.stop();

		// try {
		// Thread.sleep(30000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// PushMessage pushMsg = new PushMessage();
		// pushMsg.setCommand(Constants.COMMAND_CALLLOG_REQ);
		// Message msg = new Message(JSON.toJSONString(pushMsg));
		// sendMsg(msg, "13316523988");
	}

	public void sendMsg(final Message message, final String user) {

		Runnable runnable = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub

				if (pushServer.sendMessage(message, user)) {// 第一次发送
					log.info("发送成功：user=" + user + " msg=" + message.getMsg());
				} else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (pushServer.sendMessage(message, user)) {// 第二次发送
						log.info("第2次发送成功：user=" + user + " msg="
								+ message.getMsg());

					} else {
						try {
							Thread.sleep(8000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (pushServer.sendMessage(message, user)) {// 第三次发送
							log.info("第3次发送成功：user=" + user + " msg="
									+ message.getMsg());

						} else {
							if (falseSendMsgMap.containsKey(user)) {//
								List<Message> list = falseSendMsgMap.get(user);
								list.add(message);

							} else {
								List<Message> list = new ArrayList<Message>();
								list.add(message);
								falseSendMsgMap.put(user, list);
							}
							log.info("发送3次失败，加入发送队列：user=" + user + " msg="
									+ message.getMsg());
						}
					}

				}

			}

		};
		new Thread(runnable).start();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("log4j.properties");
		ServiceDemo serviceDemo = new ServiceDemo();
		serviceDemo.start();
		serviceDemo.startTest("13316523988");
		// serviceDemo.startLock("13316523988");

	}

	public void messageReceived(Message message, String user) {
		// TODO Auto-generated method stub
		PushMessage req = JSON.parseObject(message.getMsg(), PushMessage.class);
		log.info(message.getMsg());
		if (req == null) {
			log.info("数据json解析对象失败:");
		} else {// 消息处理

		}
	}

	public void messageResp(int respId, String user) {
		// TODO Auto-generated method stub

	}

	public void messageSent(int messageId, String user) {
		// TODO Auto-generated method stub

	}

	public boolean login(String user, String pwd) {
		if (pushServer.isExist(user))// 重复用户
			pushServer.removeUser(user);
		if (pwd.equals(defaultPwd)) {
			log.info(user + "login success");
			if (falseSendMsgMap.containsKey(user))
				startSendFalseMsg(user);
			return true;
		} else {
			log.info(user + "login false");
			return false;
		}

	}

	/**
	 * 发送指定用户的未成功信息
	 * 
	 * @param user
	 */
	private void startSendFalseMsg(final String user) {
		Runnable runnable = new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<Message> list = falseSendMsgMap.remove(user);
				if (list != null && !list.isEmpty()) {
					for (int i = 0; i < list.size(); i++) {
						sendMsg(list.get(i), user);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

		};
		new Thread(runnable).start();
	}

	

	

	public void startTest(final String msIsdn) {
		Runnable runnable = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub

				try {
					Thread.sleep(10000);

					PushMessage pushMsg = new PushMessage();
					// pushMsg.setCommand(Constants.COMMAND_APPRUNLOG_REQ);
					// sendMsg(new Message(JSON.toJSONString(pushMsg)), msIsdn);
					//
					// pushMsg = new PushMessage();
					// pushMsg.setCommand(Constants.COMMAND_CALLLOG_REQ);
					// sendMsg(new Message(JSON.toJSONString(pushMsg)), msIsdn);
					//
					// pushMsg = new PushMessage();
					// pushMsg.setCommand(Constants.COMMAND_INSTALLEDAPPLOG_REQ);
					// sendMsg(new Message(JSON.toJSONString(pushMsg)), msIsdn);
					//
					pushMsg = new PushMessage();
					pushMsg.setCommand(Constants.COMMAND_LOCATIONLOG_REQ);
					sendMsg(new Message(JSON.toJSONString(pushMsg)), msIsdn);
					//
					// pushMsg = new PushMessage();
					// pushMsg.setCommand(Constants.COMMAND_MSGLOG_REQ);
					// sendMsg(new Message(JSON.toJSONString(pushMsg)), msIsdn);
					//
					// pushMsg = new PushMessage();
					// pushMsg.setCommand(Constants.COMMAND_WEBLOG_REQ);
					// sendMsg(new Message(JSON.toJSONString(pushMsg)), msIsdn);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// pushMsg=new PushMessage();
				// pushMsg.setCommand(Constants.);
				// sendMsg(new Message(JSON.toJSONString(pushMsg)));

			}

		};
		new Thread(runnable).start();
	}
}
