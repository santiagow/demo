package com.santiagow.microbench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;









import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.santiagow.microbench.ByteBufPool.ByteBufFactory;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class ByteBufMicroBench {
	final private static long ITERATIONS = 5000000000L; 
	final private static long WARMUP = 10000000L; 
	final private static long NANOS_PER_MS = 1000L * 1000L; 
	
	private static byte[][] datas = new  byte[4][];
	static {
		datas[0] = getContentFromPath("json/test.1.json").getBytes();
		datas[1] = getContentFromPath("json/test.2.json").getBytes();
		datas[2] = getContentFromPath("json/test.3.json").getBytes();
		datas[3] = getContentFromPath("json/test.4.json").getBytes();
	}

	private static final Random rand = new Random(32);

	private static final int CONTENT_LENGTH = 65536;

	@SuppressWarnings("resource")
	public static String getContentFromPath(String path){
		String wrapedPath = Thread.currentThread().getContextClassLoader().getResource(path).getPath();

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(wrapedPath))));

			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null){
				sb.append(line);
			}

			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public static long doTest(ByteBufPool pool, long n) throws Exception{
		long start = System.nanoTime(); 
		for (long i = 0; i < n; i++) { 
			testPool(pool); 
			Thread.sleep(1);
		} 
		long end = System.nanoTime(); 
		return end - start; 
	}

	public static void testPool(ByteBufPool pool) throws Exception{
		ByteBuf buf = pool.borrowObject();

		buf.writeBytes(datas[rand.nextInt(4)%4]);
		if (buf.readableBytes() > CONTENT_LENGTH - 2048) {
			ByteBuf buf2 = buf.copy();
			
			String str = buf2.toString(CharsetUtil.UTF_8);
			
			System.out.print(str.length() + " - ");
			
			buf.clear();
			pool.returnObject(buf);
		} else {
			pool.returnObject(buf);
		}
	}
	
	private static void printStats(long n, long nanos) {
		float itrsPerMs = 0;
		float millis = nanos/NANOS_PER_MS;
		if (millis != 0) {
			itrsPerMs = n/(nanos/NANOS_PER_MS);
		}

		System.err.println("    Elapsed time in ms -> " + millis);
		System.err.println("    Iterations / ms ----> " + itrsPerMs);
	}

	public static void main(String[] args) throws Exception{
		/*for(byte[] bytes: datas){
			System.out.println(new String(bytes));
		}*/

		ByteBufFactory factory = new ByteBufFactory(CONTENT_LENGTH);
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(50);
		//		config.setMaxIdle(1);
		ByteBufPool pool = new ByteBufPool(factory, config );

		System.err.println("Warming up ..."); 
		long nanos = doTest(pool, WARMUP);
		System.err.println("1st warm up done."); 
		printStats(WARMUP, nanos); 
		System.err.println("Starting 2nd warmup ..."); 
		nanos = doTest(pool, WARMUP);
		System.err.println("2nd warm up done."); 
		printStats(WARMUP, nanos); 
		System.err.println("Starting measurement interval ..."); 
		nanos = doTest(pool, WARMUP);
		System.err.println("Measurement interval done."); 
		System.err.println("Test complete."); 
		printStats(ITERATIONS, nanos); 
	}
}
