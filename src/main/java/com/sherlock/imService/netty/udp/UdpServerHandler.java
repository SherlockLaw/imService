package com.sherlock.imService.netty.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.netty.ImInboundHandler;
import com.sherlock.imService.netty.ImServer;
import com.sherlock.imService.netty.codec.ImDecoder;
import com.sherlock.imService.netty.entity.AbstractMessage;
import com.sherlock.imService.redis.RedisService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(UdpServerHandler.class);

    private ImServer imServer;
	
	public UdpServerHandler(ImServer imServer){
		this.imServer = imServer;
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        ctx.close();
        logger.error(cause.getMessage(),cause);
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
		AbstractMessage message = ImDecoder.TCPandUDPCommonDecode(packet.content());
		logger.info("收到来自客户端的消息:"+message.getClass().getSimpleName()+JSONObject.toJSONString(message));
		ImInboundHandler.clientMessageHandler(imServer, ctx, message, packet);
	}

}
