package com.santiagow.demo.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MockHttpClient implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(MockHttpServer.class);
	private final SocketAddress addr;
	private EventLoopGroup group;
	private Channel channel;
	private MockHttpClientHandler channelHandler;

	public MockHttpClient(SocketAddress addr) {
		this.addr = addr;
	}

	public void connect() {
		Bootstrap boot = new Bootstrap();
		final EventLoopGroup group = new NioEventLoopGroup();

		channelHandler = new MockHttpClientHandler();
		boot.group(group)
				.channel(NioSocketChannel.class)
				.handler(new MockHttpClientInitializer(channelHandler));

		try {
			channel = boot.connect(addr).sync().channel();
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public boolean isConnected() {
		return channel != null && channel.isActive();
	}

	@Override
	public void close() {
		group.shutdownGracefully();
		if (channel != null) {
			channel.close();
		}
	}

	public ChannelFuture executeGet(String reqPath) {
		return executeGet(reqPath, null);
	}

	public ChannelFuture executeGet(String reqPath, HttpResponseListener responseListener) {
		final HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, reqPath);

		request.headers()
				.set(HttpHeaders.Names.ACCEPT, "text/plain")
				.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
				.set(HttpHeaders.Names.ACCEPT_ENCODING, "gzip,deflate");

		ChannelFuture future = channel.writeAndFlush(request);

		channelHandler.addListener(responseListener);

		return future;
	}

	public ChannelFuture executePost(String reqPath, String data) {
		return executePost(reqPath, data, null);
	}

	public ChannelFuture executePost(String reqPath, String data, HttpResponseListener responseListener) {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(data.length() << 1)
				.writeBytes(data.getBytes());
		final FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, reqPath, buf);

		request.headers()
				.set(HttpHeaders.Names.ACCEPT, "text/plain")
				.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
				.set(HttpHeaders.Names.ACCEPT_ENCODING, "gzip,deflate")
				.set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

		ChannelFuture future = channel.writeAndFlush(request);

		channelHandler.addListener(responseListener);

		return future;
	}

	public static class MockHttpClientInitializer extends ChannelInitializer<SocketChannel> {
		private final ChannelInboundHandlerAdapter handler;

		public MockHttpClientInitializer(ChannelInboundHandlerAdapter handler) {
			this.handler = handler;
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new HttpClientCodec());
			pipeline.addLast(new HttpContentDecompressor());
			pipeline.addLast(handler);
		}
	}

	@ChannelHandler.Sharable
	public static class MockHttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
		private StringBuilder sb = new StringBuilder();
		private List<HttpResponseListener> respListeners = new ArrayList<HttpResponseListener>();
		private boolean isChunked = false;

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
			if (msg instanceof HttpResponse) {
				HttpResponse response = (HttpResponse) msg;
				if (StringUtils.equals(response.headers().get("Transfer-Encoding"), "chunked")) {
					isChunked = true;
				}

				LOG.debug("Response Status: {}", response.getStatus().code());
			}

			if (msg instanceof HttpContent) {
				HttpContent content = (HttpContent) msg;
				sb.append(content.content().toString(CharsetUtil.UTF_8));

				if (content instanceof LastHttpContent) {
					//assume that netty keep the order of http response
					Iterator<HttpResponseListener> it = respListeners.iterator();
					while(it.hasNext()) {
						HttpResponseListener respListener = it.next();
						respListener.setResponseBody(sb.toString());
						respListener.setFinished(true);
						break;//TODO: only remove first one???
					}

					ctx.close();

					//reset StringBuilder
					sb.setLength(0);
				} else if (!isChunked) {
					Iterator<HttpResponseListener> it = respListeners.iterator();
					while(it.hasNext()) {
						HttpResponseListener respListener = it.next();
						respListener.setResponseBody(sb.toString());
						respListener.setFinished(true);
						it.remove();
						break;//TODO: only remove first one???
					}

					//ctx.close();

					//reset StringBuilder
					sb.setLength(0);
				}
			}
		}

		public void addListener(HttpResponseListener respListener) {
			if (respListener == null) {
				return;
			}

			respListeners.add(respListener);
		}
	}

}
