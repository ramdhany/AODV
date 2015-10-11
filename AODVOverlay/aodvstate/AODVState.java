/*
 * AODVState.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	16 Feb 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: AODVState.java, aodvstate 
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

package aodvstate;

import interfaces.IConfigInfo.IConfigInfo;
import interfaces.IGui.IGui;
import interfaces.IHello.IHello;
import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;
import interfaces.IPacketSender.IPacketSender;
import interfaces.IState.IAodvState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;



import msg.HELLO;
import msg.RERR;
import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import exceptions.RoutingCFException;

/**
 * @author ramdhany
 *
 */
public class AODVState extends OpenCOMComponent implements IAodvState, IHello ,IUnknown,
		IMetaInterface, ILifeCycle, IConnections {
	
	public RouteList rtList; // contains the route entries for destinations + associated minder
	public HelloMinder helloMinder;			// thread to  periodically send Hello msgs
	public ConfigInfo cfgInfo;
	
	public OCM_SingleReceptacle<IOSOperations> m_PSR_IOSOperations; //Requires Interface of type IOSOperations.
	public OCM_SingleReceptacle<IPacketSender> m_PSR_IPktSender; 	//Requires Interface of type IPacketSender.
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo; 	// To connect to ConfigInfo component
														   			// ConfigInfo contains protocol parameters
	public OCM_SingleReceptacle<ILog> m_PSR_ILog; 					// To connect to Logging component
	public OCM_SingleReceptacle<IGui> m_PSR_IGui; 					//Requires Interface of type IOSOperations.
	
	public int lastSeqNum; 					// sequence number of node
	public int lastRREQID; 					// RREQID see RFC 3561
	
	public AODVState(IUnknown pIOCM) {
		super(pIOCM);
		lastSeqNum = 0;
		lastRREQID = 0;
		m_PSR_IOSOperations = new OCM_SingleReceptacle<IOSOperations>(IOSOperations.class);
		m_PSR_IPktSender = new OCM_SingleReceptacle<IPacketSender>(IPacketSender.class);
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
		m_PSR_IGui = new OCM_SingleReceptacle<IGui>(IGui.class);
		cfgInfo = null;
		
	}
	
	//	 ---------------- IAodvState Operations -------------------------
	/**
	 * Method to initialise component member variables after component has been
	 * connected to the ConfigInfo component containing protocol parameters  
	 * 
	 */
	public void start() {
		cfgInfo = null;
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		// route list requires connection to Config Info Component
		rtList = new RouteList(this);
		
		m_PSR_ILog.m_pIntf.write(ILog.INFO_LOGGING,
		"AODVState - component initialised");
		
		//	create Hello Minder thread
		helloMinder = new HelloMinder(this);
	}
	
	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#stop()
	 */
	public void stop() throws Exception{
		
		// clean up the route table and associated entries in the 
		// kernel route table
		rtList.stop();
		
	}
	
	
	/**
	* Method to increment the local Sequence Number
	* TODO : code for number rollover
	*
	* @return int - the incremented sequence number
	*/
	public int incrementOwnSeqNum() {
		return ++lastSeqNum;
	}

	/**
	* Method to increment the local RREQ ID
	*
	* TODO : code for number rollover
	*
	* @return int - the incremented RREQ ID
	*/
	public int incrementOwnRREQID() {
		return ++lastRREQID;
	}

	/**
	* Method to compare Destination Sequence Numbers. Returns
	* 1, 0 or -1 based on whether first number is greater, equal
	* or less than the second number, respectively.
	*
	* TODO : code for number rollover
	*
	* @param int firstNum - 1st number to compare
	* @param int secondNum - 2nd number to compare
	* @return int - 1, 0 or -1 based on greater, equal or less
	*/
	public int destSeqCompare(int firstNum, int secondNum) {
		if(firstNum > secondNum)
			return GREATER;
		else if(firstNum < secondNum)
			return LESS;
		else
			return EQUAL;
	}
	
	
	
	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#checkActiveRouteLifetime(java.net.InetAddress, aodvstate.RouteMinder)
	 */
	public synchronized int checkActiveRouteLifetime(InetAddress da, RouteMinder rtMinder) throws Exception {
		
		aodvstate.RouteEntry destRte; // route entry for destination da
		int lifetime; // time left before expiration
		long currTime;
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		
		destRte = rtList.get(da);
		if(destRte == null)
			return 0;


		// if for some reason the rtMinder is not the current thread that
		// is managing the route, then stop the thread
		if(destRte.activeMinder != rtMinder) {
			return 0;
		}

		// if for some reason the route has become invalid, then stop the thread
		if(destRte.routeStatusFlag != RouteEntry.ROUTE_STATUS_FLAG_VALID) {
			return 0;
		}

		currTime = (new Date()).getTime();
		lifetime = (int) (destRte.expiryTime - currTime);
		if(lifetime <= 0) {

			// simply expire route (also start route
			// delete)

			destRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_INVALID;
			destRte.destSeqNum++;
			destRte.expiryTime = currTime + cfgInfo.deletePeriodVal;
			destRte.activeMinder = new DeleteMinder(this, destRte.destIPAddr, 
													cfgInfo.deletePeriodVal);
			destRte.activeMinder.start();

			rtList.update(da, destRte);

			return 0;

		} else
			return lifetime;
		
	}

	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#checkDeleteRouteLifetime(java.net.InetAddress, aodvstate.DeleteMinder)
	 */
	public synchronized int checkDeleteRouteLifetime(InetAddress da, DeleteMinder delMinder) throws Exception {
		
		RouteEntry destRte;
		int lifetime;

		destRte = rtList.get(da);
		if(destRte == null)
			return 0;

		// if for some reason the route has got some status other than invalid
		// OR delMinder is not the current thread that is managing the route,
		// then stop the thread
		if((destRte.routeStatusFlag != RouteEntry.ROUTE_STATUS_FLAG_INVALID)
		    || (destRte.activeMinder != delMinder)) {
			return 0;
		}

		lifetime = (int) (destRte.expiryTime - (new Date()).getTime());
		if(lifetime <= 0) {

			// if lifetime expired, delete the route from list
			rtList.remove(da);

			return 0;

		} else
			return lifetime;
	}

	
	public synchronized RouteEntry getRoute(InetAddress key) throws Exception {
		return rtList.get(key);
	}
	
	
	/**
	* Method to send RERRs dependent on a route that has expired and no hellos
	* have been heard.
	*
	* @RouteEntry destRte - the destination that is expiring
	* @return Exception - any errors
	*
	*/
	public synchronized void invalidateDestinations(RouteEntry destRte) throws Exception {
		ArrayList unreachableIPList;
		ArrayList unreachableSeqList;
		RouteEntry entry;
		Object array[];
		int i, j;
		InetAddress adrList[];
		int seqList[];
		RERR rerr;
		
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		ILog log = m_PSR_ILog.m_pIntf;
		
		array = rtList.getRouteArray();

		// if RERR unicast, send a RERR to each precursor in a
		// invalidating route
		if(cfgInfo.RERRSendingModeVal == ConfigInfo.RERR_UNICAST_VAL) {
			adrList = new InetAddress[1];
			seqList = new int[1];

			for(i = 0; i < array.length; i++) {
				entry = (RouteEntry) array[i];

				// send RERR only if the 'link break' (destRte) route
				// was the next hop
				if(!entry.destIPAddr.equals(destRte.destIPAddr)
				     && entry.nextHopIPAddr.equals(destRte.destIPAddr)) {
					adrList[0] = entry.destIPAddr;
					seqList[0] = entry.destSeqNum;

					if(entry.validDestSeqNumFlag == RouteEntry.DEST_SEQ_FLAG_VALID) {
						entry.destSeqNum++;
					}

					// send RERR to each precursor
					for(j = 0; j < entry.precursorList.size(); j++) {
						rerr = new RERR(cfgInfo, this, false,
							(InetAddress) entry.precursorList.get(j), (short) 1,
							false, (byte) 1, adrList, seqList);
						
						if (m_PSR_IPktSender.m_pIntf!=null)
							m_PSR_IPktSender.m_pIntf.sendMessage(rerr);
						else{
							//	log
							if (log!=null)
								log.write(ILog.CRITICAL_LOGGING,
								"AODVState - Receptacle of interface type IPacketSender is not connected."
								);
							break;
						}
					}

					// invalidate route & start route delete
					entry.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_INVALID;
					entry.expiryTime = (new Date()).getTime()
								    + cfgInfo.deletePeriodVal;
					entry.activeMinder = new DeleteMinder(this,
													entry.destIPAddr, cfgInfo.deletePeriodVal);
					entry.activeMinder.start();

					rtList.update(entry.destIPAddr, entry);
				}
			}

			// send RERR to the precursors of the link broken route
			adrList[0] = destRte.destIPAddr;
			seqList[0] = destRte.destSeqNum;
			// send RERR to each precursor
			for(j = 0; j < destRte.precursorList.size(); j++) {
				rerr = new RERR(cfgInfo, this, false,
					(InetAddress) destRte.precursorList.get(j), (short) 1,
					false, (byte) 1, adrList, seqList);
				
				
				if (m_PSR_IPktSender.m_pIntf!=null)
					m_PSR_IPktSender.m_pIntf.sendMessage(rerr);
				else{
					//	log
					log.write(ILog.CRITICAL_LOGGING,
						"AODVState - Receptacle of interface type IPacketSender is not connected."
						);
					break;
				}
			}


		// if RERR multicast, send one RERR with all the invalidating
		// destinations
		} else {
			unreachableIPList = new ArrayList();
			unreachableSeqList = new ArrayList();

			// collect all the destinations that become
			// invalid & start route delete
			for(i = 0; i < array.length; i++) {
				entry = (RouteEntry) array[i];

				// collect dest only if the 'link break' (destRte) route
				// was the next hop

				if(!entry.destIPAddr.equals(destRte.destIPAddr)
				   && entry.nextHopIPAddr.equals(destRte.destIPAddr)) {
					unreachableIPList.add(entry.destIPAddr);
					unreachableSeqList.add(new Integer(entry.destSeqNum));

					// invalidate route & start route delete
					entry.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_INVALID;
					entry.expiryTime = (new Date()).getTime()
								    + cfgInfo.deletePeriodVal;
					entry.activeMinder = new DeleteMinder(this,	entry.destIPAddr, 
															cfgInfo.deletePeriodVal);
					entry.activeMinder.start();

					rtList.update(entry.destIPAddr, entry);
				}
			}

			// add the link broken IP address
			unreachableIPList.add(destRte.destIPAddr);
			unreachableSeqList.add(new Integer(destRte.destSeqNum));

			if(unreachableIPList.size() > 0) {
				adrList = (InetAddress []) unreachableIPList.toArray();
				seqList = new int[unreachableSeqList.size()];
				for(i = 0; i < seqList.length; i++) {
					seqList[i] = ((Integer) unreachableSeqList.get(i)).intValue();
				}
				rerr = new RERR(cfgInfo, this, true,
						cfgInfo.ipAddressMulticastVal, (short) 1,
						false, (byte) unreachableIPList.size(),
						adrList, seqList);
				
				if (m_PSR_IPktSender.m_pIntf!=null)
					m_PSR_IPktSender.m_pIntf.sendMessage(rerr);
				else{
					//	log
					log.write(ILog.CRITICAL_LOGGING,
						"AODVState - Receptacle of interface type IPacketSender is not connected."
						);
					
				}
			}
		}
		
	}

	public synchronized RouteEntry removeRoute(InetAddress key) {
		
		return rtList.remove(key);
	}

	public synchronized void updateRouteTable(InetAddress key, RouteEntry entry) throws Exception {
		
		rtList.update(key, entry);
		
	}
	

	/**
	 * @return Returns the lastRREQID.
	 */
	public int getLastRREQID() {
		return lastRREQID;
	}

	/**
	 * @param lastRREQID The lastRREQID to set.
	 */
	public void setLastRREQID(int lastRREQID) {
		this.lastRREQID = lastRREQID;
	}

	/**
	 * @return Returns the lastSeqNum.
	 */
	public int getLastSeqNum() {
		return lastSeqNum;
	}

	/**
	 * @param lastSeqNum The lastSeqNum to set.
	 */
	public void setLastSeqNum(int lastSeqNum) {
		this.lastSeqNum = lastSeqNum;
	}

	public ILog getLog() {
		return (ILog)getConnectedSinkComp(m_PSR_ILog);
	}

	/**
	 * @return Returns a reference to the component connected to the m_PSR_IConfigInfo
	 * receptacle
	 */
	public IConfigInfo getConnectedConfigInfo() {
		return (IConfigInfo)getConnectedSinkComp(m_PSR_IConfigInfo);
	}
	
	
	/**
	 * @return Returns a reference to the component connected to the m_PSR_IGui
	 * receptacle
	 */
	public IGui getConnectedGUI(){
		return (IGui)getConnectedSinkComp(m_PSR_IGui);
	}

	/**
	 * @return Returns a reference to the component connected to the m_PSR_IOSOperations
	 * receptacle.
	 */
	public IOSOperations getConnectedOSOperations() {
		return (IOSOperations)getConnectedSinkComp(m_PSR_IOSOperations);
	}

	/**
	 * @return Returns a reference to the component connected to the m_PSR_IPktSender
	 * receptacle.
	 */
	public IPacketSender getConnectedPktSender() {
		return (IPacketSender)getConnectedSinkComp(m_PSR_IPktSender);
	}

	/**
	 * @return Returns the rtList.
	 */
	public RouteList getRtList() {
		return rtList;
	}


	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#clearRoutingTable()
	 */
	public void clearRoutingTable() {
		rtList.stop();
	}


	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#getRouteArray()
	 */
	public RouteEntry[] getRouteArray() throws Exception {
		
		return rtList.getRouteArray();
	}
	
	
	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#getRouteCount()
	 */
	public int getRouteCount() {
		
		if (rtList ==null)
			return 0;
		else
			return rtList.getRouteCount();
	}
	


	/* (non-Javadoc)
	 * @see interfaces.IState.IAodvState#doUnexpiredRoutesExist()
	 */
	public boolean doUnexpiredRoutesExist() {
		
		return rtList.doUnexpiredRoutesExist();
	}
	
// ------------------------- IHello Interface --------------------------
	
	public void startHello()
	{
		helloMinder.start();
	}
	
	public synchronized int checkHelloReceived(InetAddress da, HelloReceiptMinder hrMinder) throws Exception {

		RouteEntry destRte;
		long currTime;
		
		// get ref to ConfigInfo component
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		// get route from route list
		destRte = rtList.get(da);
		if(destRte == null)
			return 0;

		if(destRte.helloReceiptMinder != hrMinder)
			return 0;

		if(destRte.hopCount > 1)
			return 0;

		currTime = (new Date()).getTime();

		// if no hellos heard but route is active, then generate RERRs
		// to all precursors and expire all the destinations which are
		// reachable thru this route and also expire himself
		if(currTime > destRte.nextHelloReceiveTime
			&& destRte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

			invalidateDestinations(destRte);

			// remove the route
			destRte.routeStatusFlag = RouteEntry.ROUTE_STATUS_FLAG_INVALID;
			destRte.destSeqNum++;
			destRte.expiryTime = currTime + cfgInfo.deletePeriodVal;
			destRte.activeMinder = new DeleteMinder(this, destRte.destIPAddr, 
													cfgInfo.deletePeriodVal);
			destRte.activeMinder.start();

			rtList.update(da, destRte);

			return 0;
		}

		return (int) (destRte.nextHelloReceiveTime - currTime);
	}

	public synchronized boolean sendHello(HELLO hm) throws Exception {
		
		if (cfgInfo == null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		
		
		//	check if router is active
		if(!cfgInfo.RouterActive) {
			return false;
		}

		// send hello only if any active unexpired routes exist
		if(doUnexpiredRoutesExist()) {
			
			m_PSR_IPktSender.m_pIntf.sendMessage(hm);
		}

		return true;
	}
	

	// -------------------- ILifeCycle Operations --------------------

	/* (non-Javadoc)
	 * @see OpenCOM.ILifeCycle#shutdown()
	 */
	public boolean shutdown() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see OpenCOM.ILifeCycle#startup(java.lang.Object)
	 */
	public boolean startup(Object data) {
		
		
		return false;
	}

	// ---------------------- IConnections Operations ------------------
	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#connect(OpenCOM.IUnknown, java.lang.String, long)
	 */
	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IOSOperations.IOSOperations")){
			return m_PSR_IOSOperations.connectToRecp(pSinkIntf, riid, provConnID);
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
		
		return false;
	}

	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#disconnect(java.lang.String, long)
	 */
	public boolean disconnect(String riid, long connID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IOSOperations")){
			return m_PSR_IOSOperations.disconnectFromRecp(connID);
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
		
		return false;
	}



//	 --------------------- additional OpenCOM methods -----------------
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


	
}
