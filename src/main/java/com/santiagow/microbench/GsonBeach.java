package com.santiagow.microbench;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;

import java.lang.reflect.Type;
import java.util.Map;

import static com.santiagow.util.IoUtil.getContentFromPath;

public class GsonBeach {
	private static final long ITERATIONS = 50L * 1000 * 1000; //5000L * 1000 * 1000;
	private static final long WARMUP = 3 * 1000L * 1000; //10L * 1000 * 1000;
	private static final long NANOS_PER_MS = 1000L * 1000L;

	private static final Gson gson = new Gson();
	//private static final Type dpListType = new TypeToken<List<DataPoint>>() {}.getType();
	private static final Type mapType = new TypeToken<Map<String, Object>>() {
	}.getType();
	private static String[] datas = null;

	private static void initial() {
		loadDatas();
	}

	private static void loadDatas() {
		datas = new String[]{
				getContentFromPath("json/test.1.json"),
				getContentFromPath("json/test.2.json"),
				getContentFromPath("json/test.3.json"),
				getContentFromPath("json/test.4.json")};
	}

	public static long doTestDeserialize(long n) throws Exception {
		long start = System.nanoTime();
		for (long i = 0; i < n; i++) {
			testDeserialize();
		}
		long end = System.nanoTime();
		return end - start;
	}

	public static void testDeserialize() throws Exception {
		for (String data : datas) {
			Map<String, Object> resMap = gson.fromJson(data, mapType);
			Assert.assertFalse(resMap.isEmpty());
		}
	}

	public static long doTestSerialize(long n) throws Exception {
		long start = System.nanoTime();
		for (long i = 0; i < n; i++) {
			testSerialize();
		}
		long end = System.nanoTime();
		return end - start;
	}

	public static void testSerialize() throws Exception {
		//TODO
	}

	private static void printStats(long n, long nanos) {
		float itrsPerMs = 0;
		float millis = nanos / NANOS_PER_MS;
		if (millis != 0) {
			itrsPerMs = n / (nanos / NANOS_PER_MS);
		}

		System.err.println("    Elapsed time in ms -> " + millis);
		System.err.println("    Iterations / ms ----> " + itrsPerMs);
	}

	public static void main(String[] args) throws Exception {
		//init
		initial();

		//1st warn up
		System.err.println("Warming up ...");
		long nanos = doTestDeserialize(WARMUP);
		System.err.println("1st warm up done.");
		printStats(WARMUP, nanos);

		//2nd warn up
		System.err.println("Starting 2nd warmup ...");
		nanos = doTestDeserialize(WARMUP);
		System.err.println("2nd warm up done.");
		printStats(WARMUP, nanos);

		//measurement
		System.err.println("Starting measurement interval ...");
		nanos = doTestDeserialize(ITERATIONS);
		System.err.println("Measurement interval done.");
		System.err.println("Test complete.");
		printStats(ITERATIONS, nanos);
	}
}


//Warming up ...
//		1st warm up done.
//		Elapsed time in ms -> 49361.0
//		Iterations / ms ----> 60.0
//		Starting 2nd warmup ...
//		2nd warm up done.
//		Elapsed time in ms -> 48750.0
//		Iterations / ms ----> 61.0
//		Starting measurement interval ...
//		Measurement interval done.
//		Test complete.
//		Elapsed time in ms -> 793592.0
//		Iterations / ms ----> 63.0

//Warming up ...
//		1st warm up done.
//		Elapsed time in ms -> 47670.0
//		Iterations / ms ----> 62.0
//		Starting 2nd warmup ...
//		2nd warm up done.
//		Elapsed time in ms -> 48117.0
//		Iterations / ms ----> 62.0
//		Starting measurement interval ...
//		Measurement interval done.
//		Test complete.
//		Elapsed time in ms -> 797708.0
//		Iterations / ms ----> 62.0

//Warming up ...
//		1st warm up done.
//		Elapsed time in ms -> 46936.0
//		Iterations / ms ----> 63.0
//		Starting 2nd warmup ...
//		2nd warm up done.
//		Elapsed time in ms -> 46556.0
//		Iterations / ms ----> 64.0
//		Starting measurement interval ...
//		Measurement interval done.
//		Test complete.
//		Elapsed time in ms -> 776280.0
//		Iterations / ms ----> 64.0