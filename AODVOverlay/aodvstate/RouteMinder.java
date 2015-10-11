/*

AODV Overlay v0.5.3 Copyright 2007-2010  Lancaster University
Rajiv Ramdhany

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package aodvstate;

import interfaces.ILog.ILog;
import interfaces.IState.IAodvState;

import java.net.*;

import log.Logging;


/**
* This class defines the thread that managers the lifetime of a route.
*
* @author : Rajiv Ramdhany, Rajiv Ramdhany
* @date : 06-oct-2007, modified on 12/02/2007
* @email : r.ramdhany@lancaster.ac.uk, r.ramdhany@lancaster.ac.uk
*  
*
*/

public class RouteMinder extends Thread {
	ConfigInfo cfgInfo;
	ILog log;
	IAodvState pStateComp; // router state i.e. routing table
	InetAddress destIPAddr;
	int lifeTime;

	public RouteMinder(IAodvState rtstate, InetAddress da, int st) {
		cfgInfo = (ConfigInfo) rtstate.getConnectedConfigInfo();
		this.pStateComp = rtstate;
		destIPAddr = da;
		lifeTime = st;
		log = rtstate.getLog();
	}

	// in loop
	//	sleep for lifetime in route
	//	get route
	//	if lifetime expired
	//		call lifetime expired in route manager

	public void run() {

		try {

			// log
			log.write(Logging.INFO_LOGGING,
				"Route Minder - Route Minder started for destination " + destIPAddr.getHostAddress());

			// sleep for the 1st time
			sleep(lifeTime);

			while(true) {

				lifeTime = pStateComp.checkActiveRouteLifetime(destIPAddr, this);

				if(lifeTime <= 0)
					break;

				sleep(lifeTime);
			}

			// log
			log.write(Logging.INFO_LOGGING,
				"Route Minder - Route Minder terminated for destination " + destIPAddr.getHostAddress());


		} catch(Exception e) {
			// do not consider as error if the thread ended
			// due to the interrupt exception as this is done
			// purposely
			if(!(e instanceof InterruptedException)) {

				// log as error
				log.write(Logging.CRITICAL_LOGGING,
					"Route Minder - Route Minder failed -  " + e);
			}
		}
	}

	/**
	* Method to terminate the route minder
	*/
	public void terminate() {
		interrupt();
	}


}
