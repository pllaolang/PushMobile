package com.tec.push.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 
 * 
 * <p>
 * 说明: 底层传输的消息打包/解包
 * </p>
 * <p>
 * 创建日期 2015年8月21日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class TransferMsg {

	// ----------有头无体------------
	public static final int HEART_REQ = 0x00000002;
	public static final int HEART_RESP = 0x10000002;
	public static final int TERMINAL_REQ = 0x00000009;
	public static final int TERMINAL_RESP = 0x10000009;

	// ---------有头有体------------
	public static final int CONNECT_REQ = 0x00000001;
	public static final int CONNECT_RESP = 0x10000001;
	public static final int MSG_REQ = 0x00000003;
	public static final int MSG_RESP = 0x10000003;

	public static final int STATUS_NOUSE = 0;
	public static final int STATUS_OK = 1;
	public static final int STATUS_ERR = -1;
	// --------消息头---------
	private int total_Length;
	private int command_Id;
	private int sequence_Id;
	private int status;
	// -----消息体---------
	private int msg_Length;
	private byte[] msgByte;
	private byte[] attachedByte;

	private static int systemSequenceId = 0;

	public static synchronized int createSystemSequenceId() {
		++systemSequenceId;
		if (systemSequenceId >= Integer.MAX_VALUE) {
			systemSequenceId = 1;
		}
		return systemSequenceId;
	}

	private TransferMsg() {

	}

	public TransferMsg(int command_id, int status) {
		this.total_Length = 16;
		this.command_Id = command_id;
		this.sequence_Id = createSystemSequenceId();
		this.status = status;
	}

	public TransferMsg(int command_id, int sequence_id, int status) {
		this.total_Length = 16;
		this.command_Id = command_id;
		this.sequence_Id = sequence_id;
		this.status = status;
	}

	public TransferMsg(Message message, int command_Id, int status) {

		this.command_Id = command_Id;
		this.sequence_Id = createSystemSequenceId();
		this.status = status;

		if (message.getMsg() != null) {// 有内容
			try {
				this.msgByte = message.getMsg().getBytes("UTF-8");

				this.msg_Length = this.msgByte.length;

				if (message.getAttached() != null) {// 有内容有附件
					this.attachedByte = message.getAttached();
					this.total_Length = 20 + msg_Length
							+ this.attachedByte.length;
				} else {// 有内容无附件
					this.total_Length = 20 + msg_Length;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();

			}

		} else if (message.getAttached() != null) {// 有附件无内容
			this.msg_Length = 0;
			this.attachedByte = message.getAttached();
			this.total_Length = 20 + this.attachedByte.length;

		}
	}

	public TransferMsg(Message message, int command_Id, int secquence_Id,
			int status) {
		this.command_Id = command_Id;
		this.sequence_Id = secquence_Id;
		this.status = status;
		if (message.getMsg() != null) {// 有内容
			try {
				this.msgByte = message.getMsg().getBytes("UTF-8");

				this.msg_Length = this.msgByte.length;

				if (message.getAttached() != null) {// 有内容有附件
					this.attachedByte = message.getAttached();
					this.total_Length = 20 + msg_Length
							+ this.attachedByte.length;
				} else {// 有内容无附件
					this.total_Length = 20 + msg_Length;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();

			}

		} else if (message.getAttached() != null) {// 有附件无内容
			this.msg_Length = 0;
			this.attachedByte = message.getAttached();
			this.total_Length = 20 + this.attachedByte.length;

		}
	}

	public static TransferMsg parseMsg(byte[] dataByte, int total_Length) {
		// LogWriter.print("receive: " + toHex(int2bytes(total_Length))
		// + toHex(dataByte));
		TransferMsg msg = new TransferMsg();
		msg.total_Length = total_Length;

		try {
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
					dataByte);
			DataInputStream dataInputStream = new DataInputStream(
					byteInputStream);
			msg.command_Id = dataInputStream.readInt();
			msg.sequence_Id = dataInputStream.readInt();
			msg.status = dataInputStream.readInt();
			if (total_Length > 16) {
				msg.msg_Length = dataInputStream.readInt();
				if (msg.msg_Length != 0) {// 有内容
					msg.msgByte = new byte[msg.msg_Length];
					dataInputStream.readFully(msg.msgByte);
				}
				if (total_Length > 20 + msg.msg_Length) {// 有附近件
					msg.attachedByte = new byte[total_Length - 20
							- msg.msg_Length];
					dataInputStream.readFully(msg.attachedByte);
				}
			}

			dataInputStream.close();
			byteInputStream.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("解码失败");
			e.printStackTrace();
		}
		return msg;
	}

	public byte[] packByte() {
		try {
			ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
			DataOutputStream dataOutStream = new DataOutputStream(
					byteArrayOutStream);
			dataOutStream.writeInt(total_Length);
			dataOutStream.writeInt(command_Id);
			dataOutStream.writeInt(sequence_Id);
			dataOutStream.writeInt(status);
			// 有内容
			if (msgByte != null) {
				dataOutStream.writeInt(msgByte.length);
				dataOutStream.write(msgByte);
				if (attachedByte != null)
					dataOutStream.write(attachedByte);
			} else if (msgByte == null && attachedByte != null) {// 无内容有附件
				dataOutStream.writeInt(0);
				dataOutStream.write(attachedByte);
			} else {// 无内容，无附件

			}

			byte[] data = byteArrayOutStream.toByteArray();
			// LogWriter.print("send: " + toHex(data));
			dataOutStream.close();
			byteArrayOutStream.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String toHex(byte[] bytes, String separator) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String bb = Integer.toHexString(bytes[i]).toUpperCase();
			bb = bb.length() <= 1 ? "0" + bb : bb;
			bb = bb.length() >= 8 ? bb.substring(6) : bb;
			sb.append(bb + separator);
		}
		return sb.toString();
	}

	public static byte[] int2bytes(int num) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) ((num >>> (24 - i * 8)) & 0xff);
		}
		return b;
	}

	public int getTotal_Length() {
		return total_Length;
	}

	public void setTotal_Length(int total_Length) {
		this.total_Length = total_Length;
	}

	public int getCommand_Id() {
		return command_Id;
	}

	public void setCommand_Id(int command_Id) {
		this.command_Id = command_Id;
	}

	public int getSequence_Id() {
		return sequence_Id;
	}

	public void setSequence_Id(int sequence_Id) {
		this.sequence_Id = sequence_Id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getMsg_Length() {
		return msg_Length;
	}

	public void setMsg_Length(int msg_Length) {
		this.msg_Length = msg_Length;
	}

	public byte[] getMsgByte() {
		return msgByte;
	}

	public void setMsgByte(byte[] msgByte) {
		this.msgByte = msgByte;
	}

	public byte[] getAttachedByte() {
		return attachedByte;
	}

	public void setAttachedByte(byte[] attachedByte) {
		this.attachedByte = attachedByte;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (command_Id == HEART_REQ) {
			sb.append("HEART_REQ ");
		} else if (command_Id == HEART_RESP) {
			sb.append("HEART_RESP ");
		} else if (command_Id == TERMINAL_REQ) {
			sb.append("TERMINAL_REQ ");
		} else if (command_Id == TERMINAL_RESP) {
			sb.append("TERMINAL_RESP ");
		} else if (command_Id == CONNECT_REQ) {
			sb.append("CONNECT_REQ ");
		} else if (command_Id == CONNECT_RESP) {
			sb.append("CONNECT_RESP ");
		} else if (command_Id == MSG_REQ) {
			sb.append("MSG_REQ ");
		} else if (command_Id == MSG_RESP) {
			sb.append("MSG_RESP ");
		}
		sb.append("sequenceId=").append(sequence_Id);
		sb.append(",status=").append(status);
		if (msgByte != null) {
			try {
				sb.append(",msg=").append(new String(msgByte, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (attachedByte != null) {
			if (command_Id == CONNECT_REQ) {
				sb.append(",认证信息:" + toHex(attachedByte, " "));
			} else {
				sb.append(", 有附件数据");
			}
		}
		return sb.toString();
	}
}
