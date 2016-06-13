/**
 * 
 */
package com.jcode.filequeue.exception;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年4月26日 下午3:17:19
 * 
 */
public class SerializationException extends FileQueueException {

	private static final long serialVersionUID = 1;

	public SerializationException() {
		super();
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(String message, Throwable t) {
		super(message, t);
	}

	public SerializationException(Throwable t) {
		super(t);
	}

}
