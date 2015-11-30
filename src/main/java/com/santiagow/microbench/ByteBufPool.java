package com.santiagow.microbench;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ByteBufPool extends GenericObjectPool<ByteBuf>{

	public ByteBufPool(PooledObjectFactory<ByteBuf> factory, GenericObjectPoolConfig config) {
		super(factory, config);
	}
	
	
	public static class ByteBufFactory extends BasePooledObjectFactory<ByteBuf>	{
		private final int bufferSize;
		
		public ByteBufFactory(int bufferSize){
			this.bufferSize = bufferSize;
		}

		@Override
		public ByteBuf create() throws Exception {
			return PooledByteBufAllocator.DEFAULT.directBuffer(bufferSize, bufferSize);
		}

		@Override
		public PooledObject<ByteBuf> wrap(ByteBuf buf) {
			return new DefaultPooledObject<ByteBuf>(buf);
		}
	}
}
