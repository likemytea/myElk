package org.lhfe.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.lhfe.utils.ThreadPool;



public class Tasks extends Thread
{
  public static ConcurrentLinkedQueue<IJob> _jobQueue;
  public void run() {
    IJob job = null;
    while (true) {
      if (_jobQueue==null||_jobQueue.size() == 0) {
        //log.info("task sleeping");
        try {
          TimeUnit.MILLISECONDS.sleep(50L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      else {
    	while(!_jobQueue.isEmpty()) {
            job = _jobQueue.remove();
            //每个处理客户端的连接开启一个独立的线程，避免因某个终端的延迟响应引起整个server回应处理堵塞。
            Worker worker = new Worker(job);
            ThreadPool.geteService().execute(worker);
        }
      }
    }
  }
}