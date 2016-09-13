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
public class NoEnoughSpaceException extends FileQueueException {

	private static final long serialVersionUID = 1;

	public NoEnoughSpaceException() {
		super();
	}

	public NoEnoughSpaceException(String message) {
		super(message);
	}

	public NoEnoughSpaceException(String message, Throwable t) {
		super(message, t);
	}

	public NoEnoughSpaceException(Throwable t) {
		super(t);
	}

}
