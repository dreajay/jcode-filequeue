/**
 * 
 */
package com.jcode.filequeue.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.jcode.filequeue.exception.SerializationException;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午6:16:39
 * 
 */
public class JDKSerializer implements QueueSerializer<Object> {

	public JDKSerializer() {
	}

	public byte[] serialize(Object obj) throws SerializationException {
		ByteArrayOutputStream os = null;
		ObjectOutputStream oos = null;
		try {
			os = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(obj);
			byte[] data = os.toByteArray();
			return data;
		} catch (IOException e) {
			throw new SerializationException(e.getMessage(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (IOException e) {
				}
			}
			if (oos != null) {
				try {
					oos.close();
					oos = null;
				} catch (IOException e) {
				}
			}
		}
	}

	public Object deserialize(byte[] data) throws SerializationException {
		ByteArrayInputStream os = null;
		ObjectInputStream ois = null;
		try {
			os = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(os);
			Object obj = ois.readObject();
			return obj;
		} catch (Exception e) {
			throw new SerializationException(e.getMessage(), e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
					ois = null;
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (IOException e) {
				}
			}
		}
	}

	public static JDKSerializer build() {
		return new JDKSerializer();
	}
}