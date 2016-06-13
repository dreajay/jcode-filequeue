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
public class FileQueueAlreadyInuseException extends FileQueueException {

	private static final long serialVersionUID = 1;

	public FileQueueAlreadyInuseException() {
		super();
	}

	public FileQueueAlreadyInuseException(String message) {
		super(message);
	}

	public FileQueueAlreadyInuseException(String message, Throwable t) {
		super(message, t);
	}

	public FileQueueAlreadyInuseException(Throwable t) {
		super(t);
	}

}
