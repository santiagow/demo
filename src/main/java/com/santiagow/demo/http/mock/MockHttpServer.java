package com.santiagow.demo.http.mock;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.SocketAddress;

/**
 * @author Santiago Wang
 */
public class MockHttpServer implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(MockHttpServer.class);
	private final SocketAddress addr;
	private final boolean responseOnce;
	private EventLoopGroup boss;
	private EventLoopGroup worker;
	private Channel channel;

	private MockHttpServerHandler handler;

	public MockHttpServer(SocketAddress addr) {
		this(addr, false);
	}

	//TODO: support HTTPS
	public MockHttpServer(SocketAddress addr, boolean responseOnce) {
		this.addr = addr;
		this.responseOnce = responseOnce;
	}

	public void startServer() {
		ServerBootstrap boot = new ServerBootstrap();

		boss = new NioEventLoopGroup();
		worker = new NioEventLoopGroup();

		boot.group(boss, worker)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.SO_BACKLOG, 100)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new MockHttpServerInitializer(new MockHttpServerHandler()));

		ChannelFuture future = boot.bind(addr);
		future.syncUninterruptibly();
		channel = future.channel();
	}

	public boolean isStarted() {
		return channel.isOpen();
	}

	@Override
	public void close() {
		channel.close();
		boss.shutdownGracefully();
		worker.shutdownGracefully();
	}

	public void registerResponse(HttpMethod method, String path, HttpResponseStatus respStatus, String respBody) {
		handler = new MockHttpServerHandler(method, path, respStatus, respBody);
	}
}




