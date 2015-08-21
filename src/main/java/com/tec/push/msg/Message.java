package com.tec.push.msg;

/**
 * 
 * 
 * <p>
 * 说明: 字符串+附件的通用消息结构
 * </p>
 * <p>
 * 创建日期 2015年8月21日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class Message {

	private String msg;

	private byte[] attached;

	private int id;

	public Message(String msg) {
		this(msg, null);
	}

	public Message(String msg, byte[] attached) {

		this.msg = msg;
		this.attached = attached;

	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public byte[] getAttached() {
		return attached;
	}

	public void setAttached(byte[] attached) {
		this.attached = attached;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
