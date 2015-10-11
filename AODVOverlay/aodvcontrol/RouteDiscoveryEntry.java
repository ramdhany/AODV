/*

AODV Overlay v0.5.3 Copyright 2007-2010  Lancaster University

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

import interfaces.IState.IAodvState;

import java.net.*;
import java.util.*;

import msg.RREQ;

import aodvstate.ConfigInfo;


/**
* Objects of this class holds information ralated to a
* route discovery that is in progress.
*
* @author : Rajiv Ramdhany
* @date : 15-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
*/
public class RouteDiscoveryEntry {
	public ConfigInfo cfgInfo;
	public IAodvState stateComp;

	// Route discovery info
	public InetAddress destIPAddr;
	public RREQ rreq;
	public DiscoveryMinder rdThread;
	public ArrayList pktBuffer;
	public int rreqRetries;
	public int sleepTime;


	/**
	* Constructor to create a route discovery entry to be placed
	* in the list.
	* @param ConfigInfo cfg - config object
	* @param CurrentInfo cur - current object
	* @param InetAddress adr - Destination IP address for
	*			which a route is being searched
	* @param RREQ r - The RREQ sent
	* @param RouteDiscoverer rd - The Route Discovery thread
	*/
	public RouteDiscoveryEntry(ConfigInfo cfg, IAodvState cur, InetAddress adr,
					RREQ r, DiscoveryMinder rd, int st) {
		cfgInfo = cfg;
		stateComp = cur;

		destIPAddr = adr;
		rdThread = rd;
		rreq = r;
		pktBuffer = new ArrayList();
		rreqRetries = 0;
		sleepTime = st;
	}
}
