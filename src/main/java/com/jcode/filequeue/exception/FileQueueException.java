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
public class FileQueueException extends RuntimeException {

	private static final long serialVersionUID = 1;

	public FileQueueException() {
		super();
	}

	public FileQueueException(String message) {
		super(message);
	}

	public FileQueueException(String message, Throwable t) {
		super(message, t);
	}

	public FileQueueException(Throwable t) {
		super(t);
	}

}
