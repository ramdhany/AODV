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

package aodvcontrol;

import interfaces.ILog.ILog;

import java.util.*;
import java.net.*;

import aodvstate.ConfigInfo;

/**
* This class defines the thread that managers the removal of RREQID entries
* placed in the list.  The first RREQID entry in a list is considered to be
* the entry with the lowest expiry time
*
* @author : Rajiv Ramdhany
* @date : 12-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/

public class RREQIDMinder extends Thread {
	ConfigInfo cfgInfo;
	AODVControl pCrtlComp;
	

	/**
	* Constructor to create an object and initialize
	* @param ConfigInfo cfg - config object
	* @param CurrentInfo cur - current info object
	* @param RouteManager rtm - route manager object
	*/
	public RREQIDMinder(ConfigInfo cfg, AODVControl crtlComp) {
		cfgInfo = cfg;
		pCrtlComp = crtlComp;
	}

	/**
	* Method to start the RREQID minding thread. Since this extends
	* Thread class the call will call the start method
	*/
	public void run() {
		RREQIDEntry first;
		int sleepTime, defaultSleepTime;
		InetAddress adr;
		int idNum;
		
		ILog log = pCrtlComp.getLog();
		
		try {
			

			
			// when no entries in RREQID list, use half of the PATH_DISCOVERY_TIME
			// to sleep
			defaultSleepTime = cfgInfo.pathDiscoveryTimeVal / 2;

			// log
			log.write(ILog.INFO_LOGGING,
				"RREQ ID Minder - RREQ ID minder started");
			
			boolean routerActive = cfgInfo.RouterActive;  

			while(routerActive) {
				// get first entry in RREQID list (it will
				// have the smallest expiry time)
				first = pCrtlComp.getFirstRREQID();

				// if no entry in RREQID list use the
				// default time (this time is half
				// of PATH_DISCOVERY_TIME
				if(first == null) {
					sleepTime = defaultSleepTime;
					adr = null;
					idNum = 0;
				} else {
					adr = first.origIPAddr;
					idNum = first.RREQIDNum;
					sleepTime = (int) (first.expiryTime - (new Date()).getTime());
				}

				// if sllep time is positive, wait for
				// sleep time
				if(sleepTime > 0) {
					sleep(sleepTime);
				}

				if(first != null) {
					pCrtlComp.removeRREQID(adr, idNum);

					// log
					log.write(ILog.INFO_LOGGING,
						"RREQ ID Minder - RREQ ID removed for " + adr.getHostAddress()
						+ "-" + idNum);
				}
				
				// check if router is active
				routerActive = cfgInfo.RouterActive;  
			}

			// log
			log.write(ILog.INFO_LOGGING,
				"RREQ ID Minder - RREQ ID minder terminated");


		} catch(Exception e) {
			// the InterruptedException is due to user invoking
			// stop ; so dont consider it as an error
			if(!(e instanceof InterruptedException)) {
				// call log
				log.write(ILog.CRITICAL_LOGGING,
					"RREQ ID Minder - RREQ ID minder failed - " + e);
			}
		}
	}

	/**
	* Method to stop the RREQID minder
	*/
	public void terminate() {
		interrupt();
		return;
	}
}
