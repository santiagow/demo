package com.santiagow.util;

import com.google.gson.Gson;

/**
 *
 * @author Santiago Wang
 * @since 2016/6/19
 */
public final class CommonUtil {
	private static final ThreadLocal<Gson> LOCAL_GSON = new ThreadLocal<Gson>() {
		public Gson initialValue() {
			return new Gson();
		}
	};

	/**
	 * Get thread safe Gson instance.
	 *
	 * @return
	 */
	public static Gson getSafeGson() {
		return LOCAL_GSON.get();
	}
}
