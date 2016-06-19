package com.santiagow.demo.http;

import com.santiagow.demo.http.mock.MockHttpServerHandler;
import com.santiagow.demo.http.mock.MockHttpServerInitializer;
import com.santiagow.demo.server.Server;
import com.santiagow.demo.server.ServerState;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 *
 * @author Santiago Wang
 * @since 2016/6/19
 */
public class WebServer implements Server {
	private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

	private final SocketAddress addr;
	private ServerBootstrap boot;
	private EventLoopGroup boss;
	private EventLoopGroup worker;
	private Channel channel;
	private ServerState serverState = ServerState.NONE;

	//TODO: support HTTPS
	public WebServer(SocketAddress addr) {
		this.addr = addr;
	}

	public void startService() {
		if (boot == null) {
			boot = new ServerBootstrap();

			boss = new NioEventLoopGroup();
			worker = new NioEventLoopGroup();

			boot.group(boss, worker)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new WebServerInitializer(new WebServerHandler()));
		}

		ChannelFuture future = boot.bind(addr);
		future.syncUninterruptibly();
		channel = future.channel();
	}

	@Override
	public void start() {
		if (serverState == ServerState.STARTING || serverState == ServerState.STARTED) {
			LOG.warn("Already at work!");
			return;
		}

		if (serverState == ServerState.STOPPING || serverState == ServerState.STOPPED) {
			LOG.warn("Cannot start a stopped server!");
			return;
		}

		serverState = ServerState.STARTING;

		startService();

		serverState = ServerState.STARTED;
	}

	@Override
	public void stop() {
		if (serverState == ServerState.STOPPING || serverState == ServerState.STOPPED) {
			LOG.warn("Already stopped!");
			return;
		}
		serverState = ServerState.STOPPING;

		close();

		while (channel.isOpen()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		serverState = ServerState.STOPPED;
	}

	@Override
	public void pause() {
		if (serverState == ServerState.PAUSING || serverState == ServerState.PAUSED) {
			LOG.warn("Already paused!");
			return;
		}

		serverState = ServerState.PAUSING;

		// do not listen on the addr
		channel.close();

		serverState = ServerState.PAUSED;
	}

	@Override
	public void resume() {
		if (serverState == ServerState.STARTING || serverState == ServerState.STARTED) {
			LOG.warn("Already at work!");
			return;
		}

		serverState = ServerState.STARTING;

		startService();

		serverState = ServerState.STARTED;
	}

	@Override
	public ServerState getState() {
		return serverState;
	}

	public boolean isActive() {
		return channel.isOpen();
	}

	@Override
	public void close() {
		if (channel != null) {
			channel.close();
		}

		if (boss != null) {
			boss.shutdownGracefully();
		}

		if (worker != null) {
			worker.shutdownGracefully();
		}
	}

	public SocketAddress getAddr() {
		return addr;
	}
}




