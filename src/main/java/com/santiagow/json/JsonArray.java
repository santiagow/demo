package com.santiagow.json;

import java.util.List;

/**
 * Created by Santiago on 2016/1/26.
 */
public interface JsonArray {
	int optInt(int index);

	int optInt(int index, int defVal);

	long optLong(int index);

	long optLong(int index, long defVal);

	double optDouble(int index);

	double optDouble(int index, double defVal);

	float optFloat(int index);

	float optFloat(int index, float defVal);

	String optString(int index);

	String optString(int index, String defVal);

	JsonMap optJsonMap(int index);

	JsonArray optJsonArray(int index);

	boolean isEmpty();

	int length();

	//TODO: support iteration

	List<Object> getInternalList();
}
