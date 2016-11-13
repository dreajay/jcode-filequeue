/*
 *  Copyright 2011 sunli [sunli1223@gmail.com][weibo.com@sunli1223]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.jcode.filequeue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @Desc 删除处理完成的数据文件
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午3:20:55
 *
 */
public class DataFileDeleteRunner extends Thread {
	private static final Logger log = LogManager.getLogger(DataFileDeleteRunner.class);
	private FSManager fsManager;
	private File queueFilePath;

	public DataFileDeleteRunner(FSManager fsManager) {
		super("thread-DataFileDeleteRunner");
		this.fsManager = fsManager;
		queueFilePath = new File(fsManager.getFilePath());
	}

	@Override
	public void run() {
		try {
			File[] files = queueFilePath.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.startsWith(DataFile.dataFileName)) {
						int index = Integer.parseInt(name.subSequence(name.indexOf("-") + 1, name.length()).toString());
						int readFileIndex = fsManager.getReadFileIndex();
						int writeFileIndex = fsManager.getWriteFileIndex();
						if (writeFileIndex >= readFileIndex) {
							if (index < readFileIndex) {
								return true;
							} else {
								return false;
							}
						} else {
							if (index > writeFileIndex + 1 && index < readFileIndex) {
								return true;
							} else {
								return false;
							}
						}
					} else {
						return false;
					}
				}
			});
			if (files != null && files.length > 0) {
				for (File file : files) {
					try {
						DataFile dataFile = new DataFile(file.getAbsolutePath(), fsManager.getFileSize());
						boolean enduse = dataFile.isEnduse();
						dataFile.close();
						if (enduse) {
							log.info("delete data file:" + file.getName());
							file.delete();
						}
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
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
