

package net;

import interfaces.IConfigInfo.IConfigInfo;
import interfaces.ILog.ILog;
import interfaces.IPacketSender.IPacketSender;
import exceptions.CommunicationsException;
import exceptions.RoutingCFException;
import msg.AODVMessage;
import msg.IPPkt;
import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.*;

import aodvstate.ConfigInfo;


import jpcap.*;
import jpcap.NetworkInterface;
import jpcap.packet.*;



/**
* This Class provides the functionality to transmit packets. These
* packets can be either AODV messages or other IP packets.
*
* @author : Rajiv Ramdhany, modified by Rajiv Ramdhany
* @date : 26-sep-2007, 15/02/2007
* @email : r.ramdhany@lancaster.ac.uk; r.ramdhany@lancaster.ac.uk
*
*/
public class PacketSender extends OpenCOMComponent implements IPacketSender,
IMetaInterface, ILifeCycle, IConnections{
	
	public MulticastSocket mcastSock;
	public InetAddress mcastGrpAddr;
	public JpcapSender jpcapSender;
	
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo; 	// To connect to ConfigInfo component
	public OCM_SingleReceptacle<ILog> m_PSR_ILog; 					// To connect to Logging component
	private ConfigInfo cfgInfo;
	
	
	/**
	* Constructor to create the packet sender. Constructor
	* will open the AODV socket for sending messages.
	* @param CfgInfo cfg - config info object
	* @exception Exception - thrown if errors encountered
	*/
	public PacketSender(IUnknown pRuntime) throws Exception {
		super(pRuntime);
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
	}

	
	
	
	/**
	* Method to start the packet sender. Start open the packet
	* sending connection in jpcap
	* @exception Exception - thrown if error
	*/
	public void start() throws Exception {
		
		// --- get reference to ConfigInfo Component ----
		cfgInfo = null;
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		if (cfgInfo == null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		
		
		mcastGrpAddr = cfgInfo.ipAddressMulticastVal;
		mcastSock = new MulticastSocket(AODVMessage.AODV_PORT);
		//mcastSock.joinGroup(mcastGrpAddr);
		
		// choose network interface
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
		
		if (prefDev!=null)
			jpcapSender = JpcapSender.openRawSocket(); //openDevice(prefDev);
		else
			throw new CommunicationsException(CommunicationsException.INVALID_NETWORK_INTERFACE);
	}

	/**
	* Method to stop the packet sender. Stops by leaving the
	* multicast group.
	* @exception Exception - thrown when joining group, if error
	*/
	public void stop() throws Exception {
		//mcastSock.leaveGroup(mcastGrpAddr);
		jpcapSender.close();
	}

	/**
	* Method to send a AODV message through the multicast socket.
	* @param AODVMessage msg - AODV message top send
	* @exception Exception - thrown if error
	*/
	public void sendMessage(AODVMessage msg) throws Exception {
		mcastSock.setTimeToLive(msg.ttlValue);
		mcastSock.send(msg.javaUDPDgram);

		// log
//		stateComp.log.write(Logging.INFO_LOGGING,
//				"Packet Sender - AODV Message Generated - "
//						+ msg.toString());
	}

	/**
	* Method to send a IP packet out through jpcap.
	* @param IPPkt pkt - the packet to be sent
	* @exception Exception - thrown if errors encountered
	*/
	public void sendPkt(IPPkt pkt) throws Exception {
		jpcapSender.sendPacket(pkt.jpcapIPPkt);
	}

	
	// ----------------- ILifecycle Interface ---------------------
	/* (non-Javadoc)
	 * @see OpenCOM.ILifeCycle#shutdown()
	 */
	public boolean shutdown() {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see OpenCOM.ILifeCycle#startup(java.lang.Object)
	 */
	public boolean startup(Object data) {
		
		return false;
	}


	
	// ----------------- IConnections Interface --------------------

	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#connect(OpenCOM.IUnknown, java.lang.String, long)
	 */
	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.connectToRecp(pSinkIntf, riid, provConnID);
		}
		return false;
	}



	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#disconnect(java.lang.String, long)
	 */
	public boolean disconnect(String riid, long connID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.disconnectFromRecp(connID);
		}
		return false;
	}
	
//	 ---------------- OpenCOM additional methods -----------------------
	/**
	 * 
	 */
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
