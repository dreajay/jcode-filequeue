/**
 * 
 */
package com.jcode.filequeue.serializer;

import com.jcode.filequeue.exception.SerializationException;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午6:16:39
 * 
 */
public class ByteArraySerializer implements QueueSerializer<byte[]> {

	public ByteArraySerializer() {
	}

	public byte[] serialize(byte[] bytes) throws SerializationException {
		return bytes;
	}

	public byte[] deserialize(byte[] bytes) throws SerializationException {
		return bytes;
	}

	public static ByteArraySerializer build() {
		return new ByteArraySerializer();
	}
}