package com.droideley.tools;

public class FinishedThreads {
	int count;
	
	public FinishedThreads() {
		count = 0;
	}
	
	public int getCount() {
		return count;
	}
	
	public synchronized void addCount() {
		count++;
	}

}
