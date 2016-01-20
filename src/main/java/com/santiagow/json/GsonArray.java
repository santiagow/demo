package com.santiagow.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.santiagow.util.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Created by Santiago on 2016/1/27.
 */
public class GsonArray implements JsonArray {
	private static final Logger LOG = LoggerFactory.getLogger(GsonArray.class);

	private final String json;
	private final List<Object> internalList;

	public GsonArray(String json) {
		if (StringUtils.isBlank(json)) {
			this.json = "";
			internalList = Collections.emptyList();

			return;
		}

		Gson gson = CommonUtil.getSafeGson();
		Type listType = new TypeToken<List<Object>>() {
		}.getType();

		List<Object> tmpList = null;
		try {
			tmpList = gson.fromJson(json, listType);
		} catch (JsonSyntaxException e) {
			LOG.error(e.getMessage(), e);
		}

		if (tmpList != null) {
			this.json = json;
			internalList = tmpList;
		} else {
			this.json = "";
			internalList = Collections.emptyList();
		}
	}

	@Override
	public int optInt(int index) {
		return optInt(index, 0);
	}

	@Override
	public int optInt(int index, int defVal) {
		Object obj = internalList.get(index);
		if (obj instanceof Number) {
			return ((Number) obj).intValue();
		} else if (obj instanceof String) {
			try {
				return Integer.parseInt((String) obj);
			} catch (NumberFormatException e) {
				return defVal;
			}
		} else {
			return defVal;
		}
	}

	@Override
	public long optLong(int index) {
		return optLong(index, 0);
	}

	@Override
	public long optLong(int index, long defVal) {
		Object obj = internalList.get(index);
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		} else if (obj instanceof String) {
			try {
				return Long.parseLong((String) obj);
			} catch (NumberFormatException e) {
				return defVal;
			}
		} else {
			return defVal;
		}
	}

	@Override
	public double optDouble(int index) {
		return optDouble(index, 0);
	}

	@Override
	public double optDouble(int index, double defVal) {
		Object obj = internalList.get(index);
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else if (obj instanceof String) {
			try {
				return Double.parseDouble((String) obj);
			} catch (NumberFormatException e) {
				return defVal;
			}
		} else {
			return defVal;
		}
	}

	@Override
	public float optFloat(int index) {
		return optFloat(index, 0);
	}

	@Override
	public float optFloat(int index, float defVal) {
		Object obj = internalList.get(index);
		if (obj instanceof Number) {
			return ((Number) obj).floatValue();
		} else if (obj instanceof String) {
			try {
				return Float.parseFloat((String) obj);
			} catch (NumberFormatException e) {
				return defVal;
			}
		} else {
			return defVal;
		}
	}

	@Override
	public String optString(int index) {
		return optString(index, null);
	}

	@Override
	public String optString(int index, String defVal) {
		Object obj = internalList.get(index);
		return obj == null ? defVal : obj.toString();
	}

	@Override
	public JsonMap optJsonMap(int index) {
		Object obj = internalList.get(index);
		if (obj == null) {
			return null;
		}
		return new GsonMap(obj.toString());
	}

	@Override
	public JsonArray optJsonArray(int index) {
		Object obj = internalList.get(index);
		if (obj == null) {
			return null;
		}
		return new GsonArray(obj.toString());
	}

	@Override
	public boolean isEmpty() {
		return internalList.isEmpty();
	}

	@Override
	public int length() {
		return internalList.size();
	}

	@Override
	public List<Object> getInternalList() {
		return Collections.unmodifiableList(internalList);
	}

	@Override
	public String toString() {
		return json;
	}
}
