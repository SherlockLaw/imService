package com.sherlock.imService.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsTest {
	private static CountDownLatch latch = new CountDownLatch(1);

	public static void main(String[] args) {
		Thread1_1 t1_1 = new Thread1_1();
		Thread1_2 t1_2 = new Thread1_2();
		t1_1.init(t1_2);
		t1_2.init(t1_1);
		t1_1.start();
		t1_2.start();
//		Thread2_1 t2_1 = new Thread2_1();
//		Thread2_2 t2_2 = new Thread2_2();
//		t2_1.init(t2_2);
//		t2_2.init(t2_1);
//		t2_1.start();
//		t2_2.start();
	}

	private static abstract class CommonThread1 extends Thread {
		private Semaphore sem = new Semaphore(1);
		protected CommonThread1 otherThread;
		public void init(CommonThread1 otherThread) {
			this.otherThread = otherThread;
		}
		public void acquire() throws InterruptedException{
			sem.acquire();
		}
		public void release(){
			sem.release();
		}
	}
	
	private static class Thread1_1 extends CommonThread1 {
		private int max = 52;
		private int i = 1;

		@Override
		public void run() {
			try {
				otherThread.acquire();
				latch.countDown();
				while (i <= max) {
					acquire();
					System.out.print(i++);
					if (otherThread.isAlive() && (i&1)!=0) {
						otherThread.release();
					} else {
						release();
					}
				}
				otherThread.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static class Thread1_2 extends CommonThread1 {
		private char max = 'Z';
		private char i = 'A';

		@Override
		public void run() {
			try {
				latch.await();
				while (i <= max) {
					acquire();
					System.out.print(i++);
					if (otherThread.isAlive()) {
						otherThread.release();
					} else {
						release();
					}
				}
//				otherThread.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static AtomicInteger tt = new AtomicInteger(0);
	private static abstract class CommonThread2 extends Thread {
		protected CommonThread2 otherThread;
		public void init(CommonThread2 otherThread) {
			this.otherThread = otherThread;
		}
	}
	private static class Thread2_1 extends CommonThread2 {
		private int max = 52;
		private int i = 1;
		@Override
		public void run() {
			while (i <= max) {
				if (tt.get() < 2) {
					System.out.print(i++);
					if (otherThread.isAlive()) {
						tt.incrementAndGet();
					}
				}
				try {
					Thread.sleep(1l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class Thread2_2 extends CommonThread2 {
		private char max = 'Z';
		private char i = 'A';
		@Override
		public void run() {
			while (i <= max) {
				if (tt.get() == 2) {
					System.out.print(i++);
					if (otherThread.isAlive()) {
						tt.set(0);
					}
				}
				try {
					Thread.sleep(1l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
