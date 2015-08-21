package com.tec.push.msg;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * 
 * 
 * <p>
 * 说明: 解码
 * </p>
 * <p>
 * 创建日期 2015年8月21日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class TransferMessageDecoder implements MessageDecoder {
	private static final Logger log = Logger
			.getLogger(TransferMessageDecoder.class);

	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		// TODO Auto-generated method stub

		if (in.remaining() < 4) {
			return MessageDecoderResult.NEED_DATA;
		}
		int len = in.getInt();

		if (in.remaining() < len - 4) {
			return MessageDecoderResult.NEED_DATA;
		}

		return MessageDecoderResult.OK;
	}

	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub

		int len = in.getInt();
		byte[] temp = new byte[len - 4];
		in.get(temp);
		if (len > 1000) {
			log.info("receive:  (id=" + session.getId() + " frome="
					+ session.getRemoteAddress() + " )");
		} else {
			log.info("receive: "
					+ TransferMsg.toHex(TransferMsg.int2bytes(len), " ")
					+ TransferMsg.toHex(temp, " ") + "(id=" + session.getId()
					+ " frome=" + session.getRemoteAddress() + " )");
		}
		out.write(TransferMsg.parseMsg(temp, len));
		return MessageDecoderResult.OK;

		// return MessageDecoderResult.NOT_OK;

	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
