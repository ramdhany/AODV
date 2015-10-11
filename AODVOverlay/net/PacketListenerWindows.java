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

package net;

import interfaces.IAODVMsgProcessing.IAODVMsgProcessing;
import interfaces.IConfigInfo.IConfigInfo;
import interfaces.IControl.IControl;
import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;
import interfaces.IRouteDiscovery.IRouteDiscovery;
import interfaces.IState.IAodvState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.LinkedList;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.UDPPacket;
import log.Logging;
import msg.AODVMessage;
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
import exceptions.RoutingCFException;




/**
* This class handles the threads related to listening for packets and processing
* the received packets. There are 3 threads,
*	1. thread to capture packets on the LO interface
*       2. thread to capture packets that traverse the AODV capable
*          network interface (eg. eth1, eth2, etc.)
*       3. thread to process that packets that are placed in a queue by
*          the previous threads
*
* There can be 3 types of actions that the processing thread can request
*	1. A packet that was received on network interface and is a UDP packet
*          with destination port 654 - this means an AODV message that needs
           to be processed
*	2. A packet that was picked up by the LO interface that does not
*          contain the "127.." address or the destination MAC address is 00:00:00:00:00:00
*          - this means this destination IP Address of this packet has no route made,
*          therefore requires a route discovery to be initiatedn
*	3. Any other packet on the network interface - this means it is a packet
*          that uses a route, so update the route lifetimes
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
*
* @modification-history-ver-0.11
* @author : Rajiv Ramdhany
* @date : 30-nov-2007
* @email : r.ramdhany@lancaster.ac.uk
* @modification - Changed the class to process packets of LO interface, for the
*                 purpose of getting packets that require routes from the LO
*                 interface
*
*/
public class PacketListenerWindows extends OpenCOMComponent implements Runnable, IUnknown,
IMetaInterface, ILifeCycle, IConnections {
	
	
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo;
	public OCM_SingleReceptacle<IControl> m_PSR_IAodvControl;
	public OCM_SingleReceptacle<IAodvState> m_PSR_IAodvState;
	public OCM_SingleReceptacle<ILog> m_PSR_ILog;
	public OCM_SingleReceptacle<IOSOperations> m_PSR_IOSOperations;

	private LinkedList pktQueue;
	private IPPacket ipPacket;
	private UDPPacket udpPkt;
	private EthernetPacket ethPkt;
	private IPPkt ipPkt;
	private RREQ rreqMsg;
	private RREP rrepMsg;
	private RERR rerrMsg;
	private RREPACK rrepackMsg;
	private InetAddress aodvMsgSrcIPAddr;
	private byte ipAddr[];
	private boolean updateForRouteUse;
	public ConfigInfo cfgInfo;

	public PacketListenerWindows(IUnknown pRuntime) {
		super(pRuntime);

		pktQueue = new LinkedList();
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_IAodvControl = new OCM_SingleReceptacle<IControl>(IControl.class);
		m_PSR_IAodvState = new OCM_SingleReceptacle<IAodvState>(IAodvState.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
		m_PSR_IOSOperations = new OCM_SingleReceptacle<IOSOperations>(IOSOperations.class);

		
		
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		PacketInfo pktInfo;
		
		cfgInfo = null;
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		
		
		
		synchronized(pktQueue) {
				pktQueue.clear();
		}
		
		(new NetIfcPacketQueueBuilder()).start();
		
		while(true) {
			try  {
				
//				 if route inactive, stop the thread
				if(!(cfgInfo.RouterActive)) {
					return;
				}
				try {
					synchronized(pktQueue) {
						pktInfo = (PacketInfo) pktQueue.removeFirst();
					}
				} catch(Exception e) {
					pktInfo = null;
				}

				// stay a little while if no packets in the queue
				if(pktInfo == null) {
					//sleep(500);
					continue;
				}

				
				

				// if an ARP is sent to find gateway, then a route requiring
				// packet reached gateway and the MAC Adr 00-00-00-00-00-00
				// has somehow been removed. So, reset the ARP
				if(pktInfo.packet instanceof ARPPacket) {
					ipAddr = ((ARPPacket)
							pktInfo.packet).target_protoaddr;
					if(InetAddress.getByAddress(
					     ipAddr).equals(cfgInfo.ipAddressGatewayVal)) {
						
						//rtMgr.reInitRouteEnvironment(1);
						m_PSR_IOSOperations.m_pIntf.initializeRouteEnvironment(1);
						continue;
					}
				}

				//if packet is not IP, don't do anything
				if(!(pktInfo.packet instanceof IPPacket)) {
					continue;
				}

				ipPacket = (IPPacket) pktInfo.packet;

				// Only packets of the given IP version used
				if(ipPacket.version != cfgInfo.ipVersionVal) {
					continue;
				}

				// process packet according to type
				if(pktInfo.ifcType == PacketInfo.PACKET_REQUIRES_ROUTE) {
					processRouteRequiredPacket();
				} else {
					processNetIfcPacket();
				}

			} catch(Exception e) {
				m_PSR_ILog.m_pIntf.write(Logging.CRITICAL_LOGGING,
					"Packet Handler - Problem in loop - " + e);
				return;
			}
		}
	}

	/**
	* Method to process a packet that requires routes
	*
	* @exception Exception - thrown when errors occur
	*/
	void processRouteRequiredPacket() throws Exception {
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		// if the originator of packet is not me, then start local repair
		if(!(InetAddress.getByName(ipPacket.src_ip.getHostAddress()).equals(cfgInfo.ipAddressVal))) {
			// no local repair implemented
			return;
		}

		// else, this is a packet the requires a route
		IRouteDiscovery controlComp = (IRouteDiscovery) m_PSR_IAodvControl.m_pIntf;

		ipPkt = new IPPkt(cfgInfo, m_PSR_IAodvState.m_pIntf,
				ipPacket, cfgInfo.ifaceNameVal);
		controlComp.processRouteDiscovery(ipPkt);
	}

	/**
	* Method to process a packet that has come on the network interface
	* on which AODV is being supported.
	*
	* @exception Exception - thrown when errors occur
	*/
	void processNetIfcPacket() throws Exception {

		updateForRouteUse = true;
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		IAodvState pStateComp = m_PSR_IAodvState.m_pIntf;
		IAODVMsgProcessing controlComp = (IAODVMsgProcessing) m_PSR_IAodvControl.m_pIntf;
		

		// if a packet is UDP, has dest port 654 and is not originating
		// from your own machine, then this packet is an AODV message
		// that was received by your own machine
		if(ipPacket instanceof UDPPacket) {

			udpPkt = (UDPPacket) ipPacket;

			if(udpPkt.dst_port == AODVMessage.AODV_PORT) {

				aodvMsgSrcIPAddr = InetAddress.getByName(udpPkt.src_ip.getHostAddress());

				if(!aodvMsgSrcIPAddr.equals(cfgInfo.ipAddressVal)) {

					// create msg based on type and call process function
					switch(udpPkt.data[0]) {
						case AODVMessage.AODV_RREQ_MSG_CODE:
							rreqMsg = new RREQ(cfgInfo,
									pStateComp,
									udpPkt,
									cfgInfo.ifaceNameVal);
							controlComp.processAODVMsgRREQ(rreqMsg);
							break;
						case AODVMessage.AODV_RREP_MSG_CODE:
							rrepMsg = new RREP(cfgInfo,
									pStateComp,
									udpPkt,
									cfgInfo.ifaceNameVal);

							// if the RREP is a HELLO, the process differently
							if(rrepMsg.fromIPAddr.equals(rrepMsg.origIPAddr)
							   && rrepMsg.origIPAddr.equals(rrepMsg.destIPAddr)) {
								controlComp.processAODVMsgHELLO(rrepMsg);
								updateForRouteUse = false;
							} else {
								controlComp.processAODVMsgRREP(rrepMsg);
							}
							break;
						case AODVMessage.AODV_RERR_MSG_CODE:
							rerrMsg = new RERR(cfgInfo,
									pStateComp,
									udpPkt,
									cfgInfo.ifaceNameVal);
							controlComp.processAODVMsgRERR(rerrMsg);
							break;
						case AODVMessage.AODV_RREPACK_MSG_CODE:
							rrepackMsg = new RREPACK(cfgInfo,
									pStateComp,
									udpPkt,
									cfgInfo.ifaceNameVal);
							controlComp.processAODVMsgRREPACK(rrepackMsg);
							break;
					}
				}


			}
		}

		// for certain AODV messages, this update is not done
		if(updateForRouteUse) {
			// if it is not a AODV msg, then it means a packet which
			// is using an existing route
			ipPkt = new IPPkt(cfgInfo, pStateComp,
					ipPacket, cfgInfo.ifaceNameVal);
			controlComp.processExistingRouteUse(ipPkt);
		}
	}

	/**
	* This inner class defines the information that is placed in the
	* packet queue.
	*/
	public class PacketInfo {

		// values for ifcType
		public static final int PACKET_REQUIRES_ROUTE = 1;
		public static final int PACKET_IS_NORMAL = 2;

		public int ifcType;
		public Packet packet;

		public PacketInfo(int ift, Packet pkt) {
			ifcType = ift;
			packet = pkt;
		}
	}

	/**
	* This inner class processes the thread that listens to the network interface
	* to extract packets
	*/
	public class NetIfcPacketQueueBuilder extends Thread implements PacketReceiver {

		public void run() {
			try {
				
				//	choose network interface
				NetworkInterface[] devList=JpcapCaptor.getDeviceList();
				NetworkInterface prefDev = null; // preferred network interface
				
				for(int i=0; i< devList.length; i++)
				{
					if (devList[i].name.equalsIgnoreCase(cfgInfo.ifaceName))
					{
						prefDev = devList[i];
						break;
					}
				}
				
				// --- specify filter for network packets ----
				// file contains MAC address of machines to hide from this AODV node
				FileReader input = new FileReader("." + File.separator + "macfilter.txt");
				
				BufferedReader bufRead = new BufferedReader(input);
				String line;
				String strFilter="";
				String ether ="not ether src host ";
				
				line = bufRead.readLine();
				
				while(line!=null)
				{
					strFilter += ether + line.trim();
					
					line = bufRead.readLine();
					if (line!=null)
						strFilter+= " and ";
				}
				
				JpcapCaptor jpcaptor = JpcapCaptor.openDevice(prefDev, 4096, false, 20);
				
				if (strFilter.length()>0)
					jpcaptor.setFilter(strFilter, true);
				
				jpcaptor.loopPacket(-1, this);
			} catch(Exception e) {
				// log
				System.out.println(ILog.CRITICAL_LOGGING +
				"Net Ifc Packet Queue Builder - Problem in run - " + e);
			}
		}

		@SuppressWarnings("unchecked")
		public void receivePacket(Packet pkt) {

			// check each packet to figure out
			// whether the packet is normal packet or one that
			// requires a route (using the destination MAC adderss,
			// which can be 00:00:00:00:00:00
			if(((EthernetPacket)
			    pkt.datalink).getDestinationAddress().equals("00:00:00:00:00:00")) {
				synchronized(pktQueue) {
					pktQueue.addLast(new PacketInfo(PacketInfo.PACKET_REQUIRES_ROUTE,
								pkt));
				}
			} else {
				synchronized(pktQueue) {
					pktQueue.addLast(new PacketInfo(PacketInfo.PACKET_IS_NORMAL,
								pkt));
				}
			}
		}
	}
	
	// -------------------- ILifecycle interface ---------------------
	public boolean startup(Object data) {
		
		return false;
	}

	public boolean shutdown() {
		
		return false;
	}

	// ---------------------- IConnections Interface -------------------
	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IState.IAodvState")){
			return m_PSR_IAodvState.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IControl.IControl")){
			return m_PSR_IAodvControl.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IOSOperations.IOSOperations")){
			return m_PSR_IOSOperations.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		return false;
	}

	public boolean disconnect(String riid, long connID) {
		
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IState.IAodvState")){
			return m_PSR_IAodvState.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IControl.IControl")){
			return m_PSR_IAodvControl.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IOSOperations.IOSOperations")){
			return m_PSR_IOSOperations.disconnectFromRecp(connID);
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
