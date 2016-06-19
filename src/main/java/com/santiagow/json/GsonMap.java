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
import java.util.Map;

/**
 * Created by Santiago on 2016/1/27.
 */
public class GsonMap implements JsonMap {
	private static final Logger LOG = LoggerFactory.getLogger(GsonArray.class);

	private final String json;
	private final Map<String, Object> internalMap;

	public GsonMap(String json) {
		if (StringUtils.isBlank(json)) {
			this.json = "";
			internalMap = Collections.emptyMap();

			return;
		}

		Gson gson = CommonUtil.getSafeGson();
		Type mapType = new TypeToken<Map<String, Object>>() {
		}.getType();

		Map<String, Object> tmpMap = null;
		try {
			tmpMap = gson.fromJson(json, mapType);
		} catch (JsonSyntaxException e) {
			LOG.error(e.getMessage(), e);
		}

		if (tmpMap != null) {
			this.json = json;
			internalMap = tmpMap;
		} else {
			this.json = "";
			internalMap = Collections.emptyMap();
		}
	}

	GsonMap(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			this.json = "";
			internalMap = Collections.emptyMap();

			return;
		}

		json = CommonUtil.getSafeGson().toJson(map);
		internalMap = map;
	}

	@Override
	public int optInt(String key) {
		return optInt(key, 0);
	}

	@Override
	public int optInt(String key, int defVal) {
		Object obj = internalMap.get(key);
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
	public long optLong(String key) {
		return optLong(key, 0);
	}

	@Override
	public long optLong(String key, long defVal) {
		Object obj = internalMap.get(key);
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
	public double optDouble(String key) {
		return optDouble(key, 0);
	}

	@Override
	public double optDouble(String key, double defVal) {
		Object obj = internalMap.get(key);
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
	public float optFloat(String key) {
		return optFloat(key, 0);
	}

	@Override
	public float optFloat(String key, float defVal) {
		Object obj = internalMap.get(key);
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
	public String optString(String key) {
		return optString(key, null);
	}

	@Override
	public String optString(String key, String defVal) {
		Object obj = internalMap.get(key);
		return obj == null ? defVal : obj.toString();
	}

	@Override
	public JsonArray optJsonArray(String key) {
		Object obj = internalMap.get(key);
		if (obj == null) {
			return null;
		}
		return new GsonArray(obj.toString());
	}

	@Override
	public JsonMap optJsonMap(String key) {
		Object obj = internalMap.get(key);
		if (obj == null || !(obj instanceof Map)) {
			return null;
		}

		return new GsonMap((Map) obj);
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public Map<String, Object> getInternalMap() {
		return Collections.unmodifiableMap(internalMap);
	}


	@Override
	public String toString() {
		return json;
	}
}
