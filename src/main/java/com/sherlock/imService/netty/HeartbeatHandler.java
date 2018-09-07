package com.sherlock.imService.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
//	private final ByteBuf HEARTBEAT_SEQUENCE = Unpooled
//			.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.UTF_8));

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			switch (e.state()) {
            case READER_IDLE:
            	ctx.close();
                break;
			default:
				break;
			}	
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
