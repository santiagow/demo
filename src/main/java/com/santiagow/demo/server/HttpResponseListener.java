package com.santiagow.demo.server;

/**
 * HTTP response listener.
 *
 * @author Santiago Wang
 * @since 2016/5/23
 */
public interface HttpResponseListener {

	void setResponseBody(String body);
	void setFinished(boolean isFinished);
	String getResponse(long timeout);
	String getResponse();
}
