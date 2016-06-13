/**
 * 
 */
package com.jcode.filequeue.serializer;

import java.nio.charset.Charset;

import com.jcode.filequeue.exception.SerializationException;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午6:16:39
 * 
 */
public class StringSerializer implements QueueSerializer<String> {

	private final Charset charset;

	public StringSerializer() {
		this(Charset.forName("UTF8"));
	}

	public StringSerializer(Charset charset) {
		this.charset = charset;
	}

	public byte[] serialize(String string) throws SerializationException {
		return (string == null ? null : string.getBytes(charset));
	}

	public String deserialize(byte[] bytes) throws SerializationException {
		return (bytes == null ? null : new String(bytes, charset));
	}

	public static StringSerializer build() {
		return new StringSerializer();
	}
}