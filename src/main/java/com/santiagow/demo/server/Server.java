package com.santiagow.demo.server;

import java.io.Closeable;

/**
 * @author Santiago Wang
 * @since 2016/6/5
 */
public interface Server extends Closeable {
	//TODO restart?
	void start();

	void stop();

	void pause();

	void resume();

	ServerState getState();
}
