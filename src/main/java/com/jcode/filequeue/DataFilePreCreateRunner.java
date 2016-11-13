package com.jcode.filequeue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @Desc 预创建数据文件，提供性能
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午3:21:27
 *
 */
public class DataFilePreCreateRunner extends Thread {
	private static final Logger log = LogManager.getLogger(DataFilePreCreateRunner.class);
	private FSManager fsManager;

	public DataFilePreCreateRunner(FSManager fsManager) {
		super("thread-DataFilePreCreateRunner");
		this.fsManager = fsManager;
	}

	@Override
	public void run() {
		try {
			//判断空间
			if(!fsManager.judgeNoEnoughSpace()) {
				int writeFileIndex = fsManager.getWriteFileIndex();
				int nextWriteFileIndex = writeFileIndex < Integer.MAX_VALUE ? writeFileIndex + 1 : 0;
				if(!DataFile.exists(fsManager.getFilePath(), nextWriteFileIndex)) {
					DataFile dataFile = new DataFile(fsManager.getFilePath(), nextWriteFileIndex, fsManager.getFileSize());
					dataFile.close();
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}

	public void shutdown() {
		this.interrupt();
	}
}
