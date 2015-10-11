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
import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import aodvstate.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.*;
import java.io.*;



/**
* This class defines all the functions related to manipulating
* the routing environment of a Linux/IPv6 environment. Implements
* the OSOperationsInterface to provide a consistent interface
* to the route manager.
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
* @author modified by Rajiv Ramdhany
* Date: 12/02/2007
* Email: r.ramdhany@comp.lancs.ac.uk
*/
public class OSOperationsLinuxIPv6 extends OpenCOMComponent implements IOSOperations, 
											IUnknown, IMetaInterface, ILifeCycle,
											IConnections{
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo; 	// To connect to ConfigInfo component
	public OCM_SingleReceptacle<ILog> m_PSR_ILog; 					// To connect to Logging component
	public ConfigInfo cfgInfo;

	Process proc;
	String cmd, s;
	BufferedReader stdInput, stdError;

	/**
	* Constructs the object
	*/
	public OSOperationsLinuxIPv6(IUnknown IOCM) {
		super(IOCM);
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
	}

	/**
	* Method to initialize the routing environment
	* in a Linux IPv6 environment to perform AODV protocol
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
				default: // do nothing
					break;
			}

                } catch(Exception e) {
                        return 1;
                }
                return 0;
	}

	private void fullInit() throws Exception {
		cmd = "ip -6 neigh add " + cfgInfo.ipAddressGateway + " lladdr "
			+ ConfigInfo.MAC_ADDRESS_OF_GATEWAY + " dev " + cfgInfo.ifaceNameVal;
                proc = Runtime.getRuntime().exec(cmd);

                stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		// To see errors enable stdError print loop
		//
		//while((s = stdInput.readLine()) != null) {
               	//        System.out.println(s);
               	//}
		//
                //while((s = stdError.readLine()) != null) {
                //        System.out.println(s);
                //}
	}

	/**
	* Method to add a route entry in the routing environment
	* of a Linux/IPv6 environment.
	* @param RouteEntry rtEntry - the route entry from which to get
	*				information
	* @return int - returns the success or failure
	*/
	public int addRoute(RouteEntry rtEntry) {

                try {

			cmd = "ip -6 route add " + rtEntry.destIPAddr.getHostAddress() + " via "
				+ rtEntry.nextHopIPAddr.getHostAddress() + " dev " + rtEntry.ifaceName;
                        proc = Runtime.getRuntime().exec(cmd);

                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			// To see errors enable stdError print loop
			//
                        //while((s = stdInput.readLine()) != null) {
                        //        System.out.println(s);
                        //}
			//
                        //while((s = stdError.readLine()) != null) {
                        //        System.out.println(s);
                        //}
                } catch(Exception e) {
                        return 1;
                }
                return 0;
	}

	/**
	* Method to remove a route entry in the routing environment of
	* a Linux/IPv6 environment
	* @param RouteEntry rtEntry - the route entry from which to get
	*				information
	* @return int - returns the success or failure
	*/
	public int deleteRoute(RouteEntry rtEntry) {

                try {

			cmd = "ip -6 route del " + rtEntry.destIPAddr.getHostAddress() + " via "
				+ rtEntry.nextHopIPAddr.getHostAddress() + " dev " + rtEntry.ifaceName;
                        proc = Runtime.getRuntime().exec(cmd);

                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			// To see errors enable stdError print loop
			//
                        //while((s = stdInput.readLine()) != null) {
                        //        System.out.println(s);
                        //}
			//
                        //while((s = stdError.readLine()) != null) {
                        //        System.out.println(s);
                        //}
                } catch(Exception e) {
                        return 1;
                }
                return 0;
	}

	/**
	* Method to put the route environment to the original state
	* before terminating the protocol handler in Linux/IPv6
	* environment.
	* @return int - returns the success or failure
	*/
	public int finalizeRouteEnvironment() {

                try {

			cmd = "ip -6 neigh del " + cfgInfo.ipAddressGateway + " lladdr "
				+ ConfigInfo.MAC_ADDRESS_OF_GATEWAY + " dev " + cfgInfo.ifaceNameVal;
                        proc = Runtime.getRuntime().exec(cmd);

                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			// To see errors enable stdError print loop
			//
                        //while((s = stdInput.readLine()) != null) {
                        //        System.out.println(s);
                        //}
			//
                        //while((s = stdError.readLine()) != null) {
                        //        System.out.println(s);
                        //}
                } catch(Exception e) {
                        return 1;
                }
                return 0;
	}

	public boolean startup(Object data) {
		return true;
	}

	public boolean shutdown() {
		finalizeRouteEnvironment();
		return true;
	}


	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.connectToRecp(pSinkIntf, riid, provConnID);
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
