package com.santiagow.test;

import com.santiagow.demo.http.mock.MockHttpClient;
import com.santiagow.util.IoUtil;
import org.junit.Assert;

import java.net.SocketAddress;

/**
 * @author Santiago Wang
 * @since 2016/6/19
 */
public final class TestUtil {

	public static String getContentByFileName(String fileName) {
		return IoUtil.getContentFromStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
	}

	public static void checkConnection(SocketAddress addr, boolean isSuccess) throws Exception {
		MockHttpClient client = new MockHttpClient(addr);
		try {
			client.connect();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			Thread.sleep(1000);

			if (isSuccess) {
				Assert.assertTrue(client.isConnected());
			} else {
				Assert.assertFalse(client.isConnected());
			}

			Thread.sleep(100);

			IoUtil.close(client);
		}
	}
}
