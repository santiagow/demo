package com.santiagow.demo.http;

import com.santiagow.demo.http.mock.MockHttpServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 *
 * @author Santiago Wang
 * @since 2016/6/20
 */
public class WebServerInitializer extends ChannelInitializer<SocketChannel> {
	private final SimpleChannelInboundHandler<FullHttpRequest> handler;

	public WebServerInitializer(SimpleChannelInboundHandler<FullHttpRequest> handler) {
		this.handler = handler;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new ChunkedWriteHandler());
		pipeline.addLast(handler);
	}

}
