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
public class FileQueueInitException extends FileQueueException {

	private static final long serialVersionUID = 1;

	public FileQueueInitException() {
		super();
	}

	public FileQueueInitException(String message) {
		super(message);
	}

	public FileQueueInitException(String message, Throwable t) {
		super(message, t);
	}

	public FileQueueInitException(Throwable t) {
		super(t);
	}

}
