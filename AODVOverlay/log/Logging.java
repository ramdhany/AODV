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

package log;

import interfaces.IConfigInfo.IConfigInfo;
import interfaces.ILog.ILog;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.text.*;

import exceptions.RoutingCFException;

import OpenCOM.Delegator;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import aodvstate.ConfigInfo;





/**
* This class provide functionality related to logging different activities
* that occur in the protocol handler. There are 3 types of logging,
*	critical logging - error notifications
*	activity logging - error notifications and summarized
*				activity logging
*	info logging     - error notifications, summarized activity logging
*				and detailed activity logging
*
* Based on the logging level defined in the parameters by the user and the
* predefined logging level associated with any logging call, the entry
* would be written to or not, to the given log file.
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public class Logging extends OpenCOMComponent implements ILog, IUnknown, IMetaInterface, 
														 ILifeCycle, IConnections {
	public ConfigInfo cfgInfo;
	
	public OCM_SingleReceptacle<IConfigInfo> m_PSR_cfgInfo;

	// local variables
	private BufferedWriter logFilePtr;
	private SimpleDateFormat timeFormatter;
	private String timeStr;

	

	/**
	* Constructor creates a loggin object initializing the
	* internal variables.
	* @param ConfigInfo cfg - config info object
	* @param CurrentInfo cur - current info object
	*/
	public Logging(IUnknown pRuntime) {
		super(pRuntime);
		//cfgInfo = cfg;
		m_PSR_cfgInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		logFilePtr = null;
	}

	/* (non-Javadoc)
	 * @see log.ILogging#start()
	 */
	public void start() throws Exception {
		
		cfgInfo = null;
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_cfgInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}
		
		if (cfgInfo==null)
			throw new RoutingCFException(RoutingCFException.NO_CONFIGINFO_CONNECTED);
		
		// check log status
		if(!cfgInfo.loggingStatusVal)
			return;

		// open logging file (text file)
        logFilePtr = new BufferedWriter(new OutputStreamWriter(
        								new FileOutputStream(cfgInfo.logFileVal, true)
        								));
		timeFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
	}

	/* (non-Javadoc)
	 * @see log.ILogging#stop()
	 */
	public void stop() throws Exception {
		if(!cfgInfo.loggingStatusVal)
			return;

		logFilePtr.close();

		logFilePtr = null;
	}

	/* (non-Javadoc)
	 * @see log.ILogging#write(int, java.lang.String)
	 */
	public synchronized void write(int ll, String line) {
		
		Object cfgInfoObj = getConnectedSinkComp(m_PSR_cfgInfo);
		if (cfgInfoObj instanceof ConfigInfo) {
			cfgInfo= (ConfigInfo) cfgInfoObj;
		}

		// every thread calls this to log to the
		// log file, therefore made thread safe
		synchronized(this) {
			// check if logging activated
			if(!cfgInfo.loggingStatusVal)
				return;

			// check if log level defined in parameters
			// allow this given log level to be considered
			if(ll > cfgInfo.loggingLevelVal)
				return;

			// get date
			timeStr = timeFormatter.format(new Date()) + " : " ;

			try {
				// writing to log file
				logFilePtr.write(timeStr, 0, timeStr.length());
				logFilePtr.write(line, 0, line.length());
				logFilePtr.newLine();
				logFilePtr.flush();

			} catch(Exception e) {
			}
		}
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


	
	// ----------------- IConnections Interface --------------------

	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#connect(OpenCOM.IUnknown, java.lang.String, long)
	 */
	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_cfgInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}
		
		return false;
	}



	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#disconnect(java.lang.String, long)
	 */
	public boolean disconnect(String riid, long connID) {
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_cfgInfo.disconnectFromRecp(connID);
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
