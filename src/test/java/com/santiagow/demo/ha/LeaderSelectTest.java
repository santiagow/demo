package com.santiagow.demo.ha;

import com.santiagow.util.IoUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Santiago on 2016/1/18.
 */
public class LeaderSelectTest {
	private static final Logger LOG = LoggerFactory.getLogger(LeaderSelectTest.class);

	private TestingServer zkServer;
	private String zkPath;
	private List<Closeable> closerList = new ArrayList<Closeable>();

	@Before
	public void setup() throws Exception {
		zkServer = new TestingServer(2181);
		zkPath = "/ha/leaderselector";

		zkServer.start();
	}

	@After
	public void tearDown() throws Exception {
		IoUtil.close(zkServer);

		for (Closeable closer : closerList) {
			IoUtil.close(closer);
		}

		Thread.sleep(1000);
	}

	@Test
	public void testLeaderSelector() throws Exception {
		for (int i = 0; i < 3; i++) {
			final int id = i;
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			CuratorFramework client = CuratorFrameworkFactory
					.newClient(zkServer.getConnectString(), retryPolicy);
			client.start();
			closerList.add(client);

			LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
				//This method should not return until you wish to release leadership
				@Override
				public void takeLeadership(CuratorFramework client) throws Exception {
					try {
						takeZkLeadership(id);
						Thread.sleep(1000 * 10);
					} finally {
						loseZkLeadership(id);
					}
				}
			};
			LeaderSelector leaderSelector = new LeaderSelector(client, zkPath, listener);
			leaderSelector.autoRequeue();
			leaderSelector.start();
			closerList.add(leaderSelector);
		}

		Thread.sleep(1000 * 60 * 1);
		LOG.info("~ end testLeaderSelector @ {}", System.currentTimeMillis() / 1000);
	}

	@Test
	public void testLeaderLatch() throws Exception {
		//there is no better way for LeaderLatch to reuse instance for requeue
		for (int i = 0; i < 3; i++) {
			final int id = i;
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			CuratorFramework client = CuratorFrameworkFactory
					.newClient(zkServer.getConnectString(), retryPolicy);
			client.start();
			closerList.add(client);

			final LeaderLatch latch = new LeaderLatch(client, zkPath);
			latch.addListener(new LeaderLatchListener() {
				@Override
				public void isLeader() {
					if (latch.hasLeadership()) {
						takeZkLeadership(id);
					}
				}

				@Override
				public void notLeader() {
					if (!latch.hasLeadership()) {
						loseZkLeadership(id);
					}
				}
			});
			latch.start();
			closerList.add(latch);
		}

		Thread.sleep(1000 * 60);
	}

	private void loseZkLeadership(int id) {
		LOG.info(":( {} - Lose leadership @ {}", id, System.currentTimeMillis() / 1000);
	}

	private void takeZkLeadership(int id) {
		LOG.warn("^^ {} - Take leadership @ {}", id, System.currentTimeMillis() / 1000);
	}
}