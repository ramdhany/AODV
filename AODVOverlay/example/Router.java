/*
 * Router.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	28 Mar 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: Router.java, router 
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

package example;

import interfaces.IConfigure.IConfigure;
import interfaces.IControl.IControl;
import interfaces.IForward.IForward;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import OpenCOM.IDebug;
import OpenCOM.ILifeCycle;
import OpenCOM.IOpenCOM;
import OpenCOM.IUnknown;
import OpenCOM.OpenCOM;


/**
 * @author ramdhany
 *
 */
public class Router {
	
	private IUnknown runtime;
	
	/**
	* Constructor to start the GUI and the RouteManager
	* @param String args[] - The command line argument list
	*/
	public Router(IUnknown pIOCM, String args[]) {
		runtime = pIOCM;
		printBanner();
	}

	/**
	* Prints the banner of application to the character output
	* screen.
	*/
	private void printBanner() {
		System.out.println("");
		System.out.println("AODV OpenOverlay (ver 0.2)");
		System.out.println("Implements IETF RFC, 3561");
		System.out.println("");
	}

	/**
	* Prints the usage information of the application.
	*/
	private void printUsage() {
		System.out.println("Usage : java -j router.jar [cfg-file]");
		System.out.println("        where cfg-file is the name & path");
		System.out.println("        of the configuration file, if not");
		System.out.println("        given, defaulted to ./jadhoc.cfg");
		System.out.println("");
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
			String APP_PATH = System.getProperty("user.dir") + "\\";
			String cfgInfoFile; 
			
			// Create the OpenCOM runtime & Get the IOpenCOM interface reference
	        OpenCOM runtime = new OpenCOM();
	        IOpenCOM pIOCM =  (IOpenCOM) runtime.QueryInterface("OpenCOM.IOpenCOM");
	        
	        //	check & use cfg-file, if given in command line
			if(args.length >= 1)
				cfgInfoFile = APP_PATH + args[0];
			else
				cfgInfoFile = APP_PATH + "aodv.cfg";
	        
	        new Router(pIOCM, args);
	        // ---- instantiate the Configurator ----
	        IUnknown pConfiguratorIUnk = (IUnknown) pIOCM.createInstance("configurator.RFConfigurator", "Configurator");
	        ILifeCycle pILife =  (ILifeCycle) pConfiguratorIUnk.QueryInterface("OpenCOM.ILifeCycle");
	        pILife.startup(pIOCM);
	        
	        IConfigure pIConf = (IConfigure) pConfiguratorIUnk.QueryInterface("interfaces.IConfigure.IConfigure");
	        
	        // ---- configure the framework from XML topology description ----
	        pIConf.Configure(APP_PATH + "aodvwinxp.xml", cfgInfoFile);
	        IDebug pIDebug =  (IDebug) runtime.QueryInterface("OpenCOM.IDebug");
	        pIDebug.visualise();
	        
	        // ---- create (and start) the aodv overlay ----
	        IUnknown pAodvControlIUnk = runtime.getComponentPIUnknown("AODVControl");
	        IControl pAodvIControl = (IControl) pAodvControlIUnk.QueryInterface("Interfaces.IControl.IControl");
	        
	        pAodvIControl.Create("AODV Overlay", null);
	        
	        
	        // =========== Uncomment relevant part as necessary ==============
	        IUnknown pAodvFwdIUnk = runtime.getComponentPIUnknown("AODVForward");
	        IForward pAodvIFwd = (IForward) pAodvFwdIUnk.QueryInterface("Interfaces.IForward.IForward");
	        	        
	        // ------------ to receive data packets from a node in the network -----------
	         
//	        System.out.println("Receiving data from peer: 192.168.112.2 on port 4446");
//	        byte[] recvBuf = new byte[1400];
//	        
//	        for (int i=0; i<20; i++){
//		       
//		        recvBuf = pAodvIFwd.Receive("192.168.112.2:4446");
//		        
//		        String recvStr= new String(recvBuf);
//		        System.out.println(recvStr);
//	        	
//	        }
	        
	        // ----------- to send data packets to a node in the network ---------------
	        
	        String mystr = "xxxxxxxxxxxxxxxxxxxx Testing AODV xxxxxxxxxxxxxxxxxxxxxxx";
	        byte[] sendBuf = new byte[1400];
	        sendBuf = mystr.getBytes();
	        pAodvIFwd.Send("192.168.110.23:4446", sendBuf, 0); 
	        
	        
	        
	        for(int i=1; i<=20; i++)
	        {
	        	mystr = "Testing AODV " + i;
	        	sendBuf = mystr.getBytes();
	        	pAodvIFwd.Send("192.168.110.23:4446", sendBuf, 0);
	        }
	        
	        
	        
	        // ======================================================================
	        System.out.println("Enter a character to continue");
	        InputStreamReader inputStreamReader = new InputStreamReader ( System.in );
	        BufferedReader stdin = new BufferedReader ( inputStreamReader );
            stdin.readLine();
            
        }
        catch(Exception e){
        }
        
	}

}
