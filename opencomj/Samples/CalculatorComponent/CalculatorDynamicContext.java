/*
 * CalculatorDynamicContext.java
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

package Samples.CalculatorComponent;

// OpenCOM and Java
import OpenCOM.*;
import java.util.*;

// Interfaces
import Samples.AdderComponent.IAdd;
import Samples.AdderComponent.IOutput;
import Samples.SubtractComponent.ISubtract;

/**
 * Calculator component supporting arithmetic plug-in components. With a context based 
 * receptacle to select which plug-in to perform operations on. This is similar to the
 * CalculatorContext component, but this time we use a safer context transaction.
 *
 * @author  Paul Grace
 * @version 1.3.2
 */

public class CalculatorDynamicContext extends OpenCOMComponent implements ICalculator, IConnections, ILifeCycle, IUnknown, IMetaInterface {
    
    /**
     * Requires Interface of type IOutput. We want to connect this to multiple provides interfaces
     * and select one to call based upon context.
     */
    public OCM_MultiReceptacleDynamicContext<IOutput> m_PSR_IOutput;

    /**
     * Requires Interface of type IAdd.
     */
    public OCM_SingleReceptacle<IAdd> m_PSR_IAdd;
    
    /**
     * Requires Interface of type ISubtract.
     */
    public OCM_SingleReceptacle<ISubtract> m_PSR_ISubtract;

    /** Creates a new instance of Calculator */
    public CalculatorDynamicContext(IUnknown binder) {
        super(binder);
        
        // Initiate receptacles
	m_PSR_IOutput = new OCM_MultiReceptacleDynamicContext<IOutput>(IOutput.class);
        m_PSR_IAdd = new OCM_SingleReceptacle<IAdd>(IAdd.class);
        m_PSR_ISubtract = new OCM_SingleReceptacle<ISubtract>(ISubtract.class);
    }
    
    //Interface ICalculator
    /**
     * Add two integers together.
     * @param a Operand X.
     * @param b Operand Y.
     * @return The added values.
     */
    public int add(int a, int b) {
        return m_PSR_IAdd.m_pIntf.add(a, b);
    }
    
    /**
     * Concatenate a display message to the passed message.
     * @param a Operand X.
     * @param b Operand Y.
     * @return X-Y.
     */
    public int subtract(int a, int b) {
      return m_PSR_ISubtract.m_pIntf.subtract(a, b);
    }
    /**
     * Concatenate a display message to the passed message.
     * @param message The message to attach to.
     * @return The concatenated string.
     */
    public String display(String message) {
        String returnStr = message.concat(":: From Calculator");
        
        /*
         * Create a context rule - our request for interface invoke
         * Owner = "Paul Grace"
         */
        ContextRule context[] = new ContextRule[1];
        context[0]=new ContextRule("Owner", "Paul Grace");
        
        /*
         * Find out which interface has such a context match
         */
        int index = m_PSR_IOutput.getContext(context);
        
        /*
         * Invoke the indexed interface that matches.
         */
        if(index>=0)
            m_PSR_IOutput.interfaceList.get(index).DisplayMessage(message);
        return returnStr;
    }
    
    /**
     * Time passing function. Simply operates in the component
     * doing nothing for the specified time period then 
     * returns.
     * @param seconds The time to wait for.
     */
    public void Wait(long seconds) {
        long time0 = System.currentTimeMillis();
        long time1 = -1;
        while(time1 < (seconds*1000)){
            time1 = System.currentTimeMillis()-time0;
        }
    }  

    // IConnections Interface
    public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
        if(riid.toString().equalsIgnoreCase("Samples.AdderComponent.IAdd")){
		return m_PSR_IAdd.connectToRecp(pSinkIntf, riid, provConnID);
	}
        else if(riid.toString().equalsIgnoreCase("Samples.SubtractComponent.ISubtract")){
		return m_PSR_ISubtract.connectToRecp(pSinkIntf, riid, provConnID);
	}
	else if(riid.toString().equalsIgnoreCase("Samples.AdderComponent.IOutput")){
		return m_PSR_IOutput.connectToRecp(pSinkIntf, riid, provConnID);
	}
	return false;
    }
    
    public boolean disconnect(String riid, long connID) {
        
	if(riid.toString().equalsIgnoreCase("Samples.AdderComponent.IAdd")){
		return m_PSR_IAdd.disconnectFromRecp(connID);
	}
        else if(riid.toString().equalsIgnoreCase("Samples.SubtractComponent.ISubtract")){
		return m_PSR_ISubtract.disconnectFromRecp(connID);
	}
	else if(riid.toString().equalsIgnoreCase("Samples.AdderComponent.IOutput")){
		return m_PSR_IOutput.disconnectFromRecp(connID);
	}
	return false;
    }
    
    // ILifeCycle Interface
    public boolean shutdown() {
        return true;
    }
    
    public boolean startup(Object pIOCM) {
        return true;
    }  
    
}