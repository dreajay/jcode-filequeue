/**
 * 
 */
package com.jcode.filequeue;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcode.filequeue.exception.FileQueueAlreadyInuseException;
import com.jcode.filequeue.exception.FileQueueException;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月7日 下午12:28:42
 * 
 */
public class FSManager {

	private String filePath;

	private MetaFile metaFile;

	private DataFile writeFileHandler;

	private DataFile readFileHandler;

	private long fileSize;

	private DataFileDeleteRunner deleteRunner;
	private DataFilePreCreateRunner preCreateRunner;
	final Logger log = LoggerFactory.getLogger(FSManager.class);

	public FSManager(String filePath) throws IOException {
		this(filePath, DataFile.DEFAULT_FILE_SIZE, false);
	}

	public FSManager(String filePath, long fileSize) throws IOException {
		this(filePath, fileSize, false);
	}

	public FSManager(String filePath, long fileSize, boolean ignoreLock) throws IOException {
		this.filePath = filePath;
		this.fileSize = fileSize;
		File file = new File(filePath);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new FileQueueException("cannot create file queue with the path:"+filePath);
			}
		}
		if (!ignoreLock) {
			if (LockFile.isLockFileExists(filePath)) {
				throw new FileQueueAlreadyInuseException("the file queue with the path(" + filePath + ") is already in use.");
			}
			LockFile.createLockFile(filePath);
		}
		init();
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		metaFile = new MetaFile(filePath);
		writeFileHandler = new DataFile(filePath, metaFile.getWriteFileIndex(), fileSize);
		readFileHandler = new DataFile(filePath, metaFile.getReadFileIndex(), fileSize);
		deleteRunner = new DataFileDeleteRunner(this);
		deleteRunner.start();
		preCreateRunner = new DataFilePreCreateRunner(this);
		preCreateRunner.start();
	}

	public void put(byte[] data) throws IOException {
		int ret = writeFileHandler.write(data);
		// 文件已满，获取下一个
		if (ret == DataFile.FILE_FULL) {
			writeFileHandler.close();
			writeFileHandler = null;
			writeFileHandler = new DataFile(filePath, metaFile.getNextWriteFileIndex(), fileSize);
			writeFileHandler.write(data);
		}
		metaFile.incrementQueueSize();
		metaFile.addFileSize(4 + data.length);
	}

	public byte[] get() throws IOException {
		if (metaFile.getQueueSize() == 0) {
			return null;
		}
		byte[] data = readFileHandler.read();
		while (data == null && metaFile.getQueueSize() > 0) {
			int nextReadFileIndex = metaFile.getNextReadFileIndex();
			if (nextReadFileIndex < 0) {
				return null;
			} else {
				readFileHandler.enduse();
				readFileHandler.close();
				readFileHandler = new DataFile(filePath, nextReadFileIndex, fileSize);
				data = readFileHandler.read();
			}
		}
		if (data != null) {
			metaFile.decrementQueueSize();
			metaFile.addFileSize(-4 - data.length);
		}
		return data;
	}

	public byte[] peek() throws IOException {
		if (metaFile.getQueueSize() == 0) {
			return null;
		}
		byte[] data = readFileHandler.peek();
		while (data == null && metaFile.getQueueSize() > 0) {
			int nextReadFileIndex = metaFile.getNextReadFileIndex();
			if (nextReadFileIndex < 0) {
				return null;
			} else {
				readFileHandler.enduse();
				readFileHandler.close();
				readFileHandler = new DataFile(filePath, nextReadFileIndex, fileSize);
				data = readFileHandler.peek();
			}
		}
		return data;
	}

	public void reset() throws IOException {
		if (writeFileHandler != null) {
			writeFileHandler.close();
			writeFileHandler = null;
		}
		if (readFileHandler != null) {
			readFileHandler.close();
			readFileHandler = null;
		}
		if (metaFile != null) {
			metaFile.reset();
		}
		File[] files = new File(getFilePath()).listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.getName().startsWith(DataFile.dataFileName)) {
					log.debug("reset delete file:" + file.getName() + "=" + file.delete());
				}
			}
		}
		readFileHandler = new DataFile(filePath, metaFile.getReadFileIndex(), fileSize);
		writeFileHandler = new DataFile(filePath, metaFile.getWriteFileIndex(), fileSize);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getQueueSize() {
		return metaFile.getQueueSize();
	}

	public long getQueueFileSize() {
		return metaFile.getFileSize();
	}

	public int getReadFileIndex() {
		return metaFile.getReadFileIndex();
	}

	public int getWriteFileIndex() {
		return metaFile.getWriteFileIndex();
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getMetaInfo() {
		return metaFile.getMetaInfo();
	}

	public void close() throws IOException {
		if (deleteRunner != null) {
			deleteRunner.shutdown();
			deleteRunner = null;
		}
		if (preCreateRunner != null) {
			preCreateRunner.shutdown();
			preCreateRunner = null;
		}
		if (metaFile != null) {
			metaFile.close();
			metaFile = null;
		}
		if (writeFileHandler != null) {
			writeFileHandler.close();
			writeFileHandler = null;
		}
		if (readFileHandler != null) {
			readFileHandler.close();
			readFileHandler = null;
		}
		LockFile.deleteLockFile(filePath);
	}

}
