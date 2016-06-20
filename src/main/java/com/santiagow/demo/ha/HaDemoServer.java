package com.santiagow.demo.ha;

import com.santiagow.demo.http.WebServer;
import com.santiagow.demo.server.Server;
import com.santiagow.demo.server.ServerState;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;

/**
 * @author Santiago Wang
 * @since 2016/6/5
 */
public class HaDemoServer implements Server {
	private Logger LOG = LoggerFactory.getLogger(HaDemoServer.class);

	private final String zkQuorum;
	private final String zkPath;
	private CuratorFramework client;
	private LeaderSelector leaderSelector;
	private LeaderSelectorListener listener;
	private ServerState serverState = ServerState.NONE;
	private volatile boolean isLeader = false;
	private WebServer webServer;
	private final SocketAddress addr;

	public HaDemoServer(String zkQuorum, String zkPath) {
		this.zkQuorum = zkQuorum;
		this.zkPath = zkPath;
		this.addr = new InetSocketAddress("localhost", 8080);

		init();
	}

	public HaDemoServer(Properties prop) {
		//TODO: use yaml
		this.zkQuorum = prop.getProperty("zk_quorum");
		this.zkPath = prop.getProperty("zk_path");

		String hostName = prop.getProperty("host", "");
		int port = Integer.parseInt(prop.getProperty("port"));
		this.addr = new InetSocketAddress(hostName, port);

		init();
	}

	public void init() {
		client = CuratorFrameworkFactory.newClient(zkQuorum, new ExponentialBackoffRetry(1000, 3));

		listener = new LeaderSelectorListenerAdapter() {
			@Override
			public void takeLeadership(CuratorFramework client) throws Exception {
				// we are now the leader. This method should not return until we want to relinquish leadership
				actMaster();

				try {
					while (serverState == ServerState.STARTING || serverState == ServerState.STARTED) {
						Thread.sleep(100);
					}
				} finally {
					actSlave();
				}
			}
		};
		leaderSelector = new LeaderSelector(client, zkPath, listener);

		webServer = new WebServer(addr);
	}

	//TODO: maybe should make this finish in short time
	public void actMaster() {
		isLeader = true;
		webServer.start();
	}

	public void actSlave() {
		isLeader = false;
		webServer.pause();
	}

	@Override
	public void start() {
		if (serverState == ServerState.STARTING || serverState == ServerState.STARTED) {
			LOG.warn("Already at work!");
			return;
		}

		serverState = ServerState.STARTING;

		client.start();
		leaderSelector.start();

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

		serverState = ServerState.STOPPED;
	}

	@Override
	public void pause() {
		if (serverState == ServerState.PAUSING || serverState == ServerState.PAUSED) {
			LOG.warn("Already paused!");
			return;
		}

		serverState = ServerState.PAUSED;
	}

	@Override
	public void resume() {
		if (serverState == ServerState.STARTING || serverState == ServerState.STARTED) {
			LOG.warn("Already at work!");
			return;
		}

		serverState = ServerState.STARTED;
	}

	@Override
	public void close() {
		leaderSelector.close();
		client.close();

		webServer.close();
	}

	@Override
	public ServerState getState() {
		return serverState;
	}

	public boolean isLeader() {
		return isLeader;
	}

	// work mode:
	//   try to take leader, if success, then act as master, else act as slave
	//   use http to start services, use HaManager to manage services which support HA mode
	public static void main(String[] args) throws InterruptedException {
		HaDemoServer server = new HaDemoServer("localhost:2181", "/demo/ha");
		server.start();

		Thread.sleep(10 * 60 * 1000);

		server.stop();
	}
}
