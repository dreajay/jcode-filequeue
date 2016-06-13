/**
 * 
 */
package com.jcode.filequeue.serializer;

import com.jcode.filequeue.exception.SerializationException;

/**
 * @Desc 
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午6:12:38
 * 
 */
public interface QueueSerializer<E> {
	/**
	 * Serialize the given object to binary data.
	 * 
	 * @param t object to serialize
	 * @return the equivalent binary data
	 */
	byte[] serialize(E e) throws SerializationException;

	/**
	 * Deserialize an object from the given binary data.
	 * 
	 * @param bytes object binary representation
	 * @return the equivalent object instance
	 */
	E deserialize(byte[] bytes) throws SerializationException;
}
