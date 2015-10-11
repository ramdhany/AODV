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

import interfaces.IRouteDiscovery.IRouteDiscovery;


import java.net.*;

import log.Logging;

import aodvstate.*;



/**
* This class defines the thread that managers the release of buffered packets
* that were buffered when a route was being made.
*
* @author : Rajiv Ramdhany
* @date : 01-dec-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/

public class BufferMinder extends Thread {
	ConfigInfo cfgInfo;
	IRouteDiscovery  crtlComp;
	InetAddress destIPAddr;

	/**
	* Constructor to create an object and initialize
	* @param ConfigInfo cfg - config object
	* @param CurrentInfo cur - current info object
	* @param RouteManager rtm - route manager object
	* @param InetAddress dest - the detination IP address
	*/
	public BufferMinder(ConfigInfo cfg, IRouteDiscovery crtl, InetAddress dest) {
		cfgInfo = cfg;
		crtlComp = crtl;
		destIPAddr = dest;
	}

	/**
	* Method to start the Buffer minding thread. It will wait for the
	* given duration and then release the contents of the buffer.
	*/
	public void run() {

		try {

			// log
			crtlComp.getLog().write(Logging.INFO_LOGGING,
				"Buffer Minder - Buffer minder started");

			sleep(500);
			crtlComp.releaseBuffer(destIPAddr);

			// log
			crtlComp.getLog().write(Logging.INFO_LOGGING,
				"Buffer Minder - Buffer released and minder stopped");

		} catch(Exception e) {
			// the InterruptedException is due to user invoking
			// stop ; so dont consider it as an error
			if(!(e instanceof InterruptedException)) {
				// call log
				crtlComp.getLog().write(Logging.CRITICAL_LOGGING,
					"Buffer Minder - Buffer minder failed - " + e);
			}
		}
	}

	/**
	* Method to stop the Buffer minder
	*/
	public void terminate() {
		interrupt();
		return;
	}
}
