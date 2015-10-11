package aodvcontrol;

import interfaces.ILog.ILog;
import interfaces.IRouteDiscovery.IRouteDiscovery;

import java.net.InetAddress;

import aodvstate.ConfigInfo;


/**
* This class defines the thread that managers the route discovery process. A single
* thread gets activated for each request for route discovery. The procedur is as
* follows,
*
*	// retries = 1
*	// sleep-time = NET_TRAVEERSAL_TIME
*	// in loop
*	//	sleep for sleep-time
*	//	check if route has been made (made = route entry AND valid AND not expired)
*	//	if made
*	//		stop thread
*	//	if not made
*	//		retries = retries + 1
*	//		if retries > RREQ_RETRIES
*	//			stop thread
*	//		increment TTL in RREQ by TTL_INCREMENT
*	//		if TTL is now > TTL_THRESHHOLD
*	//			set TTL = NET_DIAMETER
*	//		increment RREQ ID by 1
*	// 		put RREQ ID + originator ip in list for RREQ minder with PATH_DISCOVERY_TIME
*	// 		multicast RREQ
*	//		sleep-time = sleep-time * 2
*
*
* @author : Rajiv Ramdhany
* @date : 18-aug-2006
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public class DiscoveryMinder extends Thread {
	public ConfigInfo cfgInfo;
	public IRouteDiscovery pCrtlComp;

	public InetAddress destIPAddr;
	public int sleepTime;

	/**
	* Constructor to create an initialize the thread
	*
	* @param ConfigInfo cfg - config info object
	* @param CurrentInfo cur - current info object
	* @param RouteManager rm - route manger to get work done
	* @param InetAddress da - the destination for which a route is
	*				required
	*/
	public DiscoveryMinder(ConfigInfo cfg, IRouteDiscovery controlComp, InetAddress da, int st) {
		cfgInfo = cfg;
		pCrtlComp = controlComp;
		sleepTime = st;
		destIPAddr = da;
	}

	/**
	* Method to start the thread to perform route discovery
	*/
	public void run() {

		try {

			// log
			pCrtlComp.getLog().write(ILog.INFO_LOGGING,
				"Route Discoverer - Route Discoverer started for destination " + destIPAddr.getHostAddress());

			sleep(sleepTime);

			sleepTime = pCrtlComp.continueRouteDiscovery(destIPAddr);

			while(sleepTime > 0) {

				sleep(sleepTime);

				sleepTime = pCrtlComp.continueRouteDiscovery(destIPAddr);
			}

			// log
			pCrtlComp.getLog().write(ILog.INFO_LOGGING,
				"Route Discoverer - Route Discoverer terminated for destination " + destIPAddr.getHostAddress());

		} catch(Exception e) {
			// do not consider as error if the thread ended
			// due to the interrupt exception as this is done
			// purposely
			if(!(e instanceof InterruptedException)) {

				// log as error
				pCrtlComp.getLog().write(ILog.CRITICAL_LOGGING,
					"Route Discoverer - Route Discoverer failed -  " + e);

			}
		}
	}

	/**
	* Method to terminate the route discovery process
	*/
	public void terminate() {
		interrupt();
	}
}
