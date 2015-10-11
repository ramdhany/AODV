/*
 * GUIComponent.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	4 Apr 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: GUIComponent.java, gui 
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

package gui;

import gui.ConfigDialog.CfgTblModel;
import interfaces.IConfigInfo.IConfigInfo;
import interfaces.IConfigure.IConfigure;
import interfaces.IControl.IControl;
import interfaces.IGui.IGui;
import interfaces.ILog.ILog;
import interfaces.IState.IAodvState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.Date;

import javax.crypto.spec.IvParameterSpec;

import configurator.RFConfigurator;
import exceptions.RoutingCFException;

import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import aodvstate.ConfigInfo;
import aodvstate.RouteEntry;

/**
 * @author ramdhany
 *
 */
public class GUIComponent extends OpenCOMComponent implements IGui, IUnknown, IMetaInterface, 
											ILifeCycle, IConnections{
	
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo;
	public OCM_SingleReceptacle<IAodvState> m_PSR_IAodvState;
	public OCM_SingleReceptacle<ILog> m_PSR_ILog;
	public OCM_SingleReceptacle<IControl> m_PSR_IControl;
	ConfigInfo cfgInfo = null;
	boolean GUILoaded;
	
	public GUIInterface gui;

	public GUIComponent(IUnknown mpIOCM) {
		super(mpIOCM);
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		m_PSR_IAodvState = new OCM_SingleReceptacle<IAodvState>(IAodvState.class);
		m_PSR_ILog = new OCM_SingleReceptacle<ILog>(ILog.class);
		m_PSR_IControl = new OCM_SingleReceptacle<IControl>(IControl.class);
		GUILoaded = false;
	}
	
	
	/* (non-Javadoc)
	 * @see gui.IGUI#loadGUI()
	 */
	public void loadGUI() throws Exception{
		// gui mode is active only if set in cfg-file
		
		
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_IConfigInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
        
		if(cfgInfo.executionModeVal.trim().toLowerCase().equals("gui")) {
			// create the gui based on the OS
			if(cfgInfo.osInUseVal.trim().toLowerCase().equals(ConfigInfo.LINUX_OS)) {
				//gui = new GUILinux(cfgInfo, stateComp, this);
			} else if(cfgInfo.osInUseVal.trim().toLowerCase().equals(ConfigInfo.WINDOWS_OS)) {
				gui = new GUIWindows(cfgInfo, m_PSR_IOpenCOM.m_pIntf, this);
			} else if(cfgInfo.osInUseVal.trim().toLowerCase().equals(ConfigInfo.ZAURUS_OS)) {
				//gui = new GUIZaurus(cfgInfo, stateComp, this);
			} else {
				gui = null;
				printNonGUIModeBanner();
			}
		} else {
			gui = null;
			printNonGUIModeBanner();
		}
		
		if (gui!=null)
			GUILoaded = true;
	}
	
	
	
	public boolean isGUILoaded() throws Exception
	{
		return GUILoaded;
	}
	
	
	
	/**
	* Prints the information when run in non GUI mode
	*/
	private void printNonGUIModeBanner() {
		System.out.println("");
		System.out.println("Press Ctrl+C to stop the application");
	}
	
	
//	 GUI Operations
	/*--------------------------------------------------------------------------------------*/

	// Methods invoked by the GUI
	// --------------------------
	
	
	/**
	* Method to provide the number of routes in the routing
	* environment(related to protocol handler) to the GUI.
	* @return int - route count
	*/
	public synchronized int getRouteCount() {
		return m_PSR_IAodvState.m_pIntf.getRouteCount();
	}

	/**
	* Method to get the number of fields (i.e. route info)
	* that would be shown on the GUI.
	* @return int - field count
	*/
	public synchronized int getFieldCount() {
		return 10;
	}

	/**
	* Method to return the value related to a field in the
	* routing table to the GUI.
	* @param int row - the data row (route entry)
	* @param int column - data column (field)
	* @return String - string value of data
	*/
	public synchronized String getRouteValueAt(int row, int column) {
		Object array[];
		RouteEntry rte;
		long lifetimeLong;
		String str;
		int i;

		// return spaces if the route entry count
		// changed before comming here
		if(row >= m_PSR_IAodvState.m_pIntf.getRouteCount())
			return " ";

		try {
			array = m_PSR_IAodvState.m_pIntf.getRouteArray();
		} catch(Exception e) {
			return " ";
		}
		
		rte = (RouteEntry) array[row];

		if(column == 0) {
			return rte.destIPAddr.getHostAddress();

		} else if(column == 1) {
			return "" + rte.destSeqNum;

		} else if(column == 2) {
			if(rte.validDestSeqNumFlag == RouteEntry.DEST_SEQ_FLAG_VALID)
				return "Valid";
			else
				return "Invalid";

		} else if(column == 3) {
			if(rte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_BEING_REPAIRED)
				return "Being Repaired";
			else if(rte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_REPAIRABLE)
				return "Repairable";
			else if(rte.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID)
				return "Valid";
			else
				return "Invalid";

		} else if(column == 4) {
			return rte.ifaceName;

		} else if(column == 5) {
			return "" + rte.hopCount;

		} else if(column == 6) {
			return rte.nextHopIPAddr.getHostAddress();

		} else if(column == 7) {
			lifetimeLong = rte.expiryTime - (new Date()).getTime();
			return (lifetimeLong >= 0 ? "" + lifetimeLong : "expired");

		} else if(column == 8) {
			if(rte.precursorList != null && rte.precursorList.size() > 0) {
				str = "";
				for(i = 0; i < rte.precursorList.size(); i++) {
					str += ((InetAddress) rte.precursorList.get(i)).getHostAddress()
						+ " ";
				}
				return str;
			}
			return "";

		} else if(column == 9) {
			lifetimeLong = rte.nextHelloReceiveTime - (new Date()).getTime();
			return (lifetimeLong >= 0 ? "" + lifetimeLong : " ");

		} else
			return " ";
	}

	/**
	* Method to return the name of the information field
	* in the routing table, to the GUI
	* @param int column - field column
	* @return String - string value of the field name
	*/
	public synchronized String getFieldName(int column) {
		if(column == 0)
			return "Destination";
		else if(column == 1)
			return "Sequence";
		else if(column == 2)
			return "Sequence Flag";
		else if(column == 3)
			return "Route Flag";
		else if(column == 4)
			return "Interface";
		else if(column == 5)
			return "Hop Count";
		else if(column == 6)
			return "Next Hop";
		else if(column == 7)
			return "Lifetime";
		else if(column == 8)
			return "Precursors";
		else if(column == 9)
			return "Hello Lifetime";
		else
			return " ";
	}
	
	/* (non-Javadoc)
	 * @see gui.IGUI#exitApplication()
	 */
	public void exitApplication() {
		m_PSR_IControl.m_pIntf.Leave(cfgInfo.NetworkIdentifier);
		System.exit(0);
	}

	/* (non-Javadoc)
	 * @see gui.IGUI#startApplication()
	 */
	public boolean startApplication() throws Exception {
		m_PSR_IControl.m_pIntf.Create(cfgInfo.NetworkIdentifier ,null);
		return true;
	}

	/* (non-Javadoc)
	 * @see gui.IGUI#stopApplication()
	 */
	public void stopApplication() {
		m_PSR_IControl.m_pIntf.Leave(cfgInfo.NetworkIdentifier);
	}
	
	/* (non-Javadoc)
	 * @see gui.IGUI#updateDisplay()
	 */
	public void updateDisplay() {
		if(gui == null)
			return;
		gui.redrawTable();
	}

	public void displayError(String msg) {
		if(gui == null)
			return;

		gui.displayError(msg);
	}
	
	
	// -------------- ILifecycle interface ----------------
	public boolean shutdown() {
		 
		return false;
	}

	public boolean startup(Object data) {
		
		return false;
	}
	
	// ---------------- IConnections interface ------------------
public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		
		if(riid.toString().equalsIgnoreCase("interfaces.IState.IAodvState")){
			return m_PSR_IAodvState.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IControl.IControl")){
			return m_PSR_IControl.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		return false;
	}

	public boolean disconnect(String riid, long connID) {
		
		if(riid.toString().equalsIgnoreCase("interfaces.IState.IAodvState")){
			return m_PSR_IAodvState.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IControl.IControl")){
			return m_PSR_IControl.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.disconnectFromRecp(connID);
		}
		
		if(riid.toString().equalsIgnoreCase("interfaces.ILog.ILog")){
			return m_PSR_ILog.disconnectFromRecp(connID);
		}
		
		return false;
	}
	
	// --------------------- additional OpenCOM methods -----------------
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
