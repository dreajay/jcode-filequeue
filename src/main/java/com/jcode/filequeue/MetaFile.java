/**
 * 
 */
package com.jcode.filequeue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jcode.filequeue.exception.MetaFileFormatException;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月7日 下午12:28:42
 * 
 */
public class MetaFile {
	/** 队列大小 4byte 0-3 */
	private AtomicInteger queueSize;
	/** 文件大小 8byte 4-11 */
	private AtomicLong fileSize;
	/** 读文件索引 4byte 12-15 */
	private AtomicInteger readFileIndex;
	/** 写文件索引 4byte 16-19 */
	private AtomicInteger writeFileIndex;

	private static final int metaFileSize = 20;
	public static final String metaFileName = "data.meta";
	private RandomAccessFile randomAccessFile;
	private FileChannel channel;
	private MappedByteBuffer mappedByteBuffer;

	public static final Logger log = LogManager.getLogger(MetaFile.class);

	public MetaFile(String filePath) throws IOException {
		File metaFile = new File(filePath + metaFileName);
		if (metaFile.exists()) {
			log.info("meta file exists");
			randomAccessFile = new RandomAccessFile(metaFile, "rwd");
			if (randomAccessFile.length() != metaFileSize) {
				throw new MetaFileFormatException("the file queue meta file length is wrong,file format is not correct.");
			}
			channel = randomAccessFile.getChannel();
			mappedByteBuffer = channel.map(MapMode.READ_WRITE, 0, this.metaFileSize);
			mappedByteBuffer.position(0);
			this.queueSize = new AtomicInteger(mappedByteBuffer.getInt());
			this.fileSize = new AtomicLong(mappedByteBuffer.getLong());
			this.readFileIndex = new AtomicInteger(mappedByteBuffer.getInt());
			this.writeFileIndex = new AtomicInteger(mappedByteBuffer.getInt());
		} else {
			log.info("file queue initialization, create the meta file.");
			metaFile.createNewFile();
			randomAccessFile = new RandomAccessFile(metaFile, "rwd");
			channel = randomAccessFile.getChannel();
			mappedByteBuffer = channel.map(MapMode.READ_WRITE, 0, this.metaFileSize);
			mappedByteBuffer.putInt(0);// 队列大小
			mappedByteBuffer.putLong(0L);// 文件大小
			mappedByteBuffer.putInt(0);// 读文件索引
			mappedByteBuffer.putInt(0);// 写文件索引
			mappedByteBuffer.force();
			this.queueSize = new AtomicInteger(0);
			this.fileSize = new AtomicLong(0L);
			this.readFileIndex = new AtomicInteger(0);
			this.writeFileIndex = new AtomicInteger(0);
		}
	}

	public void reset() {
		mappedByteBuffer.position(0);
		mappedByteBuffer.putInt(0);// 队列大小
		mappedByteBuffer.putLong(0L);// 文件大小
		mappedByteBuffer.putInt(0);// 读文件索引
		mappedByteBuffer.putInt(0);// 写文件索引
		mappedByteBuffer.force();
		this.queueSize = new AtomicInteger(0);
		this.fileSize = new AtomicLong(0L);
		this.readFileIndex = new AtomicInteger(0);
		this.writeFileIndex = new AtomicInteger(0);
	}
	
	/**
	 * 获取队列Size
	 * 
	 * @return
	 */
	public int getQueueSize() {
		return queueSize.get();
	}

	/**
	 * 增加队列Size
	 */
	public void incrementQueueSize() {
		mappedByteBuffer.position(0);
		mappedByteBuffer.putInt(queueSize.incrementAndGet());
	}

	/**
	 * 减少队列Size
	 */
	public void decrementQueueSize() {
		mappedByteBuffer.position(0);
		int size = queueSize.decrementAndGet();
		if (size < 0) {
			mappedByteBuffer.putInt(0);
			queueSize.set(0);
		} else {
			mappedByteBuffer.putInt(size);
		}
	}

	/**
	 * 获取队列所占空间大小
	 * 
	 * @return
	 */
	public long getFileSize() {
		return fileSize.get();
	}

	/**
	 * 添加队列所占空间
	 * 
	 * @param fileSize
	 */
	public void addFileSize(long fileSize) {
		mappedByteBuffer.position(4);
		mappedByteBuffer.putLong(this.fileSize.addAndGet(fileSize));
	}

	/**
	 * 获取当前读文件索引
	 * 
	 * @return
	 */
	public int getReadFileIndex() {
		return readFileIndex.get();
	}

	/**
	 * 判断是否有下一个读文件索引
	 * 
	 * @return
	 */
	public boolean hasNextReadFileIndex() {
		return readFileIndex.get() == writeFileIndex.get() ? false : true;
	}

	/**
	 * 获取下个读文件索引，如果小于0，表明没有下一个读文件
	 * 
	 * @return
	 */
	public int getNextReadFileIndex() {
		int currentReadFileIndex = readFileIndex.get();
		int currentWriteFileIndex = writeFileIndex.get();
		if (currentReadFileIndex == currentWriteFileIndex) {
			return -1;
		} else if (Integer.MAX_VALUE == currentReadFileIndex) {
			mappedByteBuffer.position(12);
			mappedByteBuffer.putInt(0);
			readFileIndex.set(0);
			return 0;
		} else {
			int index = readFileIndex.incrementAndGet();
			mappedByteBuffer.position(12);
			mappedByteBuffer.putInt(index);
			return index;
		}
	}

	/**
	 * 获取当前写文件索引
	 * 
	 * @return
	 */
	public int getWriteFileIndex() {
		return writeFileIndex.get();
	}

	/**
	 * 获取下一个写文件索引
	 * 
	 * @return
	 */
	public int getNextWriteFileIndex() {
		int currentWriteFileIndex = writeFileIndex.get();
		if (Integer.MAX_VALUE == currentWriteFileIndex) {
			mappedByteBuffer.position(16);
			mappedByteBuffer.putInt(0);
			writeFileIndex.set(0);
			return 0;
		} else {
			int index = writeFileIndex.incrementAndGet();
			mappedByteBuffer.position(16);
			mappedByteBuffer.putInt(index);
			return index;
		}
	}

	/**
	 * 关闭
	 */
	public void close() {
		try {
			if (mappedByteBuffer == null) {
				return ;
			}
			mappedByteBuffer.force();
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					try {
						Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
						getCleanerMethod.setAccessible(true);
						sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer, new Object[0]);
						cleaner.clean();
					} catch (Exception e) {
						log.error("close logindexy file error:", e);
					} 
					return null;
				}
			});
			if (channel != null) {
				channel.close();
				channel = null;
			}
			if (randomAccessFile != null) {
				randomAccessFile.close();
				randomAccessFile = null;
			}
			if (mappedByteBuffer != null) {
				mappedByteBuffer = null;
			}
		} catch (IOException e) {
			log.error("close logindex file error:", e);
		}
	}

	static String getMetaInfo(String filePath) {
		MetaFile metaFile = null;
		try {
			metaFile = new MetaFile(filePath);
			String info = metaFile.toString();
			return info;
		} catch (IOException e) {
			return "";
		} finally {
			if (metaFile != null) {
				metaFile.close();
			}
		}
	}

	static void setMetaInfo(String filePath, int queueSize, int fileSize, int readFileIndex, int writeFileIndex) {
		MetaFile metaFile = null;
		try {
			metaFile = new MetaFile(filePath);
			metaFile.mappedByteBuffer.putInt(queueSize);// 队列大小
			metaFile.mappedByteBuffer.putInt(fileSize);// 文件大小
			metaFile.mappedByteBuffer.putInt(readFileIndex);// 读文件索引
			metaFile.mappedByteBuffer.putInt(writeFileIndex);// 写文件索引
			metaFile.mappedByteBuffer.force();
		} catch (IOException e) {
		} finally {
			if (metaFile != null) {
				metaFile.close();
			}
		}
	}

	public String getMetaInfo() {
		return toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("queueSize=").append(getQueueSize())//
				.append(", fileSize=").append(getFileSize())//
				.append(", readFileIndex=").append(getReadFileIndex())//
				.append(", writeFileIndex=").append(getWriteFileIndex());
		return sb.toString();
	}
}
