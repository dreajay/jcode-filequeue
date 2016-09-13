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

	private volatile boolean run;

	public DataFilePreCreateRunner(FSManager fsManager) {
		this.fsManager = fsManager;
		run = true;
	}

	@Override
	public void run() {
		while (run) {
			try {
				if (run) {
					//判断空间
					if(fsManager.judgeNoEnoughSpace()) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							break;
						}
					} else {
						int writeFileIndex = fsManager.getWriteFileIndex();
						int nextWriteFileIndex = writeFileIndex < Integer.MAX_VALUE ? writeFileIndex + 1 : 0;
						if(!DataFile.exists(fsManager.getFilePath(), nextWriteFileIndex)) {
							DataFile dataFile = new DataFile(fsManager.getFilePath(), nextWriteFileIndex, fsManager.getFileSize());
							dataFile.close();
						}
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							break;
						}
					}
				} else {
					break;
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void shutdown() {
		run = false;
		this.interrupt();
	}
}
