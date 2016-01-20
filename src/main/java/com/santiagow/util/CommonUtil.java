package com.santiagow.util;

import com.google.gson.Gson;

/**
 * Created by Santiago on 2016/1/27.
 */
public class CommonUtil {
	private static final ThreadLocal<Gson> LOCAL_GSON = new ThreadLocal<Gson>(){
		public Gson initialValue() {
			return new Gson();
		}
	};

	/**
	 * Get thread safe Gson instance.
	 *
	 * @return
	 */
	public static Gson getSafeGson(){
		return LOCAL_GSON.get();
	}
}
