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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月7日 下午12:28:42
 * 
 */
public class DataFile {
	private RandomAccessFile randomAccessFile;
	private FileChannel channel;
	private MappedByteBuffer mappedByteBuffer;
	private long fileSize;
	/** 创建文件锁 */
	private Object fileLock = new Object();

	public static final byte inuse = 1;
	public static final byte enduse = 0;

	public static final int FILE_FULL = -1;
	public static final int SUCCESS = 0;

	public static final String dataFileName = "data-";

	/** 1.52G */
	public static final long DEFAULT_MAX_FILE_SIZE = Integer.MAX_VALUE - (500L * 1024L * 1024L);
	/** 100M */
	public static final long DEFAULT_FILE_SIZE = 1024L*1024L*100;

	final Logger log = LoggerFactory.getLogger(DataFile.class);

	public DataFile(String filePath, int fileIndex, long fileSize) throws IOException {
		this(filePath + dataFileName + fileIndex, fileSize);
	}

	public DataFile(String fullFilePath) throws IOException {
		this(fullFilePath, DEFAULT_FILE_SIZE);
	}

	public DataFile(String fullFilePath, long fileSize) throws IOException {
		if (fileSize < DEFAULT_FILE_SIZE) {
			fileSize = DEFAULT_FILE_SIZE;
		}
		if (fileSize > DEFAULT_MAX_FILE_SIZE) {
			fileSize = DEFAULT_MAX_FILE_SIZE;
		}
		this.fileSize = fileSize;
		File file = new File(fullFilePath);
		// 文件不存在，进行创建
		synchronized (fileLock) {
			if (file.exists()) {
				randomAccessFile = new RandomAccessFile(file, "rwd");
				channel = randomAccessFile.getChannel();
				mappedByteBuffer = channel.map(MapMode.READ_WRITE, 0, fileSize);
			} else {
				log.debug("create new data file:" + file.getAbsolutePath());
				file.createNewFile();
				randomAccessFile = new RandomAccessFile(file, "rwd");
				channel = randomAccessFile.getChannel();
				mappedByteBuffer = channel.map(MapMode.READ_WRITE, 0, fileSize);
				/** 1 字节 0*/
				mappedByteBuffer.put(inuse);// 使用标志
				/** 4 字节 1-4*/
				mappedByteBuffer.putInt(9);// 读文件位置
				/** 4 字节 5-8*/
				mappedByteBuffer.putInt(9);// 写文件位置
				mappedByteBuffer.force();
			}
		}
	}

	public static boolean isDataFileExists(String filePath, int fileIndex) {
		File file = new File(filePath + dataFileName + fileIndex);
		return file.exists();
	}

	public byte[] read() {
		int readFilePosition = getReadFilePosition();
		if (readFilePosition >= getWriteFilePosition()) {
			return null;
		}
		// 读取内容
		mappedByteBuffer.position(readFilePosition);
		int dataLength = mappedByteBuffer.getInt();
		byte[] data = new byte[dataLength];
		mappedByteBuffer.get(data);
		// 更新读位置
		setReadFilePosition(readFilePosition + 4 + dataLength);
		return data;
	}

	public byte[] peek() {
		int readFilePosition = getReadFilePosition();
		if (readFilePosition >= getWriteFilePosition()) {
			return null;
		}
		// 读取内容
		mappedByteBuffer.position(readFilePosition);
		int dataLength = mappedByteBuffer.getInt();
		byte[] data = new byte[dataLength];
		mappedByteBuffer.get(data);
		return data;
	}

	public int write(byte[] data) {
		int writeFilePosition = getWriteFilePosition();
		int incrementLength = 4 + data.length;
		if (writeFilePosition + incrementLength >= fileSize) {
			// 写满
			return FILE_FULL;
		}
		// 写数据
		mappedByteBuffer.position(writeFilePosition);
		mappedByteBuffer.putInt(data.length);
		mappedByteBuffer.put(data);
		// 更新写位置
		setWriteFilePosition(writeFilePosition + incrementLength);
		return SUCCESS;
	}

	public int getReadFilePosition() {
		mappedByteBuffer.position(1);
		return mappedByteBuffer.getInt();
	}

	public void setReadFilePosition(int readFilePosition) {
		mappedByteBuffer.position(1);
		mappedByteBuffer.putInt(readFilePosition);
	}

	public int getWriteFilePosition() {
		mappedByteBuffer.position(5);
		return mappedByteBuffer.getInt();
	}

	public void setWriteFilePosition(int writeFilePosition) {
		mappedByteBuffer.position(5);
		mappedByteBuffer.putInt(writeFilePosition);
	}

	/**
	 * 更新文件使用完毕状态
	 */
	public void enduse() {
		mappedByteBuffer.position(0);
		mappedByteBuffer.put(enduse);
	}

	/**
	 * 更新文件使用完毕状态
	 */
	public boolean isEnduse() {
		mappedByteBuffer.position(0);
		return enduse == mappedByteBuffer.get();
	}

	
	public void close() {
		try {
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
	
}
