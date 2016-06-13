/**
 * 
 */
package com.jcode.filequeue;

import java.io.File;
import java.io.IOException;

/**
 * @Desc 队列使用锁，如果存在该文件，表明队列正在使用中
 *
 * @Author daijunjie
 * @DateTime 2016年6月7日 下午12:28:42
 * 
 */
public class LockFile {
	public static final String fileName = "data.lock";

	public synchronized static boolean isLockFileExists(String filePath) {
		File file = new File(filePath + fileName);
		return file.exists();
	}

	public synchronized static void createLockFile(String filePath) throws IOException {
		File file = new File(filePath + fileName);
		if(!file.exists()) {
			file.createNewFile();
		}
	}

	public synchronized static void deleteLockFile(String filePath) throws IOException {
		File file = new File(filePath + fileName);
		file.delete();
	}

}
