package com.santiagow.json;

import java.util.Map;

/**
 * Created by Santiago on 2016/1/26.
 */
public interface JsonMap {
	int optInt(String key);

	int optInt(String key, int defVal);

	long optLong(String key);

	long optLong(String key, long defVal);

	double optDouble(String key);

	double optDouble(String key, double defVal);

	float optFloat(String key);

	float optFloat(String key, float defVal);

	String optString(String key);

	String optString(String key, String defVal);

	JsonArray optJsonArray(String key);

	JsonMap optJsonMap(String key);

	boolean isEmpty();

	Map<String, Object> getInternalMap();
}
