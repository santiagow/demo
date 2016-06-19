package com.santiagow.demo.http;

import com.santiagow.json.GsonMap;
import com.santiagow.json.JsonArray;
import com.santiagow.json.JsonMap;
import com.santiagow.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 *
 * @author Santiago Wang
 * @since 2016/6/19
 */
@ChannelHandler.Sharable
public class WebServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static final Logger LOG = LoggerFactory.getLogger(WebServerHandler.class);
	private static final Set<String> SUPPORTED_PATHS = ApiPath.getPaths();
	private static final Set<HttpMethod> SUPPORTED_METHODS = new HashSet<HttpMethod>();

	static {
		SUPPORTED_METHODS.add(HttpMethod.GET);
		SUPPORTED_METHODS.add(HttpMethod.POST);
	}

	private final Map<String, Object> dataMap = new ConcurrentHashMap<String, Object>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		String uri = request.getUri();
		HttpMethod method = request.getMethod();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Receive request, method: {}, URI: {}", method, uri);
		}

		if (!SUPPORTED_METHODS.contains(method)) {
			LOG.warn("Unsupported request method - {}", method);
			sendError(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}

		if (!SUPPORTED_PATHS.contains(request.getUri())) {
			LOG.warn("Unsupported request path - {}", uri);
			sendError(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}

		ApiPath path = ApiPath.getEnumByPath(uri);
		switch (path) {
			case SAVE:
				save(ctx, request);
				break;
			case QUERY:
				query(ctx, request);
				break;
			case UPDATE:
				update(ctx, request);
				break;
			case REMOVE:
				remove(ctx, request);
				break;
			default:
				sendError(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOG.error(cause.getMessage(), cause);
		ctx.close();
	}

	//request format:
	//	GET: key1=value1[&key2=value2...]
	//	POST:
	//	{
	//		"data": {
	//			"key": "value",
	//			...
	//		}
	//	}
	//
	//response format:
	//	{
	//		"status": "..." //success, failure
	//	}
	private void save(ChannelHandlerContext ctx, FullHttpRequest request) {
		if (request.getMethod().equals(HttpMethod.GET)) {
			//TODO
		} else if (request.getMethod().equals(HttpMethod.POST)) {
			ByteBuf content = request.content();
			String body = content.toString(CharsetUtil.UTF_8);
			LOG.debug("request body - {}", body);

			JsonMap jmap = new GsonMap(body);
			JsonMap dataJmap = jmap.optJsonMap("data");
			if (dataJmap != null) {
				dataMap.putAll(dataJmap.getInternalMap());
			}
		}

		Map<String, Object> respMap = new HashMap<String, Object>();
		//TODO: enum for status code/string
		respMap.put("status", "success");
		//TODO: configurable to return success key set: "keys": ["key1", "key2", ...]

		sendResp(ctx, request, respMap, HttpResponseStatus.OK);
	}

	//request format:
	//	GET: keys=key1,...
	//	POST:
	//	{
	//		"keys": ["key1",...]
	//	}
	//
	//response format:
	//	{
	//		"status": "...", //success, failure
	//		"data": {
	//			"key1": "value",
	//			...
	//		}
	//	}
	private void query(ChannelHandlerContext ctx, FullHttpRequest request) {
		Map<String, Object> resDataMap = null;

		if (request.getMethod().equals(HttpMethod.GET)) {
			//TODO
		} else if (request.getMethod().equals(HttpMethod.POST)) {
			ByteBuf content = request.content();
			String body = content.toString(CharsetUtil.UTF_8);
			LOG.debug("request body - {}", body);

			JsonMap jmap = new GsonMap(body);
			JsonArray keysAr = jmap.optJsonArray("keys");
			if (keysAr == null) {
				resDataMap = Collections.emptyMap();
			} else {
				resDataMap = new HashMap<String, Object>();

				for (int i = 0, n = keysAr.length(); i < n; i++) {
					String key = keysAr.optString(i);
					resDataMap.put(key, dataMap.get(key));
				}
			}
		}

		Map<String, Object> respMap = new HashMap<String, Object>();
		respMap.put("status", "success");
		respMap.put("data", resDataMap);

		sendResp(ctx, request, respMap, HttpResponseStatus.OK);
	}

	//request format:
	//	GET: key1=value1[&key2=value2...]
	//	POST:
	//	{
	//		"data": {
	//			"key": "value",
	//			...
	//		}
	//	}
	//
	//response format:
	//	{
	//		"status": "..." //success, failure
	//	}
	private void update(ChannelHandlerContext ctx, FullHttpRequest request) {
		save(ctx, request);
	}

	//request format:
	//	GET: keys=key1,...
	//	POST:
	//	{
	//		"keys": ["key1",...]
	//	}
	//
	//response format:
	//	{
	//		"status": "..." //success, failure
	//	}
	private void remove(ChannelHandlerContext ctx, FullHttpRequest request) {
		Set<String> sucKeys = null;
		if (request.getMethod().equals(HttpMethod.GET)) {
			//TODO
		} else if (request.getMethod().equals(HttpMethod.POST)) {
			ByteBuf content = request.content();
			String body = content.toString(CharsetUtil.UTF_8);
			LOG.debug("request body - {}", body);

			JsonMap jmap = new GsonMap(body);
			JsonArray keysAr = jmap.optJsonArray("keys");
			if (keysAr == null) {
				sucKeys = Collections.emptySet();
			} else {
				sucKeys = new HashSet<String>();

				for (int i = 0, n = keysAr.length(); i < n; i++) {
					String key = keysAr.optString(i);
					Object value = dataMap.remove(key);
					if (value != null) {
						sucKeys.add(key);
					}
				}
			}
		}

		Map<String, Object> respMap = new HashMap<String, Object>();
		respMap.put("status", "success");
		respMap.put("removed_keys", sucKeys);

		sendResp(ctx, request, respMap, HttpResponseStatus.OK);
	}

	private void sendResp(ChannelHandlerContext ctx, final FullHttpRequest request, String body, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

		if (HttpHeaders.isKeepAlive(request)) {
			response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		ChannelFuture future = ctx.writeAndFlush(response);
	}

	private void sendResp(ChannelHandlerContext ctx, final FullHttpRequest request, Object body, HttpResponseStatus status) {
		sendResp(ctx, request, CommonUtil.getSafeGson().toJson(body), status);
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,
				Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				future.channel().close();
			}
		});
	}

	public enum ApiPath {
		SAVE("/save"),//save key value pair
		QUERY("/query"),//query by key
		UPDATE("/update"),//update by key
		REMOVE("/remove"),//remove by key
		NONE("");

		private static volatile Set<String> paths;
		private final String path;

		ApiPath(String path) {
			this.path = path;
		}

		public static Set<String> getPaths() {
			if (paths == null) {
				synchronized (ApiPath.class) {
					if (paths == null) {
						paths = new HashSet<String>();
						for (ApiPath path : ApiPath.class.getEnumConstants()) {
							paths.add(path.getPath());
						}
						paths = Collections.unmodifiableSet(paths);
					}
				}
			}

			return paths;
		}

		public static ApiPath getEnumByPath(String path) {
			if (StringUtils.isEmpty(path)) {
				return NONE;
			}

			for (ApiPath apiPath : ApiPath.class.getEnumConstants()) {
				if (StringUtils.equals(path, apiPath.getPath())) {
					return apiPath;
				}
			}

			return NONE;
		}

		public String getPath() {
			return path;
		}
	}
}