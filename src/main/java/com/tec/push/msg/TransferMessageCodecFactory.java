package com.tec.push.msg;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;


public class TransferMessageCodecFactory extends DemuxingProtocolCodecFactory {
	private TransferMessageDecoder decoder;
	private TransferMessageEncoder encoder;

	public TransferMessageCodecFactory(TransferMessageDecoder decoder,
			TransferMessageEncoder encoder) {
		this.decoder = decoder;
		this.encoder = encoder;
		addMessageDecoder(this.decoder);
		addMessageEncoder(TransferMsg.class, this.encoder);
	}

}
