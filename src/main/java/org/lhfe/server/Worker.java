package org.lhfe.server;


public class Worker extends Thread
{
    private IJob job;
	public Worker(IJob job) {
		this.job = job;
	}
	public void run() {
	  job.execute();
	}
}