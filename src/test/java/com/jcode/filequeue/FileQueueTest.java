/**
 * 
 */
package com.jcode.filequeue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jcode.filequeue.exception.FileQueueAlreadyInuseException;
import com.jcode.filequeue.serializer.JDKSerializer;

/**
 * @Desc
 *
 * @Author daijunjie
 * @DateTime 2016年6月12日 下午2:07:27
 * 
 */
public class FileQueueTest {

	static FileQueue<byte[]> queue;

	@BeforeClass
	public static void BeforeClass() {
//		 queue = new FileQueue<byte[]>("e:/temp/filequeue/");
	}

	@Test
	public void testQueueOffer() {
		queue.clear();
		System.out.println(queue.size());
		assertTrue(queue.size() == 0);

		queue.offer("a".getBytes());
		queue.offer("b".getBytes());
		assertTrue(queue.size() == 2);

		byte[] data = queue.poll();
		System.out.println(new String(data));
		assertTrue(queue.size() == 1);

	}

	@Test
	public void testQueuePoll() {
		queue.clear();

		System.out.println(queue.size());

		assertNull(queue.poll());

		queue.offer("abc".getBytes());

		byte[] data = queue.poll();

		assertEquals("abc", new String(data));
	}

	@Test
	public void testQueuePeek() {
		queue.clear();

		System.out.println(queue.size());

		assertNull(queue.peek());

		queue.offer("1".getBytes());
		queue.offer("2".getBytes());
		queue.offer("3".getBytes());

		assertEquals("1", new String(queue.peek()));
		assertEquals("1", new String(queue.peek()));
		assertEquals("1", new String(queue.peek()));
	}

	@Test
	public void testQueueRecovery() {
		queue.clear();
		queue.offer("abc".getBytes());
		queue.close();

		FileQueue<byte[]> queue2 = new FileQueue<byte[]>("e:/temp/filequeue/");

		assertEquals(1, queue2.size());

		byte[] data = queue2.poll();

		assertEquals("abc", new String(data));

	}

	@Test
	public void testMutilProcessExecute() throws Exception {
		final CountDownLatch latch = new CountDownLatch(2);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileQueue queue = new FileQueue("e:/temp/filequeue/");
				} catch (Exception e) {
					assertTrue(e instanceof FileQueueAlreadyInuseException);
					e.printStackTrace();
				}
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileQueue queue = new FileQueue("e:/temp/filequeue/");
				} catch (Exception e) {
					assertTrue(e instanceof FileQueueAlreadyInuseException);
					e.printStackTrace();
				}
				latch.countDown();
			}
		}).start();

		latch.await();
	}

	@Test
	public void testQueueSize() throws Exception {
		queue.clear();
		final CountDownLatch latch = new CountDownLatch(100);
		// 并发添加数据
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					queue.offer("test".getBytes());
					latch.countDown();
				}
			}).start();
		}

		latch.await();
		assertEquals(100, queue.size());
		System.out.println(queue.size());
	}

	@Test
	public void testQueueFileSize() throws Exception {
		queue.clear();
		final CountDownLatch latch = new CountDownLatch(100);
		// 并发添加数据
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					queue.offer("1".getBytes());
					latch.countDown();
				}
			}).start();
		}

		latch.await();
		System.out.println(queue.getQueueInfo());
		
		byte[] data = queue.poll();
		while (data != null) {
			System.out.println(new String(data));
			System.out.println("read:"+queue.getQueueInfo());
			data = queue.poll();
		}
		System.out.println(queue.getQueueInfo());
		
	}

	@Test
	public void testQueueClear() throws Exception {
		queue = new FileQueue<byte[]>("e:/temp/readWriteFileSeparation2/", 1024 * 1, JDKSerializer.build(), true);
		System.out.println(queue.getQueueInfo());
		for (int i = 0; i < 1000000000; i++) {
			queue.offer(("1234567890--" + i).getBytes());
			System.out.println("write:"+queue.getQueueInfo());
		}
		queue.clear();
		System.out.println(queue.getQueueInfo());
		assertEquals(0, queue.size());
	}
	
	
	@Test
	public void testReadWriteFileSeparation() throws Exception {
		queue = new FileQueue<byte[]>("e:/temp/readWriteFileSeparation/", 1024 * 1, JDKSerializer.build(), true);
//		queue.clear();
		System.out.println(queue.getQueueInfo());
		final CountDownLatch latch = new CountDownLatch(2);

		new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] data = queue.poll();
				while (data != null) {
					System.out.println(new String(data));
					System.out.println("read:"+queue.getQueueInfo());
					data = queue.poll();
				}
				latch.countDown();
			}
		}).start();
		
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 1000000; i++) {
					queue.offer(("1234567890--" + i).getBytes());
					System.out.println("write:"+queue.getQueueInfo());
				}
				latch.countDown();
			}
		}).start();
		
	
		latch.await();
		System.out.println(queue.size());

//		for (int i = 0; i < 1000; i++) {
//			System.out.println(new String(queue.poll()));
//			System.out.println(queue.getQueueInfo());
//		}
		
		System.out.println(queue.getQueueInfo());

	}
	
	
	@Test
	public void testReadWriteFileSeparation2() throws Exception {
		queue = new FileQueue<byte[]>("e:/temp/readWriteFileSeparation2/", 1024 * 1, JDKSerializer.build(), true);
//		queue.clear();
		System.out.println(queue.getQueueInfo());
		
		for (int i = 0; i < 10000; i++) {
			queue.offer(("1234567890--" + i).getBytes());
			System.out.println("write:"+queue.getQueueInfo());
		}
		
		System.out.println(queue.size());

		for (int i = 0; i < 10000; i++) {
			System.out.println(new String(queue.poll()));
			System.out.println(queue.getQueueInfo());
		}
		
		System.out.println(queue.getQueueInfo());

	}
	
	@Test
	public void testGetMetaInfo() throws Exception {
		System.out.println(MetaFile.getMetaInfo("e:/temp/readWriteFileSeparation/"));
	}
	
	
	
	
}
