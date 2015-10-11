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

package os;

import interfaces.IConfigInfo.IConfigInfo;
import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import log.Logging;
import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import aodvstate.ConfigInfo;
import aodvstate.RouteEntry;
import exceptions.RoutingCFException;

/**
* This class defines all the functions related to manipulating
* the routing environment of a Windows/IPv4 environment. Implements
* the IOSOperations interface 
*
* @author : Koojana Kuladinithi
* @date : 06-oct-2007
* @email : koo@comnets.uni-bremen.de
* 
* Componentised by: Rajiv Ramdhany
* Date: 12/02/2006
* Email: r.ramdhany@comp.lancs.ac.uk
*
*/
public class OSOperationsWindowsIPv4 extends OpenCOMComponent implements IOSOperations, 
												IUnknown, IMetaInterface, ILifeCycle, 
												IConnections{
	
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo; 	// To connect to ConfigInfo component
	public OCM_SingleReceptacle<ILog> m_PSR_ILog; 					// To connect to Logging component
	public ConfigInfo cfgInfo;
	

	private Process proc;
	private String cmd, str, s;
	private BufferedReader stdInput, stdError;


	/**
	* Constructs the object
	*/
	public OSOperationsWindowsIPv4(IUnknown pRuntime) {
		super(pRuntime);
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
	}
	
	
	/**
	* Method to initialize the routing environment
	* in a Windows IPv4 environment to perform AODV protocol
	* handling
	* @param int level - The initialization level
	*			0 = full initialization level
	*			1 - 100 = other init levels
	* @return int - returns the success or failure
	*/
	public int initializeRouteEnvironment(int level) {

		try {
			switch(level) {
				case 0:
					fullInit();
					break;
				case 1: 
					arpInit();
					break;
				default: // do nothing
					break;
			}
                } catch(Exception e) {
			// log
            m_PSR_ILog.m_pIntf.write(Logging.CRITICAL_LOGGING, "OS Ops IPv4 - Init failed");

			return 1;
                }
        return 0;
	}


	private void fullInit() throws Exception {
		
		
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

		 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - Init started");

		// set gateway MAC address to 00:00:00:00:00:00
		cmd = cfgInfo.pathToSystemCmdsVal + "\\arp -s "
				+ cfgInfo.ipAddressGatewayVal.getHostAddress()
				+ " 00-00-00-00-00-00 "
				+ cfgInfo.ipAddressVal.getHostAddress();


		 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - " + cmd);
         proc = Runtime.getRuntime().exec(cmd);
		proc.waitFor();

		stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		str = new String();
                while((s = stdError.readLine()) != null) {
                	str += s;
		}
		if(str.trim().length() > 0) {
			throw new Exception(str);
		}

		// set machine to act as router , has to be run in the background
		cmd = cfgInfo.pathToSystemCmdsVal + "\\EnableRouter ";

		 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - " + cmd);
                proc = Runtime.getRuntime().exec(cmd);

		// log
		 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - Init completed");
	}

	private void arpInit() throws Exception {
		
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		// set gateway MAC address to 00:00:00:00:00:00
		cmd = cfgInfo.pathToSystemCmdsVal + "\\arp -s "
				+ cfgInfo.ipAddressGatewayVal.getHostAddress()
				+ " 00-00-00-00-00-00 "
				+ cfgInfo.ipAddressVal.getHostAddress();

		proc = Runtime.getRuntime().exec(cmd);
		proc.waitFor();
	}

	/**
	* Method to add a route entry in the routing environment
	* of a Windows/IPv4 environment.
	* @param RouteEntry rtEntry - the route entry from which to get
	*				information
	* @return int - returns the success or failure
	*/
	public int addRoute(RouteEntry rtEntry) {
		try {
			
            if (cfgInfo==null)
            	throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);


			// the "nexthop via" clause is required only if the destination is
			// not in link local network (i.e. if only hop count > 1)
			if(rtEntry.destIPAddr.equals(rtEntry.nextHopIPAddr)) {
				cmd = cfgInfo.pathToSystemCmdsVal + "\\IpRoute -a "
					+ rtEntry.destIPAddr.getHostAddress()
					+ " 255.255.255.255 "
					+ rtEntry.destIPAddr.getHostAddress() + " "
					+ cfgInfo.ipAddressVal.getHostAddress();
			} else {
				cmd = cfgInfo.pathToSystemCmdsVal + "\\IpRoute -a "
					+ rtEntry.destIPAddr.getHostAddress()
					+ " 255.255.255.255 "
					+ rtEntry.nextHopIPAddr.getHostAddress() + " "
					+ cfgInfo.ipAddressVal.getHostAddress();
			}
			 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - Route add  " + cmd);
                        proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();

			// TODO : check for errors

		} catch(Exception e) {
			// log
			 m_PSR_ILog.m_pIntf.write(Logging.CRITICAL_LOGGING, "OS Ops IPv4 - Route add failed");
                        return 1;
                }
                return 0;
	}

	/**
	* Method to remove a route entry in the routing environment of
	* a Windows/IPv4 environment
	* @param RouteEntry rtEntry - the route entry from which to get
	*				information
	* @return int - returns the success or failure
	*/
	public int deleteRoute(RouteEntry rtEntry) {

           try {
        	   
        	
       		if (cfgInfo==null)
       			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);

			cmd = cfgInfo.pathToSystemCmdsVal + "\\IpRoute -d "
				+ rtEntry.destIPAddr.getHostAddress();
			 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - Route delete " + cmd);
                        proc = Runtime.getRuntime().exec(cmd);

			// remove entry from ARP cache; only for routes in the link
			// network
			if(rtEntry.destIPAddr.equals(rtEntry.nextHopIPAddr)) {
				cmd = cfgInfo.pathToSystemCmdsVal + "\\arp -d "
					+ rtEntry.destIPAddr.getHostAddress();
			}

			// TODO : check for errors

                } catch(Exception e) {
			 m_PSR_ILog.m_pIntf.write(Logging.CRITICAL_LOGGING, "OS Ops IPv4 - Route delete failed");
                        return 1;
                }
                return 0;
	}

	/**
	* Method to put the route environment to the original state
	* before terminating the protocol handler in Windows/IPv4
	* environment.
	* @return int - returns the success or failure
	*/
	public int finalizeRouteEnvironment() {

		try {
			
			if (cfgInfo==null)
				throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
			 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - Termination started");
			cmd = cfgInfo.pathToSystemCmdsVal + "\\arp -d "
				+ cfgInfo.ipAddressGatewayVal.getHostAddress();
			 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - " + cmd);
                        proc = Runtime.getRuntime().exec(cmd);

                        stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			str = new String();
                        while((s = stdError.readLine()) != null) {
                                str += s;
			}
			if(str.trim().length() > 0) {
				throw new Exception(str);
			}

			 m_PSR_ILog.m_pIntf.write(Logging.INFO_LOGGING, "OS Ops IPv4 - Termination completed");

                } catch(Exception e) {
			 m_PSR_ILog.m_pIntf.write(Logging.CRITICAL_LOGGING, "OS Ops IPv4 - Termination failed");
                        return 1;
                }
                return 0;
	}

//	 ----------------- ILifecycle Interface ---------------------
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

//	 ----------------- IConnections Interface --------------------

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
