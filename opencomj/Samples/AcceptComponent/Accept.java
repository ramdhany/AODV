/*
 * Accept.java
  *
 * OpenCOMJ is a flexible component model for reconfigurable reflection developed at Lancaster University.
 * Copyright (C) 2005 Paul Grace
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package Samples.AcceptComponent;

// OpenCOM and Java
import OpenCOM.*;
import java.util.*;
/**
 * This component implements a simple checking mechanism for the calculator framework.
 * i.e. Both receptacles on the internal calculator must be connected.
 * @see OpenCOM.IAccept
 * @author  Paul Grace
 * @version 1.3.2
 */
public class Accept extends OpenCOMComponent implements IUnknown, IAccept, IMetaInterface, ILifeCycle, IConnections {
 
    /**
     * Require use of both the meta architecture and meta interface MOPs
     */
    public OCM_SingleReceptacle<IMetaArchitecture> m_PSR_IMetaArchitecture;
    public OCM_SingleReceptacle<IMetaInterface> m_PSR_IMetaInterface;
    
    /** Creates a new instance of Accept component */
    public Accept(IUnknown pRuntime) {
        super(pRuntime);
        m_PSR_IMetaArchitecture = new OCM_SingleReceptacle<IMetaArchitecture>(IMetaArchitecture.class);
        m_PSR_IMetaInterface = new OCM_SingleReceptacle<IMetaInterface>(IMetaInterface.class);
        
        // Connect ourself to the meta-architecture of the runtime
        m_PSR_IOpenCOM.m_pIntf.connect(this, pRuntime, "OpenCOM.IMetaArchitecture");          
    }

    // ILifeCycle Interface
    public boolean shutdown() {
        return true;
    }
    
    public boolean startup(Object pIOCM) {
        return true;
    }
    
    // IAccept Interface implementation -- Programmatic check
    /**
     * This method performs validation checks on CF graphs.
     * @param graph A Vector containing the internal graph of the composite component framework to check
     * @param Intfs A Vector describing the list of interfaces exposed by the component framework
     * @param cComps An integer representing the number of components in the graph
     * @param cIntfs An integer describing the number of exposed interfaces
     * @return A boolean indicating whether the CF contains a valid or invalid configuration
     **/
    public boolean isValid(Vector<IUnknown> graph, Vector<CFMetaInterface.ExposedInterface> Intfs, int cComps, int cIntfs) {
        /*
         * Hand coded check on the validity of the calculator framework. 
         * In this case we want 3 components - with the calculator's receptacles
         * both connected.
         */ 
        Vector<Long> pConnsIDS =  new Vector<Long>();
        if(cComps==3){
            for(int index = 0; index<cComps; index++){
                IUnknown component = (IUnknown) graph.get(index);
                Class clsid =  m_PSR_IOpenCOM.m_pIntf.getComponentCLSID(component);
                if(clsid.getName().equalsIgnoreCase("CalculatorComponent.Calculator")){
                    
                    long connID = m_PSR_IOpenCOM.m_pIntf.connect(this, component, "OpenCOM.IMetaInterface");
                    Vector<OCM_RecpMetaInfo_t> ppRecpMetaInfo = new Vector<OCM_RecpMetaInfo_t>();
                    int recps = m_PSR_IMetaInterface.m_pIntf.enumRecps(ppRecpMetaInfo);
                    m_PSR_IOpenCOM.m_pIntf.disconnect(connID);
                   
                    // Check there are two connections - one from each receptacle
                    for(int index2 = 0; index2<recps; index2++){
                        int recpsConnCount = m_PSR_IMetaArchitecture.m_pIntf.enumConnsFromRecp(component, ppRecpMetaInfo.get(index2).iid, pConnsIDS);
                        if(recpsConnCount<1)
                            return false;
                    }
                }   
            }
            return true;
        }
        else if(cComps==0){
            if(Intfs.size()>0)
                return false;
            else
                return true;
        }
        return false;
    }    
    
    // IConnections Interface
    public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
        if(riid.toString().equalsIgnoreCase("OpenCOM.IMetaArchitecture")){
		return m_PSR_IMetaArchitecture.connectToRecp(pSinkIntf, riid, provConnID);
	}
        else if(riid.toString().equalsIgnoreCase("OpenCOM.IMetaInterface")){
		return m_PSR_IMetaInterface.connectToRecp(pSinkIntf, riid, provConnID);
	}
	return false;
    }
    
    public boolean disconnect(String riid, long connID) {
	if(riid.toString().equalsIgnoreCase("OpenCOM.IMetaArchitecture")){
		return m_PSR_IMetaArchitecture.disconnectFromRecp(connID);
	}
        else if(riid.toString().equalsIgnoreCase("OpenCOM.IMetaInterface")){
		return m_PSR_IMetaInterface.disconnectFromRecp(connID);
	}

	return false;
    }
 
}