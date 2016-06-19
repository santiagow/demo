package com.santiagow.demo.http;

/**
 * @author Santiago Wang
 * @since 2016/5/24
 */
public class BaseHttpResponseListener implements HttpResponseListener {
	private String respBody;
	private boolean isFinished;

	@Override
	public void setResponseBody(String respBody) {
		this.respBody = respBody;
	}

	@Override
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	@Override
	public String getResponse(long timeout) {
		//TODO: update timeout
		while (!isFinished) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return respBody;
	}

	@Override
	public String getResponse() {
		return getResponse(0);
	}
}
