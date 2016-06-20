package com.santiagow.demo.ha;

import com.santiagow.demo.server.ServerState;
import com.santiagow.util.IoUtil;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;
import static com.santiagow.test.TestUtil.checkConnection;

/**
 * @author Santiago Wang
 * @since 2016/6/5
 */
public class HaDemoServerTest {
	private TestingServer zkServer;
	private String zkPath;
	private HaDemoServer server;
	private HaDemoServer competitor1;
	private HaDemoServer competitor2;
	private int serverPort1;
	private int serverPort2;
	private int serverPort3;

	@Before
	public void setup() throws Exception {
		zkServer = new TestingServer(2181);
		zkServer.start();

		zkPath = "/demo/ha";

		serverPort1 = 8080;
		serverPort2 = 8081;
		serverPort3 = 8082;

		server = new HaDemoServer(getProp(serverPort1));
		competitor1 = new HaDemoServer(getProp(serverPort2));
		competitor2 = new HaDemoServer(getProp(serverPort3));
	}

	private Properties getProp(int port) {
		Properties prop = new Properties();
		prop.put("zk_quorum", zkServer.getConnectString());
		prop.put("zk_path", zkPath);
		prop.put("host", "localhost");
		prop.put("port", Integer.toString(port));

		return prop;
	}

	@After
	public void tearDown() throws Exception {
		IoUtil.close(zkServer);
		IoUtil.close(server);
		IoUtil.close(competitor1);
		IoUtil.close(competitor2);
	}

	@Test
	public void test() throws Exception {
		final SocketAddress addr1 = new InetSocketAddress("localhost", serverPort1);
		final SocketAddress addr2 = new InetSocketAddress("localhost", serverPort2);
		final SocketAddress addr3 = new InetSocketAddress("localhost", serverPort3);

		server.start();

		Thread.sleep(1000);

		competitor1.start();
		competitor2.start();

		Thread.sleep(1000);

		Assert.assertEquals(ServerState.STARTED, server.getState());
		Assert.assertTrue(server.isLeader());
		Assert.assertFalse(competitor1.isLeader());
		Assert.assertFalse(competitor2.isLeader());

		checkConnection(addr1, true);
		checkConnection(addr2, false);
		checkConnection(addr3, false);

		//relinquish
		server.pause();

		Thread.sleep(1000);

		Assert.assertEquals(ServerState.PAUSED, server.getState());
		Assert.assertFalse(server.isLeader());
		checkConnection(addr1, false);

		Assert.assertTrue(competitor1.isLeader() || competitor2.isLeader());
		if (competitor1.isLeader()) {
			checkConnection(addr2, true);
			checkConnection(addr3, false);
		} else {
			checkConnection(addr2, false);
			checkConnection(addr3, true);
		}

		//relinquish
		server.resume();

		Thread.sleep(1000);

		Assert.assertEquals(ServerState.STARTED, server.getState());
		Assert.assertFalse(server.isLeader());
		checkConnection(addr1, false);

		Assert.assertTrue(competitor1.isLeader() || competitor2.isLeader());
		if (competitor1.isLeader()) {
			checkConnection(addr2, true);
			checkConnection(addr3, false);
		} else {
			checkConnection(addr2, false);
			checkConnection(addr3, true);
		}
	}
}
