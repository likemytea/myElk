package org.lhfe.rabbitmq.producer;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.lhfe.common.Constants;

public class LocalQueRunner implements Runnable{
	static ConcurrentLinkedQueue<String> _messQue;
	@Override
	public void run() {
    	String[] qms;
    	String ms = null;
    	while(true){
    		try{
    			if(_messQue.size()==0){
    				continue;
    			}
    			System.out.println(" local que size:"+_messQue.size());
    			ms = _messQue.remove();
    			qms = splitStr(ms);
        		if(!BasicSenderLongConn.sendAndQueInMem(qms[0],qms[1],Constants.RBT_TRY_CONN)){
        			_messQue.add(ms);
        		}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    
		
	}
	
    /**
     * Cycle processing local que
     * */

    static String[] splitStr(String mess){
  	  String[] x = mess.split("&");
  	  String[] res = new String[2];
  	  res[0] = x[0];
  	  res[1] = mess.substring(x[0].length()+1, mess.length());
  	  return res;
    }
}
