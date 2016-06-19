package com.santiagow.demo.http.mock;

import com.santiagow.util.IoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by Santiago on 2016/1/31.
 */
public class MockHttpClientTest {
	private SocketAddress addr;
	private MockHttpClient httpClient;

	@Before
	public void setup() throws Exception {
		addr = new InetSocketAddress("localhost", 8080);
		httpClient = new MockHttpClient(addr);
	}

	@After
	public void tearDown() throws Exception {
		IoUtil.close(httpClient);
	}

	@Test
	public void test() {
		httpClient.connect();
	}
}
