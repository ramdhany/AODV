/*
 * AODVControl.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	08-Feb-2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: AODVControl.java, AODVControl 
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

package aodvcontrol;

import interfaces.IAODVMsgProcessing.IAODVMsgProcessing;
import interfaces.IConfigInfo.IConfigInfo;
import interfaces.IControl.IControl;
import interfaces.IGui.IGui;
import interfaces.IHello.IHello;
import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;
import interfaces.IPacketSender.IPacketSender;
import interfaces.IRouteDiscovery.IRouteDiscovery;
import interfaces.IState.IAodvState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import log.Logging;
import msg.IPPkt;
import msg.RERR;
import msg.RREP;
import msg.RREPACK;
import msg.RREQ;
import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import aodvstate.ConfigInfo;
import aodvstate.DeleteMinder;
import aodvstate.HelloReceiptMinder;
import aodvstate.RouteEntry;
import aodvstate.RouteMinder;

import common.ResultCode.ResultCode;

import exceptions.RoutingCFException;

/**
 * @author ramdhany
 *
 */
public class AODVControl extends OpenCOMComponent implements IControl, IUnknown,
IAODVMsgProcessing, IRouteDiscovery,
IMetaInterface, ILifeCycle, IConnections {

	public RouteDiscoveryList rdList;		// to buffer packets requiring route discovery
	public RREQIDList idList;
	public RREQIDMinder idMinder;
	public ConfigInfo cfgInfo;
	public Thread pktListener;
	
	private static AODVControl instance;
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo;
	public OCM_SingleReceptacle<IAodvState> m_PSR_IAodvState;
	public OCM_SingleReceptacle<ILog> m_PSR_ILog;
	public OCM_SingleReceptacle<IPacketSender> m_PSR_IPktSender; 	//Requires Interface of type IPacketSender.
	public OCM_SingleReceptacle<IGui> m_PSR_IGui; 					// To connect to Logging component
	public OCM_SingleReceptacle<IOSOperations> m_PSR_IOSOperations;
	

	public AODVControl(IUnknown pIOCM) {
		super(pIOCM);
		// create route discovery buffer
		rdList = new RouteDiscoveryList(this);

		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_IAodvState = new OCM_SingleReceptacle<IAodvState>(IAodvState.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
		m_PSR_IPktSender = new OCM_SingleReceptacle<IPacketSender>(IPacketSender.class);
		m_PSR_IGui = new OCM_SingleReceptacle<IGui>(IGui.class);
		m_PSR_IOSOperations = new OCM_SingleReceptacle<IOSOperations>(IOSOperations.class);
	}

	public static AODVControl Instance(IUnknown mpIOCM)
	{
		// Use 'Lazy initialization'
		if (instance == null)
		{
			instance = new AODVControl(mpIOCM);
		}
		return instance;
	}

	/**
	 * Method to initialise component member variables after component has been
	 * connected to the ConfigInfo component containing protocol parameters  
	 * 
	 */
	public void start() throws Exception{

		cfgInfo = null;
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}

		idList = new RREQIDList(cfgInfo);
		idMinder = new RREQIDMinder(cfgInfo, this);
		
	}



	// -------------------- IAODVMsgProcessing Interface ------------------
	/**
	 * Method to process HELLO messages received by this node.
	 *
	 * @param RREP rrep - HELLO message to process. A RREP becomes
	 *			a HELLO message when it's
	 *			DestIPAddr = OrigIPAddr and when it comes
	 *			from a next hop
	 * @exception Exception - thrown in case of errors
	 */
	public synchronized void processAODVMsgHELLO(RREP rrep) throws Exception {
		RouteEntry prevHopRte;

		// get ref to state component
		IAodvState aodvStateCompRef = m_PSR_IAodvState.m_pIntf;

		// log
		m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING,
				"Route Manager - HELLO Received "+  rrep.toString());

		// if no active routes exist, don't react to HELLOs
		if(!aodvStateCompRef.doUnexpiredRoutesExist()) {
			return;
		}

		// find route to prev hop (who sent RREP, i.e dest=prev hop)
		// if not, create route with a valid seq num
		prevHopRte = aodvStateCompRef.getRoute(rrep.fromIPAddr);


		// if no route found, means no active
		// route for this destination, so dont do anything
		if(prevHopRte == null) {
			return;
		}

		prevHopRte.nextHelloReceiveTime = (new Date()).getTime() + rrep.lifeTime;
		prevHopRte.destSeqNum = rrep.destSeqNum;
		prevHopRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_VALID;

		// start the thread, if not started already
		if(prevHopRte.helloReceiptMinder == null) {
			prevHopRte.helloReceiptMinder = new HelloReceiptMinder(
					(IAodvState) getConnectedSinkComp(m_PSR_IAodvState), 
					rrep.fromIPAddr, rrep.lifeTime);
			prevHopRte.helloReceiptMinder.start();
		}

		m_PSR_IAodvState.m_pIntf.updateRouteTable(rrep.fromIPAddr, prevHopRte);
	}

	/**
		// RERR
		// compile list of routes to (unreachable dest in RERR
		//	AND that have the sender of RERR as next hop)
		//
		// send RERR to all precursors of the above list
		//			{ copy dest seq from RERR
		//			  set route status = INVALID
		//			  set lifetime to DELETE_PERIOD
		//			  start route deleters for all }
		//
	 */
	public synchronized void processAODVMsgRERR(RERR rerr) throws Exception {
		ArrayList unreachableIPList;
		ArrayList unreachableSeqList;
		RouteEntry entry;
		int i, j;
		InetAddress adrList[];
		int seqList[];
		RERR newRERR;

		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		//	get ref to state component
		// the delegator to the state component is actually returned
		IAodvState aodvStateCompRef = m_PSR_IAodvState.m_pIntf;

		// if RERR unicast, send a RERR to each precursor in a
		// invalidating route
		if(cfgInfo.RERRSendingModeVal == ConfigInfo.RERR_UNICAST_VAL) {

			adrList = new InetAddress[1];
			seqList = new int[1];

			for(i = 0; i < rerr.destCount; i++) {
				entry = aodvStateCompRef.getRoute(rerr.destIPAddr[i]);

				// regenerate RERR only if the nexthop of this route is the
				// sender of the RERR but dont remove the route to the
				// sender of the RERR
				if(entry != null
						&& entry.nextHopIPAddr.equals(rerr.fromIPAddr)
						&& !entry.destIPAddr.equals(rerr.fromIPAddr)) {

					entry.destSeqNum = rerr.destSeqNum[i];

					adrList[0] = entry.destIPAddr;
					seqList[0] = entry.destSeqNum;

					// regenerate RERR to each precursor
					for(j = 0; j < entry.precursorList.size(); j++) {
						newRERR = new RERR(cfgInfo, aodvStateCompRef, false,
								(InetAddress) entry.precursorList.get(j), (short) 1,
								false, (byte) 1, adrList, seqList);

						m_PSR_IPktSender.m_pIntf.sendMessage(newRERR);
					}

					// invalidate route & start route delete
					entry.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_INVALID;
					entry.expiryTime = (new Date()).getTime()
					+ cfgInfo.deletePeriodVal;
					entry.activeMinder = new DeleteMinder(
							(IAodvState) getConnectedSinkComp(m_PSR_IAodvState) , // the actual component, not the delegator
							entry.destIPAddr, cfgInfo.deletePeriodVal);

					entry.activeMinder.start();

					aodvStateCompRef.updateRouteTable(entry.destIPAddr, entry);
				}
			}


			// if RERR multicast, regenerate one RERR with all the invalidating
			// destinations
		} else {
			unreachableIPList = new ArrayList();
			unreachableSeqList = new ArrayList();

			// collect all the destinations that become
			// invalid & start route delete
			for(i = 0; i < rerr.destCount; i++) {
				entry = aodvStateCompRef.getRoute(rerr.destIPAddr[i]);

				// collect dest only if the 'link break' (destRte) route
				// was the next hop
				if(entry != null
						&& !entry.destIPAddr.equals(rerr.fromIPAddr)
						&& entry.nextHopIPAddr.equals(rerr.fromIPAddr)) {
					entry.destSeqNum = rerr.destSeqNum[i];

					unreachableIPList.add(entry.destIPAddr);
					unreachableSeqList.add(new Integer(entry.destSeqNum));

					// invalidate route & start route delete
					entry.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_INVALID;
					entry.expiryTime = (new Date()).getTime()
					+ cfgInfo.deletePeriodVal;
					entry.activeMinder = new DeleteMinder(
							(IAodvState) getConnectedSinkComp(m_PSR_IAodvState),
							entry.destIPAddr, cfgInfo.deletePeriodVal);
					entry.activeMinder.start();

					aodvStateCompRef.updateRouteTable(entry.destIPAddr, entry);
				}
			}
			if(unreachableIPList.size() > 0) {
				adrList = (InetAddress []) unreachableIPList.toArray();
				seqList = new int[unreachableSeqList.size()];
				for(i = 0; i < seqList.length; i++) {
					seqList[i] = ((Integer) unreachableSeqList.get(i)).intValue();
				}
				newRERR = new RERR(cfgInfo, aodvStateCompRef, true,
						cfgInfo.ipAddressMulticastVal, (short) 1,
						false, (byte) unreachableIPList.size(),
						adrList, seqList);
				m_PSR_IPktSender.m_pIntf.sendMessage(newRERR);
			}
		}

	}

	/**
	 * This method is responsible for handling RREP messages recived by the
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
	public synchronized void processAODVMsgRREP(RREP rrep) throws Exception {
		RouteEntry prevHopRte, destRte, origRte;
		boolean	mf, rf, af;
		InetAddress sendto;
		short ttl;
		byte ps, hc;
		InetAddress da, oa;
		int lt, dsn;
		long activeRouteExpiryTime;
		RREP newRREP;
		RouteDiscoveryEntry rde;


//		get ref to ConfigInfo component

		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

//		get ref to state component
		IAodvState aodvStateCompRef = m_PSR_IAodvState.m_pIntf;


		// log
		m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING,
				"AODV Control - RREP Received "+  rrep.toString());

		// find route to prev hop (who sent RREP, i.e dest=prev hop)
		// if not, create route without a valid seq num
		prevHopRte = aodvStateCompRef.getRoute(rrep.fromIPAddr);

		// if no route entry available
		if(prevHopRte == null) {

			// create entry
			prevHopRte = new RouteEntry(aodvStateCompRef);

			prevHopRte.destIPAddr = rrep.fromIPAddr;
			prevHopRte.destSeqNum = 0;
			prevHopRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_INVALID;
			prevHopRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			prevHopRte.ifaceName = rrep.ifaceName ;
			prevHopRte.hopCount = 1;
			prevHopRte.nextHopIPAddr = rrep.fromIPAddr;
			prevHopRte.precursorList = new LinkedList();
			prevHopRte.expiryTime = (new Date()).getTime() + cfgInfo.activeRouteTimeoutVal;
			prevHopRte.activeMinder = new RouteMinder(aodvStateCompRef, rrep.fromIPAddr,
					cfgInfo.activeRouteTimeoutVal);
			prevHopRte.activeMinder.start();


			// if available and not expired
		} else if(prevHopRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

			// route is active, only extend lifetime
			prevHopRte.expiryTime = (new Date()).getTime() + cfgInfo.activeRouteTimeoutVal;

			// if available but expired
		} else {

			// set kernel route, start the minder and extend lifetime
			prevHopRte.hopCount = 1;
			prevHopRte.nextHopIPAddr = rrep.fromIPAddr;

			prevHopRte.expiryTime = (new Date()).getTime() + cfgInfo.activeRouteTimeoutVal;
			prevHopRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			prevHopRte.activeMinder = new RouteMinder(aodvStateCompRef, rrep.fromIPAddr,
					cfgInfo.activeRouteTimeoutVal);
			prevHopRte.activeMinder.start();
		}

		aodvStateCompRef.updateRouteTable(rrep.fromIPAddr, prevHopRte);

		// increment hop count in RREP
		rrep.hopCount++;

		// find route to dest
		destRte =   aodvStateCompRef.getRoute(rrep.destIPAddr);


		// if route found ( compare dest seq num)
		// AND (seq num invalid in route
		//    OR (dest seq in RREP > what is in route (2s comp) AND dest seq valid)
		//    OR (seq == seq AND route is inactive route)
		//    OR (seq num == seq num AND active route AND hop count in RREP is < hop count in route))
		// update route
		if(destRte != null
				&& (destRte.validDestSeqNumFlag == RouteEntry.DEST_SEQ_FLAG_INVALID
						|| (aodvStateCompRef.destSeqCompare(rrep.destSeqNum, destRte.destSeqNum) == IAodvState.GREATER
								&& destRte.validDestSeqNumFlag == RouteEntry.DEST_SEQ_FLAG_VALID)
								|| (aodvStateCompRef.destSeqCompare(rrep.destSeqNum, destRte.destSeqNum) == IAodvState.EQUAL
										&& destRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_INVALID)
										|| (aodvStateCompRef.destSeqCompare(rrep.destSeqNum, destRte.destSeqNum) == IAodvState.EQUAL
												&& destRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID
												&& rrep.hopCount < destRte.hopCount))) {

			destRte.destIPAddr = rrep.destIPAddr;
			destRte.destSeqNum = rrep.destSeqNum;
			destRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_VALID;
			destRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			destRte.ifaceName = rrep.ifaceName ;
			destRte.hopCount = rrep.hopCount;
			destRte.nextHopIPAddr = rrep.fromIPAddr;
			destRte.expiryTime = (new Date()).getTime() + rrep.lifeTime;
			destRte.activeMinder = new RouteMinder(aodvStateCompRef, rrep.destIPAddr,
					rrep.lifeTime);
			destRte.activeMinder.start();
			aodvStateCompRef.updateRouteTable(rrep.destIPAddr, destRte);
			// log

			// if route not found
		} else if(destRte == null) {
			// (100) create route - route flag = active, dest seq flag = valid,
			// 	 next hop = src ip in RREP, hop count = hop count in RREP
			// 	 expiry time = current time + lifetime in RREP
			// 	 dest seq num = dest seq num of RREP
			// 	 dest ip = dest ip in RREP
			// 	 iface = iface from which RREP recvd
			destRte = new RouteEntry(aodvStateCompRef);

			destRte.destIPAddr = rrep.destIPAddr;
			destRte.destSeqNum = rrep.destSeqNum;
			destRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_VALID;
			destRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			destRte.ifaceName = rrep.ifaceName ;
			destRte.hopCount = rrep.hopCount;
			destRte.nextHopIPAddr = rrep.fromIPAddr;
			destRte.precursorList = new LinkedList();
			destRte.expiryTime = (new Date()).getTime() + rrep.lifeTime;
			destRte.activeMinder = new RouteMinder(aodvStateCompRef, rrep.destIPAddr,
					rrep.lifeTime);
			destRte.activeMinder.start();
			aodvStateCompRef.updateRouteTable(rrep.destIPAddr, destRte);
		} else {
			destRte.expiryTime = (new Date()).getTime() + rrep.lifeTime;
			aodvStateCompRef.updateRouteTable(rrep.destIPAddr, destRte);
		}


		// if i am not originator of RREP
		if(!(cfgInfo.ipAddressVal.equals(rrep.origIPAddr))) {
			//find route to originator
			origRte = aodvStateCompRef.getRoute(rrep.origIPAddr);

			if(origRte == null) {
				// somethin wrong
				//log
				aodvStateCompRef.getLog().write(Logging.CRITICAL_LOGGING,
				"AODV Control - No originator route entry found ; try extending lifetime ");

				return;
			} else {
				// update lifetime of route to max of (existing lifetime, currtime + ACTIVE_ROUTE_TIMEOUT)
				// update precursor list - using from the src ip from whom the RREP was
				// 							recvd (i.e. next hop)
				activeRouteExpiryTime = cfgInfo.activeRouteTimeoutVal + (new Date()).getTime();

				if(activeRouteExpiryTime > origRte.expiryTime) {
					origRte.expiryTime = activeRouteExpiryTime;
				}

				origRte.precursorList.add(rrep.fromIPAddr);

				aodvStateCompRef.updateRouteTable(rrep.origIPAddr, origRte);

				// find route to dest
				// update precuror list to dest (i.e. next hop to dest) - put ip of
				//  next hop to which RREP is forwarded (not necessarily originator)
				destRte.precursorList.add(origRte.nextHopIPAddr);
				aodvStateCompRef.updateRouteTable(rrep.destIPAddr, destRte);


				// send RREP to next hop to originator

				mf = false;
				sendto = origRte.nextHopIPAddr;
				ttl = 225;
				rf = rrep.repairFlag;
				af = rrep.ackFlag;
				ps = rrep.prefixSize;
				hc = rrep.hopCount;
				da = rrep.destIPAddr;
				dsn = rrep.destSeqNum;
				oa = rrep.origIPAddr;
				lt = rrep.lifeTime;



				// ???????????????????????????????????????????????????????????
				// creating a new object is not very efficient
				newRREP = new RREP(cfgInfo, aodvStateCompRef, mf, sendto, ttl, rf,
						af, ps, hc, da, dsn, oa, lt);
				// ??????????????????????????????????????????????????????????
				m_PSR_IPktSender.m_pIntf.sendMessage(newRREP);

//				rrep.toIPAddr = origRte.nextHopIPAddr;
//				rrep.ttlValue = 225;
//				rrep.multiCast = false;
//				m_PSR_IPktSender.m_pIntf.sendMessage(rrep);

			}

			// if i am originator
		} else {

			//	unicast RREP-ACK to dest
		}

		// due to this RREP, if a route was made for a route being
		// discovered, start the BufferMinder to release the packets
		// at a given time after the route is made
		// if buffering is not set, simply delete the route discovery
		// entry
		rde = rdList.get(rrep.destIPAddr);
		if(rde != null) {

			if(cfgInfo.packetBufferingVal) {
				(new BufferMinder(cfgInfo, this,
						rrep.destIPAddr)).start();
			} else {
				rdList.remove(rrep.destIPAddr);
			}

			// log
			m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING,
					"AODVControl - Route discovery terminated as route made to "
					+ rrep.destIPAddr.getHostAddress());
		}


	}

	public synchronized void processAODVMsgRREPACK(RREPACK rrepack) throws Exception {
		//	RREP-ACK

		// not implemented

		return;

	}


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
	public synchronized void processAODVMsgRREQ(RREQ rreq) throws Exception {
		RouteEntry prevHopRte, origRte, destRte;
		boolean mf, rf, af;
		InetAddress sendto;
		short ttl;
		byte ps, hc;
		InetAddress da, oa;
		int lt, dsn;
		boolean jf, gf, df, usnf;
		int ri, osn;
		boolean generateRREP;
		long minimalLifetime;
		RREP rrep;
		RREQ newRREQ;


		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		IAodvState pAodvStateComp = m_PSR_IAodvState.m_pIntf;
		if (pAodvStateComp == null)
			throw new RoutingCFException(RoutingCFException.NO_STATE_COMPONENT_CONNECTED);


		ILog log = m_PSR_ILog.m_pIntf;
		if (log == null)
			throw new RoutingCFException(RoutingCFException.NO_LOGGER_CONNECTED);

		// log
		log.write(Logging.INFO_LOGGING,
				"AODV Control - RREQ Received "+  rreq.toString());

		// create or update a route to the prev hop increase lifetime
		//		by ACTIVE_ROUTE_TIMEOUT (without a valid seq num, i.e. validDestSeqNumFlag = invalid
		prevHopRte = pAodvStateComp.getRoute(rreq.fromIPAddr);

		// if no route entry available
		if(prevHopRte == null) {

			// create entry
			prevHopRte = new RouteEntry(pAodvStateComp);

			prevHopRte.destIPAddr = rreq.fromIPAddr;
			prevHopRte.destSeqNum = 0;
			prevHopRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_INVALID;
			prevHopRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			prevHopRte.ifaceName = rreq.ifaceName ;
			prevHopRte.hopCount = 1;
			prevHopRte.nextHopIPAddr = rreq.fromIPAddr;
			prevHopRte.precursorList = new LinkedList();
			prevHopRte.expiryTime = (new Date()).getTime() + cfgInfo.activeRouteTimeoutVal;
			prevHopRte.activeMinder = new RouteMinder(pAodvStateComp, rreq.fromIPAddr,
					cfgInfo.activeRouteTimeoutVal);
			prevHopRte.activeMinder.start();

			// if available and not expired
		} else if(prevHopRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

			// route is active, only extend lifetime
			prevHopRte.expiryTime = (new Date()).getTime() + cfgInfo.activeRouteTimeoutVal;

			// if available but expired
		} else {

			// set kernel route, start the minder and extend lifetime
			prevHopRte.hopCount = 1;
			prevHopRte.nextHopIPAddr = rreq.fromIPAddr;

			prevHopRte.expiryTime = (new Date()).getTime() + cfgInfo.activeRouteTimeoutVal;
			prevHopRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			prevHopRte.activeMinder = new RouteMinder(pAodvStateComp, rreq.fromIPAddr,
					cfgInfo.activeRouteTimeoutVal);
			prevHopRte.activeMinder.start();
		}

		pAodvStateComp.updateRouteTable(rreq.fromIPAddr, prevHopRte);

		// check RREQ previously recvd (check from RREQ ID + orig IP in list), if so drop
		if(idList.exist(rreq.origIPAddr, rreq.RREQID)) {

			// log
			log.write(Logging.INFO_LOGGING,
			"AODVControl - RREQ disregarded as previously processed");

			return;
		}

		// in RREQ, increment hop count
		rreq.hopCount++;


		// serach a route to the originator of RREQ, then add or update route
		//			(use orig seq num in RREQ to update, see FORMULA
		//			 set validDestSeqNumFlag = valid)
		origRte = pAodvStateComp.getRoute(rreq.origIPAddr);

		// if route not available
		if(origRte == null) {
			origRte = new RouteEntry(pAodvStateComp);
			origRte.destIPAddr = rreq.origIPAddr;
			origRte.destSeqNum = rreq.origSeqNum;
			origRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_VALID;
			origRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			origRte.ifaceName = rreq.ifaceName ;
			origRte.hopCount = (byte) rreq.hopCount;
			origRte.nextHopIPAddr = rreq.fromIPAddr;
			origRte.precursorList = new LinkedList();
			origRte.expiryTime = (new Date()).getTime();

			// if available and not expired
		} else if(origRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

			// only lifetime need extended, done later

			// if available but expired
		} else {

			// create whole route
			origRte.destSeqNum = rreq.origSeqNum;
			origRte.validDestSeqNumFlag = RouteEntry.DEST_SEQ_FLAG_VALID;
			origRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_VALID;
			origRte.ifaceName = rreq.ifaceName ;
			origRte.hopCount = (byte) rreq.hopCount;
			origRte.nextHopIPAddr = rreq.fromIPAddr;
			origRte.precursorList = new LinkedList();
			origRte.expiryTime = (new Date()).getTime();
		}


		// update lifetime
		// route lifetime to originator should be updated using FORMULA
		//	maximum of (ExistingLifetime, MinimalLifetime)
		//   MinimalLifetime = (current time + 2*NET_TRAVERSAL_TIME -
		//                                    2*HopCount*NODE_TRAVERSAL_TIME).
		minimalLifetime = (2 * cfgInfo.netTraversalTimeVal)
		- (2 * origRte.hopCount * cfgInfo.nodeTraversalTimeVal)
		+ (new Date()).getTime();
		if(minimalLifetime > origRte.expiryTime)
			origRte.expiryTime = minimalLifetime;

		origRte.activeMinder = new RouteMinder(pAodvStateComp, rreq.origIPAddr,
				(int) (origRte.expiryTime - (new Date()).getTime()));
		origRte.activeMinder.start();

		pAodvStateComp.updateRouteTable(rreq.origIPAddr, origRte);

		// check if i am the destnation, then RREP generated
		if(cfgInfo.ipAddressVal.equals(rreq.destIPAddr)) {
			generateRREP = true;
			destRte = null;

			// check if route to dest available AND active AND D flag is not set AND my dest seq num is valid
			// 			AND my dest seq num >= to dest seq num in RREQ, then RREP generated
		} else {
			destRte =pAodvStateComp.getRoute(rreq.destIPAddr);
			if(destRte != null
					&& (destRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID)
					&& !rreq.destOnlyFlag
					&& destRte.validDestSeqNumFlag == RouteEntry.DEST_SEQ_FLAG_VALID
					&& (pAodvStateComp.destSeqCompare(destRte.destSeqNum, rreq.destSeqNum) == IAodvState.GREATER
							|| pAodvStateComp.destSeqCompare(destRte.destSeqNum, rreq.destSeqNum) == IAodvState.EQUAL)) {
				generateRREP = true;

				// if none of above, propagate the RREQ
			} else {
				generateRREP = false;
				destRte = null;
			}
		}

		// if RREP generated
		if(generateRREP) {
			// if i am destination
			if(cfgInfo.ipAddressVal.equals(rreq.destIPAddr)) {
				// seq num see FORMULA
				// hop count = 0, lifetime = MY_ROUTE_TIME, prefix = 0, R flag 0,
				// A flag from parametrs, rest from RREQ
				// unicast send, to sender of RREQ (prev hop)

				mf = false;
				sendto = prevHopRte.destIPAddr;
				ttl = 255;
				rf = false;
				af = cfgInfo.RREPAckRequiredVal;
				ps = 0;
				hc = 0;
				da = cfgInfo.ipAddressVal;
				//rreq.destSeqNum++;

				if(pAodvStateComp.destSeqCompare(rreq.destSeqNum, pAodvStateComp.getLastSeqNum()) == IAodvState.GREATER) {
					pAodvStateComp.setLastSeqNum(rreq.destSeqNum);
				} else if(pAodvStateComp.destSeqCompare(rreq.destSeqNum, pAodvStateComp.getLastSeqNum()) == IAodvState.EQUAL) {
					pAodvStateComp.incrementOwnSeqNum();
				} else {
					// use existing value
				}
				dsn = pAodvStateComp.getLastSeqNum();
				oa = origRte.destIPAddr;
				lt = cfgInfo.myRouteTimeoutVal;

				rrep = new RREP(cfgInfo, pAodvStateComp, mf, sendto, ttl, rf,
						af, ps, hc, da, dsn, oa, lt);
				m_PSR_IPktSender.m_pIntf.sendMessage(rrep);


				// if not destination (intermediate node)
			} else {

				// seq num = what is route entry
				// hop count = what is route entry
				// lifetime = what is route entry (route time - curr time)
				// rest from RREQ
				// unicast send, to sender of RREQ (prev hop)
				mf = false;
				sendto = prevHopRte.destIPAddr;
				ttl = 255;
				rf = false;
				af = cfgInfo.RREPAckRequiredVal;
				ps = 0;
				hc = (byte) destRte.hopCount;
				da = destRte.destIPAddr;
				dsn = destRte.destSeqNum;
				oa = rreq.origIPAddr;
				lt = (int) (destRte.expiryTime - (new Date()).getTime());

				rrep = new RREP(cfgInfo, pAodvStateComp, mf, sendto, ttl, rf,
						af, ps, hc, da, dsn, oa, lt);
				m_PSR_IPktSender.m_pIntf.sendMessage(rrep);

				// if G flag set in RREQ (send RREP to destination)
				if(rreq.gratRREPFlag) {
					// hop count = from route to originator
					// dest ip adddress = originate of RREQ
					// dest seq num = originator seq num of RREQ
					// originator ip address = dest ip address
					// lifetime = (route time - curr time) to originator
					// unicast send to next hop to destination (from route)

					mf = false;
					sendto = destRte.nextHopIPAddr;
					ttl = 225;
					rf = false;
					af = cfgInfo.RREPAckRequiredVal;
					ps = 0;
					hc = (byte) origRte.hopCount;
					da = origRte.destIPAddr;
					dsn = origRte.destSeqNum;
					oa = destRte.destIPAddr;
					lt = (int) (origRte.expiryTime - (new Date()).getTime());

					rrep = new RREP(cfgInfo, pAodvStateComp, mf, sendto, ttl, rf,
							af, ps, hc, da, dsn, oa, lt);
					m_PSR_IPktSender.m_pIntf.sendMessage(rrep);

				}
			}

			// if no RREP generated, then propagate RREQ
		} else {
			// check the TTL, should be > 1 else drop packet
			if(rreq.ttlValue > 1) {
				// reduce TTL by 1
				// place highest of dest seq considering RREQ and route to dest in my route list
				// put RREQ ID + originator ip in list for RREQ minder with PATH_DISCOVERY_TIME
				// propogate RREQ
				rreq.ttlValue--;
				if(destRte != null) {
					if(!rreq.unknownSeqNumFlag
							&& pAodvStateComp.destSeqCompare(rreq.destSeqNum, destRte.destSeqNum)
							== IAodvState.GREATER) {

						destRte.destSeqNum = rreq.destSeqNum;
						pAodvStateComp.updateRouteTable(rreq.destIPAddr, destRte);
						rreq.unknownSeqNumFlag = false;
					} else {
						rreq.destSeqNum = destRte.destSeqNum;
					}
				}

				sendto = cfgInfo.ipAddressMulticastVal;
				ttl = rreq.ttlValue;
				jf = rreq.joinFlag;
				rf = rreq.repairFlag;
				gf = rreq.gratRREPFlag;
				df = rreq.destOnlyFlag;
				usnf = rreq.unknownSeqNumFlag;
				hc = rreq.hopCount;
				ri = rreq.RREQID;
				da = rreq.destIPAddr;
				dsn = rreq.destSeqNum;
				oa = rreq.origIPAddr;
				osn = rreq.origSeqNum;
				newRREQ = new RREQ(cfgInfo, pAodvStateComp, true, sendto, ttl, jf, rf, gf, df, usnf, hc,
						ri, da, dsn, oa, osn);
				idList.add(oa, ri);
				m_PSR_IPktSender.m_pIntf.sendMessage(newRREQ);

			}
		}

	}

	/**
	 * Method to prolong the lifetime of routes in use. Routes to the originator, next hop to
	 * the originator, destination, next hop towards destination are extended.
	 */
	public synchronized void processExistingRouteUse(IPPkt pkt) throws Exception {

		RouteEntry origRte, destRte, nextHopRte;
		long currTime;

		currTime = (new Date()).getTime();


		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		IAodvState pAodvStateComp = m_PSR_IAodvState.m_pIntf;
		if (pAodvStateComp == null)
			throw new RoutingCFException(RoutingCFException.NO_STATE_COMPONENT_CONNECTED);


		ILog log = m_PSR_ILog.m_pIntf;
		if (log == null)
			throw new RoutingCFException(RoutingCFException.NO_LOGGER_CONNECTED);

		// if I am not the originator of the packet, then update route to originator
		// of packet and also the next hop, if one exists
		if(!(pkt.fromIPAddr.equals(cfgInfo.ipAddressVal))) {

			origRte = pAodvStateComp.getRoute(pkt.fromIPAddr);

			if(origRte != null
					&& origRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

				origRte.expiryTime = currTime + cfgInfo.activeRouteTimeoutVal;
				pAodvStateComp.updateRouteTable(pkt.fromIPAddr, origRte);

				if(origRte.hopCount > 1) {

					nextHopRte = pAodvStateComp.getRoute(origRte.nextHopIPAddr);

					if(nextHopRte != null
							&& nextHopRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

						nextHopRte.expiryTime = currTime
						+ cfgInfo.activeRouteTimeoutVal;
						pAodvStateComp.updateRouteTable(origRte.nextHopIPAddr, nextHopRte);

					}
				}

			}
		}

		// if I am not the destination of the packet, then update route to destination
		// of packet and also the next hop, if one exists
		if(!(pkt.toIPAddr.equals(cfgInfo.ipAddressVal))) {

			destRte = pAodvStateComp.getRoute(pkt.toIPAddr);

			if(destRte != null
					&& destRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

				destRte.expiryTime = currTime + cfgInfo.activeRouteTimeoutVal;
				pAodvStateComp.updateRouteTable(pkt.toIPAddr, destRte);

				if(destRte.hopCount > 1) {

					nextHopRte = pAodvStateComp.getRoute(destRte.nextHopIPAddr);

					if(nextHopRte != null
							&& nextHopRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

						nextHopRte.expiryTime = currTime
						+ cfgInfo.activeRouteTimeoutVal;
						pAodvStateComp.updateRouteTable(destRte.nextHopIPAddr, nextHopRte);

					}
				}

			}
		}

	}
	
	
	/**
	 * startRREQIDMinder starts the RREQID minder thread
	 * 
	 * @throws Exception
	 */
	public void startRREQIDMinder() throws Exception{
		if (idMinder!=null)
			idMinder.start();
	}
	
	/**
	 * 
	 * stopRREQIDMinder stops the RREQID minder thread
	 * 
	 * @throws Exception
	 */
	public void stopRREQIDMinder() throws Exception{
		
		if (idMinder!=null)
			idMinder.terminate();
	}
	
	

	// ----------------------- IRouteDiscovery Interface --------------------
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
	public synchronized void processRouteDiscovery(IPPkt pkt) throws Exception{
		RouteEntry dest;
		RREQ rreq;
		InetAddress sendto, da, oa;
		short ttl;
		boolean jf, rf, gf, df, usnf;
		byte hc;
		int ri, dsn, osn;
		RouteDiscoveryEntry rde;
		DiscoveryMinder rdThread;
		int initialSleep;



		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		IAodvState pAodvStateComp = m_PSR_IAodvState.m_pIntf;
		if (pAodvStateComp == null)
			throw new RoutingCFException(RoutingCFException.NO_STATE_COMPONENT_CONNECTED);


		ILog log = m_PSR_ILog.m_pIntf;
		if (log == null)
			throw new RoutingCFException(RoutingCFException.NO_LOGGER_CONNECTED);



		try {

			// if already a route is being discovered for the
			// given destination, then add the packet to the packet buffer in the 
			// route discovery entry in the list
			// don't do anything else
			rde = rdList.get(pkt.toIPAddr);
			if(rde != null) {

				// dont add if packet buffering is not
				// enabled
				if(cfgInfo.packetBufferingVal) {
					rde.pktBuffer.add(pkt);
					
				}
				return;
			}

			//find route to dest in table
			dest = pAodvStateComp.getRoute(pkt.toIPAddr);

			// if route made, this means that this is a
			// bufferred packet ; therefore, send the packet out
			// don't do anything else
			if(dest != null && dest.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

				// send buffered packets only if parameter is set
				// as this too is related to packet buffering
				if(cfgInfo.packetBufferingVal) {
					m_PSR_IPktSender.m_pIntf.sendPkt(pkt);
				}

				return;
			}


			// log
			log.write(Logging.INFO_LOGGING,
					"AODV Control - Route discovery started for "
					+ pkt.toIPAddr.getHostAddress());


			// generate RREQ

			//( found means (route status = invalid) )
			if(dest != null) {

				// multicast RREQ
				// set TTL to (hop count of route + TTL_INCREMENT)
				// join flag & repair flag is not set as simple dest search
				// G flag from parameters
				// D flag from parameters
				// dest seq num known
				// hop count = 0
				// increment RREQ ID, RREQ ID of originator = RREQ ID
				// destination adr from the IP packet
				// dest seq = last know seq num
				// has to be always my own IP adr
				// increment own seq num, orig seq num = own num

				sendto = cfgInfo.ipAddressMulticastVal;
				ttl = (short) (dest.hopCount + cfgInfo.TTLIncrementVal);
				jf = false;
				rf = false;
				gf = cfgInfo.gratuitousRREPVal;
				df = cfgInfo.onlyDestinationVal;
				usnf = false;
				hc = 0;
				ri = pAodvStateComp.incrementOwnRREQID();
				da = pkt.toIPAddr;
				dsn = dest.destSeqNum;
				oa = pkt.fromIPAddr;
				osn = pAodvStateComp.incrementOwnSeqNum();

			} else { //if not found

				// multicast RREQ
				// set TTL to TTL_START
				// join flag & repair flag is not set as simple dest search
				// G flag from parameters
				// D flag from parameters
				// since not in route, dest seq num not known
				// hop count = 0
				// increment RREQ ID, RREQ ID of originator = RREQ ID
				// destination adr from the IP packet
				// dest seq not known
				// has to be always my own IP adr
				// increment own seq num, orig seq num = own num
				sendto = cfgInfo.ipAddressMulticastVal;
				ttl = (short) cfgInfo.TTLStartVal;
				jf = false;
				rf = false;
				gf = cfgInfo.gratuitousRREPVal;
				df = cfgInfo.onlyDestinationVal;
				usnf = true;
				hc = 0;
				ri = pAodvStateComp.incrementOwnRREQID();
				da = pkt.toIPAddr;
				dsn = 0;
				oa = pkt.fromIPAddr;
				osn = pAodvStateComp.incrementOwnSeqNum();
			}


			rreq = new RREQ(cfgInfo, pAodvStateComp, true, sendto, ttl, jf, rf, gf, df, usnf, hc,
					ri, da, dsn, oa, osn);

			// put RREQ ID + originator ip in list for RREQ minder with PATH_DISCOVERY_TIME
			idList.add(oa, ri);

			// multicast the RREQ
			m_PSR_IPktSender.m_pIntf.sendMessage(rreq);

			// if route discovery is ERS
			if(cfgInfo.routeDiscoveryModeVal == ConfigInfo.ROUTE_DISCOVERY_ERS_VAL) {
				initialSleep = 2 * cfgInfo.nodeTraversalTimeVal
				* ( ttl + cfgInfo.timeoutBufferVal);

				// else assumes, route discovery is non-ERS
			} else {
				initialSleep = cfgInfo.netTraversalTimeVal;
			}


			// start route discoverer
			rdThread = new DiscoveryMinder(cfgInfo, this, da, initialSleep);
			rdThread.start();

			// add the fist packet to the packet buffer (if enabled) and
			// update the route discovery list
			rde = new RouteDiscoveryEntry(cfgInfo, pAodvStateComp, da, rreq, rdThread, initialSleep);
			if(cfgInfo.packetBufferingVal) {
				rde.pktBuffer.add(pkt);
			}
			rdList.update(da, rde);

		} catch(Exception e) {

			// log
			log.write(Logging.CRITICAL_LOGGING,
					"AODVControl - Route discovery failed " + e);
		}

	}

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
	public synchronized int continueRouteDiscovery(InetAddress destIP) throws Exception {
		RouteEntry entry;
		RREQ newRREQ;
		RouteDiscoveryEntry rde;
		IPPkt pkt;
		InetAddress sendto, da, oa;
		short ttl;
		boolean jf, rf, gf, df, usnf;
		byte hc;
		int ri, dsn, osn;

		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		IAodvState pAodvStateComp = m_PSR_IAodvState.m_pIntf;
		if (pAodvStateComp == null)
			throw new RoutingCFException(RoutingCFException.NO_STATE_COMPONENT_CONNECTED);


		ILog log = m_PSR_ILog.m_pIntf;
		if (log == null)
			throw new RoutingCFException(RoutingCFException.NO_LOGGER_CONNECTED);

		entry = pAodvStateComp.getRoute(destIP);

		// if route made, stop discovery
		if(entry != null && entry.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {
			
			return 0; // next sleep time for dicoverer minder == 0
		}

		// get entry from the discovery list
		rde = rdList.get(destIP);
		if(rde == null) {

			// log
			log.write(Logging.INFO_LOGGING,
			"AODV Control - Route discovery terminated due to no RDE entry; RDE removed when RREP recvd.");
			return 0;
		}

		// if max retries exceeded, stop discovery
		if((rde.rreqRetries + 1) > cfgInfo.RREQRetriesVal) {
			rdList.remove(destIP);

			// log
			log.write(Logging.INFO_LOGGING,
					"AODVControl - Route discovery terminated as max retries reached ("
					+ rde.rreqRetries + ")");
			return 0;
		}


		// create the new RREQ, using the old RREQ (except for TTL and RREQID)
		sendto = rde.rreq.toIPAddr;

		// increment TTL by TTL_INCREMENT, but it should not exceed TTL_THRESHHOLD
		ttl = (short) (rde.rreq.ttlValue + cfgInfo.TTLIncrementVal);
		if(ttl >= cfgInfo.TTLThresholdVal) {
			ttl = (short) cfgInfo.netDiameterVal;
		}

		jf = rde.rreq.joinFlag;
		rf = rde.rreq.repairFlag;
		gf = rde.rreq.gratRREPFlag;
		df = rde.rreq.destOnlyFlag;
		usnf = rde.rreq.unknownSeqNumFlag;
		hc = rde.rreq.hopCount;

		// Increment RREQID and use this value
		ri = pAodvStateComp.incrementOwnRREQID();

		da = rde.rreq.destIPAddr;
		dsn = rde.rreq.destSeqNum;
		oa = rde.rreq.origIPAddr;
		osn = rde.rreq.origSeqNum;

		newRREQ = new RREQ(cfgInfo, pAodvStateComp, true, sendto, ttl, jf, rf, gf, df, usnf, hc,
				ri, da, dsn, oa, osn);

		rde.rreq = newRREQ;

		// put RREQ ID + originator ip in list for RREQ minder with PATH_DISCOVERY_TIME
		idList.add(oa, ri);

		// multicast the RREQ again
		m_PSR_IPktSender.m_pIntf.sendMessage(newRREQ);

		// increment the number of RREQs re-send
		rde.rreqRetries++;

		// if route discovery is ERS
		if(cfgInfo.routeDiscoveryModeVal == ConfigInfo.ROUTE_DISCOVERY_ERS_VAL) {
			if(ttl >= cfgInfo.TTLThresholdVal) {
				rde.sleepTime = cfgInfo.netTraversalTimeVal;
			} else {
				rde.sleepTime = 2 * cfgInfo.nodeTraversalTimeVal
				* ( ttl + cfgInfo.timeoutBufferVal);
			}

			// else assumes, route discovery is non-ERS
		} else {
			rde.sleepTime = rde.sleepTime * 2;
		}

		return rde.sleepTime;
	}


	/**
	 * This method releases the packets in a buffer when given the
	 * destination IP address. This method is called by the BufferMinder
	 * that is responsible for releasing the buffer.
	 *
	 * @param InetAddress dest - Destination IP
	 */
	public synchronized void releaseBuffer(InetAddress dest) throws Exception {
		RouteDiscoveryEntry rde;
		IPPkt pkt;

		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);


		rde = rdList.get(dest);
		if(rde != null) {

			// send buffered packets only if parameter is set
			if(cfgInfo.packetBufferingVal) {
				while(rde.pktBuffer.size() > 0) {
					pkt = (IPPkt) rde.pktBuffer.remove(0);
					m_PSR_IPktSender.m_pIntf.sendPkt(pkt);
				}
			}
			rdList.remove(dest);
		}

	}

	public ILog getLog() {

		return m_PSR_ILog.m_pIntf;
	}


	/*--------------------------------------------------------------------------------------*/

	// RREQID minder interface
	// -----------------------

	public synchronized RREQIDEntry getFirstRREQID() {
		return idList.getFirst();
	}

	public synchronized RREQIDEntry removeRREQID(InetAddress adr, int id) {
		return idList.remove(adr, id);
	}


	/*--------------------------------------------------------------------------------------*/

	// ------------------------ IControl Interface -----------------------
	/* (non-Javadoc)
	 * @see interfaces.IControl.IControl#Create(java.lang.String, java.lang.Object)
	 */
	public ResultCode Create(String networkIdentifier, Object additionalParameters) {
		try{
			// start AODV router and start listening to packets  
			cfgInfo = null;
			Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
			if (cfgInfoObj instanceof ConfigInfo) {
				cfgInfo= (ConfigInfo) cfgInfoObj;
			}
			if (cfgInfo == null)
				throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
			
			// flag router as active
			cfgInfo.RouterActive = true;
			
			// save network identifier
			cfgInfo.NetworkIdentifier = networkIdentifier;
			
			// initialise AODV Control component
			this.start();
			
			// load GUI as per configinfo gui mode parameter
			if (m_PSR_IGui.m_pIntf !=null){
				
				if (!m_PSR_IGui.m_pIntf.isGUILoaded())
					m_PSR_IGui.m_pIntf.loadGUI();
			}
            	
			// start activity logging
			m_PSR_ILog.m_pIntf.start();
	
			// start packet sender
			m_PSR_IPktSender.m_pIntf.start();
			
			// initialize the routing environment
			m_PSR_IOSOperations.m_pIntf.initializeRouteEnvironment(0);
	
			// activate the router state component
			m_PSR_IAodvState.m_pIntf.start();
			
			// start packet listener thread
			IUnknown pPktListenIUnk = m_PSR_IOpenCOM.m_pIntf.getComponentPIUnknown("PacketListener");
			Runnable pPktListenRunnable= (Runnable) getDelegatedComp(pPktListenIUnk);
			pktListener = new Thread(pPktListenRunnable);
			pktListener.start();
			
			// start hello sending thread
			IUnknown pAodvStateIUnk = m_PSR_IOpenCOM.m_pIntf.getComponentPIUnknown("AODVState");
			IHello pAodvStateHello = (IHello) pAodvStateIUnk.QueryInterface("interfaces.IHello.IHello");
			pAodvStateHello.startHello();
			
	
			// start RREQ ID tracking thread
			idMinder.start();
	
			// log
			m_PSR_ILog.m_pIntf.write(ILog.ACTIVITY_LOGGING,
				"Control - Protocol Handler started");
			
		}catch(Exception e){
			
			// log
			m_PSR_ILog.m_pIntf.write(ILog.CRITICAL_LOGGING,
				"AODV Control - Problem in start - " + e);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see interfaces.IControl.IControl#Join(java.lang.String, java.lang.Object)
	 */
	public ResultCode Join(String networkIdentifier, Object additionalParameters) {

		return null;
	}
	

	/* (non-Javadoc)
	 * @see interfaces.IControl.IControl#Leave(java.lang.String)
	 */
	public ResultCode Leave(String networkIdentifier) {
		
		try{
			if ((cfgInfo.RouterActive) && (cfgInfo.NetworkIdentifier.equalsIgnoreCase(networkIdentifier)))
			{
				cfgInfo.RouterActive = false;
				
				//	deactivate the route list
				m_PSR_IAodvState.m_pIntf.stop();
		
				// terminate the routing environment
				m_PSR_IOSOperations.m_pIntf.finalizeRouteEnvironment();
		
				// stop activity logging
				m_PSR_ILog.m_pIntf.stop();
			}
		}
		catch(Exception e)
		{
			return new ResultCode(0);
		}
			
		return null;
	}

	// ------------------------ ILifecycle Interface ----------------------
	public boolean startup(Object data) {


		return false;
	}

	public boolean shutdown() {

		return false;
	}

	// ---------------------------- IConnections Interface ---------------------
	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {

		if(riid.toString().equalsIgnoreCase("interfaces.IState.IAodvState")){
			return m_PSR_IAodvState.connectToRecp(pSinkIntf, riid, provConnID);
		}

		if(riid.toString().equalsIgnoreCase("interfaces.IPacketSender.IPacketSender")){
			return m_PSR_IPktSender.connectToRecp(pSinkIntf, riid, provConnID);
		}

		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}

		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IGui.IGui")){
			return m_PSR_IGui.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IOSOperations.IOSOperations")){
			return m_PSR_IOSOperations.connectToRecp(pSinkIntf, riid, provConnID);
		}
		return false;
	}

	public boolean disconnect(String riid, long connID) {

		if(riid.toString().equalsIgnoreCase("interfaces.IState.IAodvState")){
			return m_PSR_IAodvState.disconnectFromRecp(connID);
		}

		if(riid.toString().equalsIgnoreCase("interfaces.IPacketSender.IPacketSender")){
			return m_PSR_IPktSender.disconnectFromRecp(connID);
		}

		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.disconnectFromRecp(connID);
		}

		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IGui.IGui")){
			return m_PSR_IGui.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IOSOperations")){
			return m_PSR_IOSOperations.disconnectFromRecp(connID);
		}

		return false;
	}

//	--------------------- additional OpenCOM methods -----------------
	public Object getConnectedSinkComp(OCM_SingleReceptacle pSR)
	{
		if (pSR.m_pIntf instanceof Proxy) {
			Proxy objProxy = (Proxy) pSR.m_pIntf;
			InvocationHandler delegatorIVh = Proxy.getInvocationHandler(objProxy);

			if (delegatorIVh instanceof Delegator) {
				return((Delegator) delegatorIVh).obj;
			}
		}
		return null;
	}


	/**
	 * 
	 */
	public Object getDelegatedComp(IUnknown pSinkIUnk)
	{
		if (pSinkIUnk instanceof Proxy) {
        	Proxy objProxy = (Proxy) pSinkIUnk;
        	InvocationHandler delegatorIVh = Proxy.getInvocationHandler(objProxy);
        	
        	if (delegatorIVh instanceof Delegator) {
				return((Delegator) delegatorIVh).obj;
			}
        	
        	
		}
		return null;
	}



}
