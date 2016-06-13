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
public class MetaFileFormatException extends FileQueueException {

	private static final long serialVersionUID = 1;

	public MetaFileFormatException() {
		super();
	}

	public MetaFileFormatException(String message) {
		super(message);
	}

	public MetaFileFormatException(String message, Throwable t) {
		super(message, t);
	}

	public MetaFileFormatException(Throwable t) {
		super(t);
	}

}
