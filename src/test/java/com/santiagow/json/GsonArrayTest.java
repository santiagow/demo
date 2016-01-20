package com.santiagow.json;

import com.santiagow.util.IoUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Santiago on 2016/1/29.
 */
public class GsonArrayTest {
	private String json;
	private GsonArray gsonArray;

	@Before
	public void setup() {
		json = IoUtil.getContentFromPath("json/gsonarray.json");
		gsonArray = new GsonArray(json);
	}

	@Test
	public void test() {
		Assert.assertNotNull(gsonArray);

		Assert.assertEquals(5, gsonArray.optInt(0));
		Assert.assertEquals(1454083150260L, gsonArray.optLong(1));
		Assert.assertEquals(6.3, gsonArray.optDouble(2), 0.01);
		Assert.assertEquals(6.3f, gsonArray.optFloat(2), 0.01);
		Assert.assertEquals("6.3", gsonArray.optString(2));
		Assert.assertEquals("hello world", gsonArray.optString(3));
		Assert.assertNotNull(gsonArray.optString(4));
		Assert.assertNotNull(gsonArray.optJsonMap(4));
		Assert.assertTrue(gsonArray.optJsonArray(4).isEmpty());
		Assert.assertEquals(json, gsonArray.toString());
		Assert.assertFalse(gsonArray.isEmpty());
		Assert.assertEquals(6, gsonArray.getInternalList().size());
	}
}
