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
* This is the minder that manages a route when in the delete
* period. Procedure
*
* 	sleep for DELETE period
* 	call lifetime expiry in route manager
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public class DeleteMinder extends Thread {
	IAodvState rtStateComp;
	InetAddress destIPAddr;
	int lifeTime;
	ConfigInfo cfgInfo;
	ILog log;

	public DeleteMinder(IAodvState rs, InetAddress da, int st) {
		cfgInfo = (ConfigInfo) rs.getConnectedConfigInfo(); //type cast necessary
		rtStateComp = rs;
		destIPAddr = da;
		lifeTime = st;
		this.log = rs.getLog();
	}

	// in loop
	//	sleep for lifetime in route
	//	get route
	//	if lifetime expired
	//		call delete route lifetime expired in route manager

	public void run() {

		try {
			// log
			log.write(Logging.INFO_LOGGING,
				"Delete Minder - Delete Minder started for destination "
					+ destIPAddr.getHostAddress());

			sleep(lifeTime);

			while(true) {

				lifeTime = rtStateComp.checkDeleteRouteLifetime(destIPAddr, this);

				if(lifeTime <= 0)
					break;

				sleep(lifeTime);
			}

			// log
			log.write(Logging.INFO_LOGGING,
				"Delete Minder - Delete Minder terminated for destination "
					+ destIPAddr.getHostAddress());


		} catch(Exception e) {
			// do not consider as error if the thread ended
			// due to the interrupt exception as this is done
			// purposely
			if(!(e instanceof InterruptedException)) {

				// log as error
				log.write(Logging.CRITICAL_LOGGING,
					"Delete Minder - Delete Minder failed -  " + e);
			}
		}
	}

	/**
	* Method to terminate the delete minder
	*/
	public void terminate() {
		interrupt();
	}
}

