package com.santiagow.json;

import com.santiagow.util.IoUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Santiago on 2016/1/29.
 */
public class GsonMapTest {
	private String json;
	private GsonMap gsonMap;

	@Before
	public void setup() {
		json = IoUtil.getContentFromPath("json/gsonmap.json");
		gsonMap = new GsonMap(json);
	}

	@Test
	public void test() {
		Assert.assertNotNull(gsonMap);

		Assert.assertEquals(556, gsonMap.optInt("int"));
		Assert.assertEquals(1454083150260L, gsonMap.optLong("long"));
		Assert.assertEquals(145408315.026, gsonMap.optDouble("double"), 0.0001);
		Assert.assertEquals(145408315.026f, gsonMap.optFloat("float"), 0.0001);
		Assert.assertEquals("hello world", gsonMap.optString("string"));
		Assert.assertNotNull(gsonMap.optString("map"));
		Assert.assertNotNull(gsonMap.optJsonMap("map"));
		Assert.assertTrue(gsonMap.optJsonArray("array").isEmpty());
		Assert.assertEquals(json, gsonMap.toString());
		Assert.assertFalse(gsonMap.isEmpty());
		Assert.assertEquals(10, gsonMap.getInternalMap().keySet().size());
	}
}
