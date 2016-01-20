package com.santiagow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by Santiago on 2016/1/20.
 */
public final class IoUtil {
	private static final Logger LOG = LoggerFactory.getLogger(IoUtil.class);

	public static void close(Closeable closer) {
		if (closer == null) {
			return;
		}

		try {
			closer.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	//absolute path
	public static String getContentFromPath(String path) {
		String wrapedPath = Thread.currentThread().getContextClassLoader().getResource(path).getPath();

		try {
			return getContentFromStream(new FileInputStream(new File(wrapedPath)));
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
		}

		return "";
	}

	public static String getContentFromStream(InputStream stream) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(stream));

			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return sb.toString();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			close(br);
		}

		return "";
	}
}
