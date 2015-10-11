/*
 * VisualTest.java
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

package Samples.CalculatorFramework;
import OpenCOM.*;
import Samples.AdderComponent.*;
import Samples.AcceptComponent.*;
import Samples.SubtractComponent.*;
import Samples.CalculatorComponent.*;
import Samples.Interceptors.*;
import java.util.*;
import java.io.*;
/**
 * Visualises a component framework after it has been deployed.
 * @author  Paul Grace
 * @version 1.3.1
 */
public class VisualTest {
    
    public static ICalculator pICalc = null;
    
    /** Creates a new instance of TestProgram */
    public VisualTest() {
    }
    
    static class newThread extends Thread{
        
        long time = 0;

        public newThread(long i){
            time=i;
        }
        
        public void run(){
            try{
                System.out.println("Started blocked execution inside Framework (Cannot reconfigure until end)");
                pICalc.Wait(time);
                System.out.println("Ended blocked execution inside Framework");
            }
            catch(Exception e){
                e.printStackTrace();
                return;
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Create the OpenCOM runtime & Get the IOpenCOM interface reference
        OpenCOM runtime = new OpenCOM();
        IOpenCOM pIOCM =  (IOpenCOM) runtime.QueryInterface("OpenCOM.IOpenCOM");
        
        // Create the CF component
        IUnknown pCFIUnk = (IUnknown) pIOCM.createInstance("Samples.CalculatorFramework.CalculatorFramework", "Calculator Framework");
        ILifeCycle pILife =  (ILifeCycle) pCFIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        IUnknown pAcceptIUnk = (IUnknown) pIOCM.createInstance("Samples.AcceptComponent.Accept", "Calculator Validator/Accept");
        pILife =  (ILifeCycle) pAcceptIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        long connID1 = pIOCM.connect(pCFIUnk, pAcceptIUnk, "OpenCOM.IAccept");
        
        ICFMetaInterface pCF = (ICFMetaInterface) pCFIUnk.QueryInterface("OpenCOM.ICFMetaInterface");
        
        // Try an invalid configuration
        pCF.init_arch_transaction();
        IUnknown pAdder = pCF.create_component("Samples.AdderComponent.Adder", "Adder");
        IUnknown pCal = pCF.create_component("Samples.CalculatorComponent.Calculator", "Calculator");
       
        // Connect the local components
        long connid = pCF.local_bind( pCal, pAdder, "Samples.AdderComponent.IAdd");
        boolean get =  pCF.expose_interface("Samples.CalculatorComponent.ICalculator", pCal);
        boolean success = pCF.commit_arch_transaction();
        if(!success){
            System.out.println("First confiugration is an Invalid configuration");
            System.out.println();
        }
        
        pCF.init_arch_transaction();
        pAdder = pCF.create_component("Samples.AdderComponent.Adder", "Adder");
        pCal = pCF.create_component("Samples.CalculatorComponent.Calculator", "Calculator");
        IUnknown pSub = pCF.create_component("Samples.SubtractComponent.Subtract", "Subtract");

        // Connect the local components
        long connid2 = pCF.local_bind( pCal, pAdder, "Samples.AdderComponent.IAdd");
        long connid3 = pCF.local_bind( pCal, pSub, "Samples.SubtractComponent.ISubtract");
        get =  pCF.expose_interface("Samples.CalculatorComponent.ICalculator", pCal);

        success = pCF.commit_arch_transaction();
        if(!success){
            System.out.println("Invalid configuration");
        }
               
        
        Vector<IUnknown> ppComps = new Vector<IUnknown>();
        int noComps = pCF.get_internal_components(ppComps);
        System.out.println("The number of components is : "+noComps);
        for (int i=0; i< noComps; i++){
             System.out.println("Component "+i+" is "+pIOCM.getComponentName((IUnknown) ppComps.get(i)));
        }
        System.out.println();
        
        pICalc = (ICalculator) pCFIUnk.QueryInterface("Samples.CalculatorComponent.ICalculator");
         // Lets test the Add and Subtract component
        System.out.println("The value of 18+19 = "+ pICalc.add(18,19));
        System.out.println("The value of 63-16 = "+ pICalc.subtract(63,16));
        System.out.println();
        
        Vector<CFMetaInterface.ConnectedComponent> ppConnections = new Vector<CFMetaInterface.ConnectedComponent>();
        int val = pCF.get_bound_components(pCal, ppConnections);
        System.out.println("There are "+val+" components bound to the calculator");
        for(int i=0; i<val;i++){
            OCM_ConnInfo_t TempConnInfo = pIOCM.getConnectionInfo(ppConnections.get(i).Connection);
                System.out.println("Component "+ TempConnInfo.sinkComponentName + " is connected to " + 
                    TempConnInfo.sourceComponentName + " on interface " + TempConnInfo.interfaceType); 
        }
        System.out.println();
	/////////////////////////////////////////////////////////////////////////////
	// Method - get_internal_bindings 											
	// Description - Returns a list with the ids of all bindings that are		
	// part of the base-level composition												
	/////////////////////////////////////////////////////////////////////////////
        Vector<Long> ppConnIDs = new Vector<Long>();
	int val2 = pCF.get_internal_bindings(ppConnIDs);
        System.out.println("There are "+val2+" internal connection in the calculator framework");
	for(int index=0; index<val2; index++){
                OCM_ConnInfo_t TempConnInfo = pIOCM.getConnectionInfo(ppConnIDs.get(index).longValue());
                System.out.println("Component "+ TempConnInfo.sinkComponentName + " is connected to " + 
                    TempConnInfo.sourceComponentName + " on interface " + TempConnInfo.interfaceType); 
        }
        System.out.println();

        /*
         * Visualise the current system (snapshot)
         */
        IDebug pDebug = (IDebug) runtime.QueryInterface("OpenCOM.IDebug");
        pDebug.visualise();
        
	/////////////////////////////////////////////////////////////////////////////
	// Method - get_exposed_interfaces 											
	// Description - Returns a list with the interface ids of all exposed 
	// interfaces
	/////////////////////////////////////////////////////////////////////////////
        Vector<String> ppIntfs = new Vector<String>();
	int val3 = pCF.get_exposed_interfaces(ppIntfs);
        System.out.println("There are "+val3+" exposed Interfaces:");
        for(int i=0; i<val3; i++){
            System.out.println(ppIntfs.get(i)+" is exposed");
        }
        System.out.println();
        
        // Test the locking mechanism
        newThread calcWait = new newThread(10);
        calcWait.start();
        
        System.out.println("Testing the framework lock....Press the enter key to continue");
        InputStreamReader inputStreamReader = new InputStreamReader ( System.in );
        BufferedReader stdin = new BufferedReader ( inputStreamReader );
        try{
            stdin.readLine();
        }
        catch(Exception e){
        }
        pCF.init_arch_transaction();
        System.out.println("Got lock");
        boolean cfSucc2=pCF.unexpose_all_interfaces();
        boolean cfSucc=pCF.delete_component(pAdder);
        cfSucc= pCF.delete_component(pSub);
        cfSucc=pCF.delete_component(pCal);
        pCF.commit_arch_transaction();
	try{
            stdin.readLine();
        }
        catch(Exception e){
        }
        
    }
}
