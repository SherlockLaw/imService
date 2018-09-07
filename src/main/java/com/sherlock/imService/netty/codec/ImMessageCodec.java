package com.sherlock.imService.netty.codec;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.CombinedChannelDuplexHandler;

public class ImMessageCodec extends CombinedChannelDuplexHandler<ChannelInboundHandler, ChannelOutboundHandler>{
	public ImMessageCodec(){
		super(new ImDecoder(), new ImEncoder());
	}
}
