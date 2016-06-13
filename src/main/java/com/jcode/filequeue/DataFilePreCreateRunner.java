package com.jcode.filequeue;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Desc 预创建数据文件，提供性能
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午3:21:27
 *
 */
public class DataFilePreCreateRunner extends Thread {
	private final Logger log = LoggerFactory.getLogger(DataFilePreCreateRunner.class);
	private FSManager fsManager;
	private File queueFilePath;

	private volatile boolean run;

	public DataFilePreCreateRunner(FSManager fsManager) {
		this.fsManager = fsManager;
		queueFilePath = new File(fsManager.getFilePath());
		run = true;
	}

	@Override
	public void run() {
		while (run) {
			try {
				int writeFileIndex = fsManager.getWriteFileIndex();
				int nextWriteFileIndex = writeFileIndex < Integer.MAX_VALUE ? writeFileIndex + 1 : 0;
				// 预先创建DataFile文件
				if (queueFilePath.getUsableSpace() < fsManager.getFileSize()) {
					log.error(fsManager.getFilePath() + " TotalSpace:{}G,UsableSpace:{}G,FreeSpace:{}G", new Object[] { queueFilePath.getTotalSpace() / 1024 / 1024 / 1024, queueFilePath.getUsableSpace() / 1024 / 1024 / 1024, queueFilePath.getFreeSpace() / 1024 / 1024 / 1024 });
				} else {
					if (run) {
						new DataFile(fsManager.getFilePath(), nextWriteFileIndex, fsManager.getFileSize()).close();
					} else {
						break;
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
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
