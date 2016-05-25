package com.santiagow.demo.ha;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by Santiago on 2016/1/14.
 */
public class HaManager {

	private CuratorFramework client;
	private LeaderSelector leaderSelector;
	private LeaderLatch leaderLatch;
	private String zkConnect;

	public void init() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.newClient(zkConnect, retryPolicy);

		LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
			//This method should not return until you wish to release leadership
			@Override
			public void takeLeadership(CuratorFramework client) throws Exception {
				//TODO
			}
		};
		String path = "";
		LeaderSelector leaderSelector = new LeaderSelector(client, path, listener);

		LeaderLatch latch = new LeaderLatch(client, path);
		latch.addListener(new LeaderLatchListener() {
			public void isLeader() {

			}

			public void notLeader() {

			}
		});
	}

	public void start() {
		client.start();
	}

	public void close() {
		if (client != null) {
			client.close();
		}
	}

	public static abstract class HaStateListener {
		private HaState state;
		public HaState getState() {
			return state;
		}

		abstract void stateChange(HaState newState);
	}

	public enum HaState{
		Master, Slave;
	}

}
