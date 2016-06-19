package com.santiagow.demo.http;

import com.santiagow.demo.http.mock.MockHttpClient;
import com.santiagow.test.TestUtil;
import com.santiagow.util.IoUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.santiagow.demo.http.WebServerHandler.ApiPath;
import static com.santiagow.test.TestUtil.checkConnection;

/**
 * @author Santiago Wang
 * @since 2016/6/19
 */
public class WebServerTest {
	private Logger LOG = LoggerFactory.getLogger(WebServerTest.class);
	private SocketAddress addr;
	private WebServer server;

	private String saveBody;
	private String queryBody;
	private String updateBody;
	private String removeBody;

	@Before
	public void setup() throws Exception {
		addr = new InetSocketAddress("localhost", 8080);
		server = new WebServer(addr);

		saveBody = TestUtil.getContentByFileName("com/santiagow/demo/http/save.json");
		queryBody = TestUtil.getContentByFileName("com/santiagow/demo/http/query.json");
		updateBody = TestUtil.getContentByFileName("com/santiagow/demo/http/update.json");
		removeBody = TestUtil.getContentByFileName("com/santiagow/demo/http/remove.json");
	}

	@After
	public void tearDown() throws Exception {
		IoUtil.close(server);
	}

	@Test
	public void testCRUD() throws Exception {
		server.startService();

		Thread.sleep(1000);

		Assert.assertTrue(server.isActive());

		MockHttpClient client = new MockHttpClient(addr);
		client.connect();

		Thread.sleep(1000);

		Assert.assertTrue(client.isConnected());

		//test save
		BaseHttpResponseListener listener = new BaseHttpResponseListener();
		client.executePost(ApiPath.SAVE.getPath(), saveBody, listener);
		String resp = listener.getResponse();
		Assert.assertNotNull(resp);
		Assert.assertTrue(resp.length() > 0);
		LOG.info("save response: \n{}", resp);

		//test query
		listener = new BaseHttpResponseListener();
		client.executePost(ApiPath.QUERY.getPath(), queryBody, listener);
		resp = listener.getResponse();
		Assert.assertNotNull(resp);
		Assert.assertTrue(resp.length() > 0);
		LOG.info("query response: \n{}", resp);
		//verify query result is saved
		//...

		//test update
		listener = new BaseHttpResponseListener();
		client.executePost(ApiPath.UPDATE.getPath(), updateBody, listener);
		resp = listener.getResponse();
		Assert.assertNotNull(resp);
		Assert.assertTrue(resp.length() > 0);
		LOG.info("update response: \n{}", resp);

		//verify update success
		//...

		//test remove
		listener = new BaseHttpResponseListener();
		client.executePost(ApiPath.UPDATE.getPath(), removeBody, listener);
		resp = listener.getResponse();
		Assert.assertNotNull(resp);
		Assert.assertTrue(resp.length() > 0);
		LOG.info("remove response: \n{}", resp);
	}

	@Test
	public void testPauseResume() throws Exception {
		server.startService();

		Thread.sleep(1000);

		Assert.assertTrue(server.isActive());
		checkConnection(addr, true);

		// pause
		server.pause();

		Thread.sleep(1000);

		checkConnection(addr, false);

		// resume
		server.resume();

		Thread.sleep(1000);

		checkConnection(addr, true);

	}

	@Test
	public void testStop() throws Exception {
		server.startService();

		Thread.sleep(1000);

		Assert.assertTrue(server.isActive());
		checkConnection(addr, true);

		// stop
		server.stop();

		Thread.sleep(1000);

		checkConnection(addr, false);

		// start again
		try {
			server.start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		Thread.sleep(1000);

		checkConnection(addr, false);
	}

}
