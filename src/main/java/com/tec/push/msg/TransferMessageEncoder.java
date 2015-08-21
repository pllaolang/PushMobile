package com.tec.push.msg;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

/**
 * 
 * 
 * <p>
 * 说明:编码
 * </p>
 * <p>
 * 创建日期 2015年8月21日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class TransferMessageEncoder implements MessageEncoder<TransferMsg> {
	private static final Logger log = Logger
			.getLogger(TransferMessageEncoder.class);

	public void encode(IoSession session, TransferMsg msg,
			ProtocolEncoderOutput out) throws Exception {

		byte[] tempByte = msg.packByte();
		if (tempByte.length > 1000) {
			log.info("send:   (id=" + session.getId() + " to="
					+ session.getRemoteAddress() + " )");
		} else {
			log.info("send: " + TransferMsg.toHex(tempByte, " ") + "(id="
					+ session.getId() + " to=" + session.getRemoteAddress()
					+ " )");
		}
		IoBuffer buffer = IoBuffer.allocate(tempByte.length);
		buffer.put(tempByte);
		buffer.flip();
		out.write(buffer);

	}

}
