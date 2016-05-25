package com.santiagow.demo.server;

import com.santiagow.util.IoUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.santiagow.demo.server.MockHttpServerHandler.ApiPath;

/**
 * Created by Santiago on 2016/1/25.
 */
public class MockHttpServerTest {
	private Logger LOG = LoggerFactory.getLogger(MockHttpServerTest.class);
	private SocketAddress addr;
	private MockHttpServer httpServer;

	private String saveBody;
	private String queryBody;
	private String updateBody;
	private String removeBody;

	@Before
	public void setup() throws Exception {
		addr = new InetSocketAddress("localhost", 8080);
		httpServer = new MockHttpServer(addr, false);

		saveBody = getContentByFileName("com/santiagow/demo/server/save.json");
		queryBody = getContentByFileName("com/santiagow/demo/server/query.json");
		updateBody = getContentByFileName("com/santiagow/demo/server/update.json");
		removeBody = getContentByFileName("com/santiagow/demo/server/remove.json");
	}

	@After
	public void tearDown() throws Exception {
		IoUtil.close(httpServer);
	}

	@Test
	public void testCRUD() throws Exception {
		httpServer.startServer();

		Thread.sleep(1000);

		Assert.assertTrue(httpServer.isStarted());

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

	private String getContentByFileName(String fileName) {
		return IoUtil.getContentFromStream(this.getClass().getClassLoader().getResourceAsStream(fileName));
	}
}
