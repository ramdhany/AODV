/*
 * RFConfigurator.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	8 Mar 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: RFConfigurator.java, configurator 
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

package configurator;


import interfaces.IAODVMsgProcessing.IAODVMsgProcessing;
import interfaces.IConfigInfo.IConfigInfo;
import interfaces.IConfigure.IConfigure;
import interfaces.IGui.IGui;
import interfaces.IHello.IHello;
import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;
import interfaces.IPacketSender.IPacketSender;
import interfaces.IState.IAodvState;



import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Vector;

import exceptions.RoutingCFException;

import sun.rmi.transport.proxy.CGIHandler;

import OpenCOM.Delegator;
import OpenCOM.ICFMetaInterface;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_SingleReceptacle;
import OpenCOM.OpenCOMComponent;
import aodvstate.ConfigInfo;

/**
 * The purpose of this class is to create configurations of components in the AODV
 * overlay based on an XML description of the components, and connections on sink interfaces. 
 * @author Rajiv Ramdhany
 * @date 06-03-2007
 * @email r.ramdhany@comp.lancs.ac.uk
 *
 */
public class RFConfigurator extends OpenCOMComponent implements IConfigure, IUnknown,
		IMetaInterface, ILifeCycle, IConnections {

	public OCM_SingleReceptacle<IConfigInfo> m_PSR_IConfigInfo; 	// To connect to ConfigInfo component
	private ConfigInfo cfgInfo;
	
	
	/**
	 * @param mpIOCM
	 */
	public RFConfigurator(IUnknown mpIOCM) {
		super(mpIOCM);
		m_PSR_IConfigInfo = new OCM_SingleReceptacle<IConfigInfo>(IConfigInfo.class);
		
	}
	
	// ------------- IConfigure Interface --------------
	/* (non-Javadoc)
	 * @see interfaces.IConfigure.IConfigure#Configure(java.lang.String)
	 */
	public boolean Configure(String XMLFileName, String cfgInfoFile) {
		try{
			
			FileReader inputStream = new FileReader(XMLFileName);
			
			BufferedReader in = new BufferedReader(inputStream);
            myXML xmlroot = new myXML((BufferedReader)in);
            myXML Components = xmlroot.findElement("Components");
            
            ConfigureComponents(Components, cfgInfoFile);
            ConnectComponents(Components);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
	}

	public boolean ReConfigure(ICFMetaInterface pMeta, String XMLFileName) {
		// TODO Auto-generated method stub
		//????????????????????????????????????????????????
		//???????????? TO BE IMPLEMENTED ?????????????????
		//????????????????????????????????????????????????
		
		return false;
	}
	
	
	
	/**
     * Takes the list of components described in XML. Using this information to instantiate
     * components within the framework.
     * @param pXMLComponents The XML node holding a list of component descriptions
     * @return Boolean indicating operation success.
     * @see org.w3c.dom.Node
     */
    private Vector ConfigureComponents(myXML pXMLComponents, String cfgInfoFile) throws Exception{

        Vector<IUnknown> Components = new Vector<IUnknown>(pXMLComponents.size());
        myXML pCompInstance = null; 
        myXML p=null;
        for(int i=0; i< pXMLComponents.size(); i++){
            pCompInstance = pXMLComponents.getElement(i);
            p = pCompInstance.findElement("ComponentName");
            if(p!=null){
                String compName = (String) p.getValue();
                p = pCompInstance.findElement("ComponentType");
                String compID = (String) p.getValue();
                IUnknown pNew = (IUnknown) m_PSR_IOpenCOM.m_pIntf.createInstance(compID, compName);
                
                Components.add(pNew);
                ILifeCycle pILife = (ILifeCycle) pNew.QueryInterface("OpenCOM.ILifeCycle");
                pILife.startup(null);
                
                
                if (compID.equalsIgnoreCase("aodvstate.ConfigInfo")){
                	IConfigInfo pIConfig = (IConfigInfo) pNew.QueryInterface(
                			"interfaces.IConfigInfo.IConfigInfo");
                	pIConfig.initialise(cfgInfoFile);
                }
 
                
            }
        }
        return Components;
    }
	
	
    /**
     * Takes the list of components described in XML. This contains the information about how components
     * need to be connected together. This is extracted and used to perform meta-operations.
     * @param pXMLComponents The XML node holding a list of component descriptions
     * @return Boolean indicating operation success.
     * @see org.w3c.dom.Node
     */
    private boolean ConnectComponents(myXML pXMLComponents){

        myXML pCompInstance = null; 
        myXML pConnections =null;
        myXML pConnectionInstance =null;
        
        for(int i=0; i< pXMLComponents.size(); i++){
            pCompInstance = pXMLComponents.getElement(i);
            
            myXML p = pCompInstance.findElement("ComponentName");
            
            if(p!=null){
                String compName = (String) p.getValue();

                pConnections =  pCompInstance.findElement("Connections");
                if(pConnections!=null){
                    for(int j=0; j< pConnections.size(); j++){
                        pConnectionInstance = pConnections.getElement(j);
                        myXML q = pConnectionInstance.findElement("Name");
                        String conName = (String) q.getValue();

                        q = pConnectionInstance.findElement("Sink");
                        String compSink = (String) q.getValue();

                        q = pConnectionInstance.findElement("Interface");
                        String compIntf = (String) q.getValue();

                        IUnknown pSource = m_PSR_IOpenCOM.m_pIntf.getComponentPIUnknown(compName);
                        IUnknown pSink = m_PSR_IOpenCOM.m_pIntf.getComponentPIUnknown(compSink);
                        
                        long connid = m_PSR_IOpenCOM.m_pIntf.connect(pSource, pSink, compIntf);
                        
                     }
                 }
            }
        }
        
        // connect configurator receptacles as well
        IUnknown pSource = m_PSR_IOpenCOM.m_pIntf.getComponentPIUnknown("Configurator");
        IUnknown pConfigInfoSink = m_PSR_IOpenCOM.m_pIntf.getComponentPIUnknown("ConfigInfo");

        long connid = m_PSR_IOpenCOM.m_pIntf.connect(pSource, pConfigInfoSink, "interfaces.IConfigInfo.IConfigInfo");
           
        return true;  
    }
	
	
	
	   
	
	// --------------- ILifecycle interface --------------
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
		// TODO Auto-generated method stub
		return false;
	}

	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.connectToRecp(pSinkIntf, riid, provConnID);
		}
		return false;
	}

	public boolean disconnect(String riid, long connID) {
		
		if(riid.toString().equalsIgnoreCase("interfaces.IConfigInfo.IConfigInfo")){
			return m_PSR_IConfigInfo.disconnectFromRecp(connID);
		}
		
		return false;
	}

	
	// ---------------- OpenCOM additional methods -----------------------
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
