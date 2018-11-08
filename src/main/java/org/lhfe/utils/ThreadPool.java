package org.lhfe.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ThreadPool {

	public static ExecutorService eService;

	public static ExecutorService geteService() {
		if(eService==null){
			eService = Executors.newCachedThreadPool();
		}
		return eService;
	}
}
