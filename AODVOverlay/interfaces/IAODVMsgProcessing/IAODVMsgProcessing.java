/*
 * IAODVMsgProcessing.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	14-Mar-2007
 * AUTHOR
 * 		Rajiv Ramdhany
 * LOG
 * 		Log: IAODVMsgProcessing.java, interfaces.IAODVMsgProcessing 
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

package interfaces.IAODVMsgProcessing;


import msg.IPPkt;
import msg.RERR;
import msg.RREP;
import msg.RREPACK;
import msg.RREQ;
import OpenCOM.IUnknown;

/**
 * @author Rajiv Ramdhany
 *
 */
public interface IAODVMsgProcessing extends IUnknown {
	
	/**
	* This method handles a RREQ messge received by the protocol handle. In
	* summary, either it will send a RREP or propogate the RREQ. Following
	* text describes the
	*
	*	create or update a route to the prev hop increase lifetime
	*		by ACTIVE_ROUTE_TIMEOUT (without a valid seq num, i.e. validDestSeqNumFlag = invalid
	* 	check RREQ prevously recvd (check from RREQ ID + orig ip list), if so drop
	*
	*	in RREQ increment hop count
	*	serach a route to the originator of RREQ, then add or update route
	*			(use orig seq num in RREQ to update, see FORMULA
	*			 set validDestSeqNumFlag = valid)
	*
	*	route lifetime to originator should be updated using FORMULA
	*
	*	check if i am the destnation, then RREP generated
	*	check if route to dest available AND active AND D flag is not set AND my dest seq num is valid
	*				AND my dest seq num >= to dest seq num in RREQ, then RREP generated
	*	if RREP generated
	*		if destination
	*			seq num FORMULA
	*			hop count = 0, lifetime = MY_ROUTE_TIME, prefix = 0, R flag 0,
	*			A flag from parametrs, rest from RREQ
	*			unicast send, to sender of RREQ (prev hop)
	*
	*		if not destination (intermediate)
	*			seq num = what is route entry
	*			hop count = what is route entry
	*			lifetime = what is route entry (route time - curr time)
	*			rest from RREQ
	*			unicast send, to sender of RREQ (prev hop)
	*			if G flag set in RREP (send RREP to destination)
	*				hop count = from route to originator
	*				dest ip adddress = originate of RREQ
	*				dest seq num = originator seq num of RREQ
	*				originator ip address = dest ip address
	*				lifetime = (route time - curr time) to originator
	*				unicast send to next hop to destination (from route)
	*
	*	if no RREP generated then
	*		check the TTL, should be > 1 else drop packet
	*		reduce TTL by 1
	*		place highest of dest seq considering RREQ and route to dest in my route list
	*		put RREQ ID + originator ip in list for RREQ minder with PATH_DISCOVERY_TIME
	*		propogate RREQ
	*
	* @param RREQ rreq - the RREQ to process
	* @exception Exception - exceptions thrown when error
	*/
	public void processAODVMsgRREQ(RREQ rreq) throws Exception;
	
	
	/**
	* This method is responsible for handling RREP messages received by the
	* node. The following is the procedure,
	*
	*		find route to prev hop (who sent RREP, i.e dest=prev hop)
	*		if not, create route without a valid seq num
	*		increment hop count in RREP
	*
	*		find route to dest
	*		if route found, compare dest seq num
	*			if seq num invalid in route
	*			   OR (dest seq in RREP > what is in route (2s comp) AND dest seq valid)
	*			   OR (seq == seq AND route is inactive route)
	*			   OR (seq num == seq num AND active route AND hop count in RREP is < hop count in route)
	*				update route
	*					do as (100)
	*		if route not found
	*			(100) create route - route flag = active, dest seq flag = valid,
	*				 next hop = src ip in RREP, hop count = hop count in RREP
	*				 expiry time = current time + lifetime in RREP
	*				 dest seq num = dest seq num of RREP
	*				 dest ip = dest ip in RREP
	*				 iface = iface from which RREP recvd
	*
	*
	*		if i am not originator of RREP
	*			find route to originator
	*			update lifetime of route to max of (existing lifetime, currtime + ACTIVE_ROUTE_TIMEOUT)
	*			update precursor list - using from the src ip from whom the RREP was
	*											recvd (i.e. next hop)
	*
	*			find route to dest
	*			update precuror list to dest (i.e. next hop to dest) - put ip of next hop to which RREP is
	*					 forwarded (not	ncessarily originator)
	*			send RREP to next hop to originator
	*
	*		if i am originator
	*			unicast RREP-ACK to dest
	*
	* @param RREP rrep - the RREP recived
	* @exception Exception - thrown for any error occured
	*/
	public void processAODVMsgRREP(RREP rrep) throws Exception;
	
	/**
	* RERR
	* compile list of routes to (unrechable dest in RERR
	* AND that have the sender of RERR as next hop)
	*
	* send RERR to all precursors of the above list
	*			{ copy dest seq from RERR
	*			  set route status = INVALID
	*			  set lifetime to DELETE_PERIOD
	*			  start route deleters for all }
	*
	*/
	public  void processAODVMsgRERR(RERR rerr) throws Exception;
	
	
	/**
	* Method to process HELLO messages received by this node.
	*
	* @param RREP rrep - HELLO message to process. A RREP becomes
	*			a HELLO message when it's
	*			DestIPAddr = OrigIPAddr and when it comes
	*			from a next hop
	* @exception Exception - thrown in case of errors
	*/
	public void processAODVMsgHELLO(RREP rrep) throws Exception;
	
	
	public void processAODVMsgRREPACK(RREPACK rrepack) throws Exception;
	
	public void processExistingRouteUse(IPPkt pkt) throws Exception;
	
	
	/**
	 * Method to initialise component member variables after component has been
	 * connected to the ConfigInfo component containing protocol parameters  
	 * 
	 */
	public void start() throws Exception;
	
	/**
	 * startRREQIDMinder starts the RREQID minder thread
	 * 
	 * @throws Exception
	 */
	public void startRREQIDMinder() throws Exception;
	
	/**
	 * 
	 * stopRREQIDMinder stops the RREQID minder thread
	 * 
	 * @throws Exception
	 */
	public void stopRREQIDMinder() throws Exception;
	
	
	

}
