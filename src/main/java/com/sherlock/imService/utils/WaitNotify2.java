package com.sherlock.imService.utils;

public class WaitNotify2 {
	
	private static Object lock = new Object();
	
	private static boolean numberCanPrint = false;
	private static boolean charCanPrint = false;
	
	static class PrintNumber implements Runnable{
		@Override
		public void run() {
			synchronized(lock){
				
				int prints = 0;
				
				for(int i = 1; i < 53; i++){
					
					prints = prints + 1;
					
					if(prints > 2){
						prints = 0;
						i--;
						numberCanPrint = false;
					}
					else{
						numberCanPrint = true;
					}
					
					if(numberCanPrint){
						System.out.print(i);
						lock.notify();
					}
					else{
						try {
							lock.wait();
						} 
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	static class PrintChar implements Runnable{
		@Override
		public void run() {
			synchronized(lock){
				
				int prints = 0;
				
				for(int i = 65; i < 26+65; i++){
					prints = prints + 1;
					
					if(prints > 1){
						charCanPrint = false;
						prints = 0;
						i--;
					}
					else{
						charCanPrint = true;
					}
					
					if(charCanPrint){
						System.out.println((char)i);
						lock.notify();
					}
					else{
						try {
							lock.wait();
						} 
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new Thread(new PrintNumber(), "printNumber").start();
		new Thread(new PrintChar(), "printChar").start();
	}
}
