/*
 * IRouteDiscovery.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	14-Mar-2007
 * AUTHOR
 * 		Rajiv Ramdhany
 * LOG
 * 		Log: IRouteDiscovery.java, interfaces.IRouteDiscovery 
 *
 * GridKit is a configurable and dynamically reconfigurable middleware for Grid and pervasive computing.
 * It is the intention that all individual elements (components and frameworks) are resuable within other
 * networking and middleware software. However, please retain this original license for individual component
 * re-use.
 *
 * Copyright (C) 2005 Paul Grace
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package interfaces.IRouteDiscovery;


import interfaces.ILog.ILog;

import java.net.InetAddress;

import msg.IPPkt;
import OpenCOM.IUnknown;

/**
 * @author Rajiv Ramdhany
 *
 */
public interface IRouteDiscovery extends IUnknown {
	
	/**
	* This method releases the packets in a buffer when given the
	* destination IP address. This method is called by the BufferMinder
	* that is responsible for releasing the buffer.
	*
	* @param InetAddress dest - Destination IP
	*/
	public void releaseBuffer(InetAddress dest) throws Exception;
	
	/**
	* Method to be called when the local(my) machine requires a route
	* to some destination. This method is called by the packet listener
	* when it receives a packet with the given destination MAC address.
	*
	* The following text defines the procedure
	*	find route to dest in table
	*	if found( found means (route status = valid AND expired) OR (route status = invalid) )
	*		(200) dest seq = last know seq num
	*		increment own seq num
	*		orig seq num = own num
	*		increment RREQ ID
	*		RREQ ID of originator = RREQ ID
	*		hop count = 0
	*		G flag from parameters
	*		D flag from parameters
	*		set TTL to (hop count of route + TTL_INCREMENT)
	*
	*	if not found
	*		do as (200)
	*		U flag = true
	*		set TTL to TTL_START
	*
	*	put RREQ ID + originator ip in list for RREQ minder with PATH_DISCOVERY_TIME
	*	multicast RREQ
	*	start route discoverer with NET_TRAVERSAL_TIME and RREQ packet
	*
	* @param IPPkt pkt - the packet for which a route was required
	*/
	public void processRouteDiscovery(IPPkt pkt) throws Exception;
	
	/**
	* Method to be called to resend a RREQ after the specified duration. This method
	* is called by the Route Discovering thread.
	*
	* @param int retries - the number of RREQ retries done
	* @param InetAddress destIP - the destination for which route being searched
	* @return int - returns > 0 if the route discovery should continue (this being
	*		the next wait time, or else 0 to stop route discovery
	* @exception Exception - due to any error when calling other methods
	*/
	public int continueRouteDiscovery(InetAddress destIP) throws Exception;
	
	/**
	 * @return Returns the log.
	 */
	public ILog getLog();

}
