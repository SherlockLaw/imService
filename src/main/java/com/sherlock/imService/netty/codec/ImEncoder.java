package com.sherlock.imService.netty.codec;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.netty.ImInboundHandler;
import com.sherlock.imService.netty.ImServer;
import com.sherlock.imService.netty.configure.Configure;
import com.sherlock.imService.netty.entity.AbstractMessage;
import com.sherlock.imService.netty.entity.RoutMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToByteEncoder;

public class ImEncoder extends MessageToByteEncoder<Object>{
	private static final Logger logger = LoggerFactory.getLogger(ImEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		AbstractMessage message = (AbstractMessage) msg;
		//报文头部
		out.writeInt(Configure.START_FLAG);
		out.writeByte(Configure.MSG_VER);
		out.writeByte(message.getMsgType());
		out.writeInt(0);
		out.writeCharSequence(JSONObject.toJSONString(message), Configure.CHARSET);
		out.setInt(Configure.POSITION_LENGTH, out.readableBytes());
	}
	private static void encode(Object msg, ByteBuf out){
		AbstractMessage message = (AbstractMessage) msg;
		//报文头部
		out.writeInt(Configure.START_FLAG);
		out.writeByte(Configure.MSG_VER);
		out.writeByte(message.getMsgType());
		out.writeInt(0);
		out.writeCharSequence(JSONObject.toJSONString(message), Configure.CHARSET);
		out.setInt(Configure.POSITION_LENGTH, out.readableBytes());
	}
	public static void write(ChannelHandlerContext ctx,AbstractMessage msg,InetSocketAddress address){
		if (Configure.isTcp) {
			ctx.writeAndFlush(msg);
		} else {
			writeByUDP(ctx, msg,address);
		}
	}
	public static void writeByUDP(ChannelHandlerContext ctx,AbstractMessage msg,InetSocketAddress address){
		ByteBuf buf = Unpooled.directBuffer(1024);
		ImEncoder.encode(msg,buf);
		Channel channel = null;
		if (ctx!=null) {
			channel = ctx.channel();
		} else {
			channel = ImServer.channel;
		}
		logger.debug("发送时的地址："+address);
		channel.writeAndFlush(new DatagramPacket(buf,address));//.sync();
	}
}
