package aodvstate;

import interfaces.IHello.IHello;
import interfaces.ILog.ILog;
import interfaces.IState.IAodvState;

import java.net.InetAddress;



/**
* This class defines the thread that managers the lifetime of hello messages
* that are received for a given route.
*
* @author : Rajiv Ramdhany
* @date : 15-dec-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/

public class HelloReceiptMinder extends Thread {
	IAodvState pStateComp;
	InetAddress destIPAddr;
	int lifeTime;

	public HelloReceiptMinder(IAodvState cur, InetAddress da, int st) {
		pStateComp = cur;
		destIPAddr = da;
		lifeTime = st;
	}

	// in loop
	//	sleep for lifetime given in hello expiry
	// 	then call method in route manager to check
	// 	expiry

	public void run() {

		try {

			// log
			pStateComp.getLog().write(ILog.INFO_LOGGING,
				"Hello Receipt Minder - Hello Receipt Minder started for destination "
						+ destIPAddr.getHostAddress());

			// sleep for the 1st time
			sleep(lifeTime);
			
			IHello pStateHello = (IHello) pStateComp.QueryInterface("interfaces.IHello.IHello");
			
			while(true) {
				
				lifeTime = pStateHello.checkHelloReceived(destIPAddr, this);

				if(lifeTime <= 0)
					break;

				sleep(lifeTime);
			}

			// log
			pStateComp.getLog().write(ILog.INFO_LOGGING,
				"Hello Receipt Minder - Hello Receipt Minder terminated for destination "
						+ destIPAddr.getHostAddress());


		} catch(Exception e) {
			// do not consider as error if the thread ended
			// due to the interrupt exception as this is done
			// purposely
			if(!(e instanceof InterruptedException)) {

				// log as error
				pStateComp.getLog().write(ILog.CRITICAL_LOGGING,
					"Hello Receipt Minder - Hello Receipt Minder failed -  " + e);
			}
		}
	}

	/**
	* Method to terminate the hello receipt minder
	*/
	public void terminate() {
		interrupt();
	}
}
