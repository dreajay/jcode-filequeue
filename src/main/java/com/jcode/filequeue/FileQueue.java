/**
 * 
 */
package com.jcode.filequeue;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jcode.filequeue.exception.FileQueueException;
import com.jcode.filequeue.serializer.JDKSerializer;
import com.jcode.filequeue.serializer.QueueSerializer;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月7日 下午12:28:42
 * 
 */
public class FileQueue<E> extends AbstractQueue<E> implements Queue<E>, java.io.Serializable {
	private static final long serialVersionUID = -5960741434564940154L;
	private FSManager fsManager;
	private Lock lock = new ReentrantReadWriteLock().writeLock();
	protected QueueSerializer<E> serializer;
	protected static JDKSerializer default_serializer = new JDKSerializer();
	private volatile boolean closed = false;
	/** 文件队列锁，防止并发处理同一文件 */
	private static Object fileQueueLock = new Object();

	private static final Logger log = LogManager.getLogger(FileQueue.class);

	public FileQueue(String path) {
		this(path, -1L, default_serializer, false);
	}

	public FileQueue(String path, QueueSerializer serializer) {
		this(path, -1L, serializer, false);
	}
	
	public FileQueue(String path, long fileSize) {
		this(path, fileSize, default_serializer, false);
	}

	public FileQueue(String path, long fileSize, QueueSerializer serializer) {
		this(path, fileSize, serializer, false);
	}

	public FileQueue(String path, long fileSize, QueueSerializer serializer, boolean ignoreLock) {
		synchronized (fileQueueLock) {
			log.info("create File Queue:" + path);
			try {
				fsManager = new FSManager(path, fileSize, ignoreLock);
			} catch (FileQueueException e) {
				throw e;
			} catch (IOException e) {
				throw new FileQueueException("file queue create error:"+e.getMessage(), e);
			}
			addShutdownHook();
			log.info("create File Queue success...");
		}
		this.serializer = serializer;
	}

	/**
	 * 关闭钩子
	 */
	public void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				close();
			}
		}));
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException("iterator Unsupported now");
	}

	@Override
	public int size() {
		return fsManager.getQueueSize();
	}

	@Override
	public boolean offer(E e) {
		if(e == null) {
			throw new NullPointerException();
		}
		try {
			lock.lock();
			fsManager.put(serializer.serialize(e));
			return true;
		} catch (Exception ex) {
			log.error(ex.getMessage(),ex);
			return false;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E peek() {
		try {
			lock.lock();
			byte[] data = fsManager.peek();
			if (data == null) {
				return null;
			}
			return (E) serializer.deserialize(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
			lock.unlock();
		}

	}

	@Override
	public E poll() {
		try {
			lock.lock();
			byte[] data = fsManager.get();
			if (data == null) {
				return null;
			}
			return (E) serializer.deserialize(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		try {
			lock.lock();
			fsManager.reset();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			lock.unlock();
		}
	}

	public String getQueueInfo() {
		try {
			lock.lock();
			return fsManager.getMetaInfo();
		} finally {
			lock.unlock();
		}
	}

	public static String getQueueInfo(String path) {
		return MetaFile.getMetaInfo(path);
	}
	
	public void close() {
		try {
			lock.lock();
			if(closed) {
				return;
			}
			closed = true;
			log.info("close File Queue ...");
			if (fsManager != null) {
				try {
					fsManager.close();
				} catch (IOException e) {
					log.error("close file queue error:"+e.getMessage(), e);
				}
				fsManager = null;
			}
			log.info("close File Queue success...");
		} finally {
			lock.unlock();
		}
	}
}
