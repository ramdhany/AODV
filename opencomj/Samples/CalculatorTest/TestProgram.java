/*
 * TestProgram.java
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

package Samples.CalculatorTest;

// Import OpenCOM and Java
import OpenCOM.*;
import java.util.*;

// Component Interfaces
import Samples.AdderComponent.IAdd;
import Samples.AdderComponent.IOutput;
import Samples.SubtractComponent.ISubtract;
import Samples.CalculatorComponent.ICalculator;

// Application Classes
import Samples.Interceptors.PreAndPostMethods;

/**
 * Test program for all the basic operations of the OpenCOM runtime.
 * @author  Paul Grace
 * @version 1.3.2
 */
public class TestProgram {
    
    /** Creates a new instance of TestProgram */
    public TestProgram() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Create the OpenCOM runtime & Get the IOpenCOM interface reference
        OpenCOM runtime = new OpenCOM();
        IOpenCOM pIOCM =  (IOpenCOM) runtime.QueryInterface("OpenCOM.IOpenCOM");
 
        // Create the Adder component
        IUnknown pAdderIUnk = (IUnknown) pIOCM.createInstance("Samples.AdderComponent.Adder", "Adder");
        ILifeCycle pILife =  (ILifeCycle) pAdderIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        // Create the Subtract component
        IUnknown pSubIUnk = (IUnknown) pIOCM.createInstance("Samples.SubtractComponent.Subtract", "Subtract");
        pILife =  (ILifeCycle) pSubIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        // Create the Calculator component
        IUnknown pCalcIUnk = (IUnknown) pIOCM.createInstance("Samples.CalculatorComponent.Calculator", "Calculator");
        pILife =  (ILifeCycle) pCalcIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        // Get the Calculator Interface
        ICalculator pICalc =  (ICalculator) pCalcIUnk.QueryInterface("Samples.CalculatorComponent.ICalculator");
        
        long ConnID1 = runtime.connect(pCalcIUnk, pAdderIUnk, "Samples.AdderComponent.IAdd");
        long ConnID2 = runtime.connect(pCalcIUnk, pSubIUnk, "Samples.SubtractComponent.ISubtract");
        
        // Get the debug interface and dump component configuration to console output
        IDebug pIDebug =  (IDebug) runtime.QueryInterface("OpenCOM.IDebug");
        pIDebug.dump();
        
        // Lets test the Add and Subtract component
        System.out.println("The value of 18+19 = "+ pICalc.add(18,19));
        System.out.println("The value of 63-16 = "+ pICalc.subtract(63,16));
        
        IMetaInterface pMeta2 =  (IMetaInterface) pAdderIUnk.QueryInterface("OpenCOM.IMetaInterface");
        // Test the insertion of meta data // Add values to IADD
        pMeta2.SetAttributeValue("Samples.AdderComponent.IAdd", "Interface", "Variation", "int", new Integer(8));
        

        // However, the addition is wrong by 8, so lets use interception to correct it
        IMetaInterception pIMeta = (IMetaInterception) runtime.QueryInterface("OpenCOM.IMetaInterception");
        IDelegator pIAdderDel = pIMeta.GetDelegator(pAdderIUnk, "Samples.AdderComponent.IAdd");
        
        // Add the new pre-method
        PreAndPostMethods Interceptors = new PreAndPostMethods(pIOCM);
        pIAdderDel.addPreMethod(Interceptors, "Pre0");
        pIAdderDel.addPreMethod(Interceptors, "checkAdd");
        // Lets test the Add component again
        System.out.println("The \"Intercepted value\" of 18+19 = "+ pICalc.add(18,19));
         
        // Lets do some more interception - Calculator has a display routine which we'll encrypt
        System.out.println(pICalc.display("I am the calculator"));
        
        IDelegator pICalculatorDel = pIMeta.GetDelegator(pCalcIUnk, "Samples.CalculatorComponent.ICalculator");
        pICalculatorDel.addPreMethod(Interceptors, "Pre1");
        
        // Encrypted version
        System.out.println("Encrypted: " +pICalc.display("I am the calculator"));
        System.out.println();
        
        // Now decrypt using Post Interception
        pICalculatorDel.addPostMethod(Interceptors, "Post1");
        // Decrypted version
        System.out.println("Decrypted: " +pICalc.display("I am the calculator"));
        System.out.println();
        
        // Test the IMetaArchitecture interface
        IMetaArchitecture pIMetaArch = (IMetaArchitecture) runtime.QueryInterface("OpenCOM.IMetaArchitecture");
        Vector<Long> list = new Vector<Long>();
        int NoConns = pIMetaArch.enumConnsToIntf(pAdderIUnk, "Samples.AdderComponent.IAdd", list);
        for(int index=0; index<NoConns; index++){
            OCM_ConnInfo_t TempConnInfo = pIOCM.getConnectionInfo(list.get(index).longValue());
            System.out.println("Component "+ TempConnInfo.sinkComponentName + " is connected to " + 
                TempConnInfo.sourceComponentName + " on interface " + TempConnInfo.interfaceType); 
        }
        System.out.println();
              
        Vector<Long> Recplist = new Vector<Long>();
        int NoConns2 = pIMetaArch.enumConnsFromRecp(pCalcIUnk, "Samples.AdderComponent.IAdd", Recplist);
        for(int index=0; index<NoConns2; index++){
            OCM_ConnInfo_t TempConnInfo = pIOCM.getConnectionInfo(Recplist.get(index).longValue());
            System.out.println("Component "+ TempConnInfo.sourceComponentName + " is connected to " + 
                TempConnInfo.sinkComponentName + " by receptacle of interface " + TempConnInfo.interfaceType); 
        }
        System.out.println();
        
        // Test IMetaInterface
        IMetaInterface pMeta =  (IMetaInterface) pAdderIUnk.QueryInterface("OpenCOM.IMetaInterface");
        Vector<Class> ppIntf = new Vector<Class>();
        int length = pMeta.enumIntfs( ppIntf);
        System.out.println("The number of Interfaces on Adder component is "+length);
        for (int y=0; y<length; y++){
            Class temp =  ppIntf.elementAt(y);
            System.out.println(temp.toString());
        }
        System.out.println();

         
        Vector<OCM_RecpMetaInfo_t> ppRecps = new Vector<OCM_RecpMetaInfo_t>();
        pMeta =  (IMetaInterface) pCalcIUnk.QueryInterface("OpenCOM.IMetaInterface");
        int length2 = pMeta.enumRecps( ppRecps);
        System.out.println("The number of receptacles on Calculator component is "+length2);
        for (int y=0; y<length2; y++){
            OCM_RecpMetaInfo_t temp = ppRecps.elementAt(y);
            System.out.println("Receptacle interface is : "+temp.iid);
            System.out.println("Receptacle type is: "+temp.recpType);
        }     
        System.out.println();
        
        // Test Connection info
        OCM_ConnInfo_t ConnInfo = pIOCM.getConnectionInfo(ConnID1);
        System.out.println("Component "+ ConnInfo.sourceComponentName + " is connected to " 
            + ConnInfo.sinkComponentName + " by receptacle of interface " + ConnInfo.interfaceType);
        
        // Enumerate components
        Vector<IUnknown> ppComps = new Vector<IUnknown>();
        int noComps = pIOCM.enumComponents(ppComps);
        System.out.println("The number of components is : "+noComps);
        
        // Test GetComponentName & GetComponentCLSID by listing the enumeration
        for(int index=0; index<noComps;index++){
            System.out.println("Component Name is: " + pIOCM.getComponentName(ppComps.elementAt(index))+ "Class ID is: " + pIOCM.getComponentCLSID(ppComps.elementAt(index)));  
        }

        // Test component deletion
        
        runtime.deleteInstance(pSubIUnk);
        noComps = pIOCM.enumComponents(ppComps);
        System.out.println("The number of components is : "+noComps);
        pIDebug.dump();
        
        String[] delList= new String[5];
        long numdels = pIAdderDel.viewPreMethods(delList);
        
        for (int i=0; i< numdels; i++)
            System.out.println(delList[i]);
        
        // Remove the del off ICalc - otherwise add will fail
        pICalculatorDel.delPreMethod("Pre1");
        
        pICalculatorDel.delPostMethod("Post1");
        
        // Check add still operates
        System.out.println("The value of 17+45 = "+ pICalc.add(17,45));
    }
    
}
