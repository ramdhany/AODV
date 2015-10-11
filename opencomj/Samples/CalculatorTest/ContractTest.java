/*
 * ContractTest.java
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
import OpenCOM.*;
import Samples.AdderComponent.*;
import Samples.SubtractComponent.*;
import Samples.CalculatorComponent.*;
import Samples.Interceptors.*;
import java.lang.reflect.*;
import java.util.*;
/**
 * Demonstrates how to program and check interface contracts.
 * @author  Paul Grace
 * @version 1.3.2
 */
public class ContractTest {
    
    /** Creates a new instance of TestProgram */
    public ContractTest() {
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
        
        IMetaInterface pMeta3 =  (IMetaInterface) pCalcIUnk.QueryInterface("OpenCOM.IMetaInterface");
        
        // Test the insertion of meta data // Add values to IADD
        pMeta3.SetAttributeValue("Samples.AdderComponent.IAdd", "Receptacle", "Variation", "int",  new Integer(0));
        
        // However, the addition is wrong by 8, so lets use interception to correct it
        IMetaInterception pIMeta = (IMetaInterception) runtime.QueryInterface("OpenCOM.IMetaInterception");
        IDelegator pIAdderDel = pIMeta.GetDelegator(pAdderIUnk, "Samples.AdderComponent.IAdd");
        
        // Add the new pre-method
        PreAndPostMethods Interceptors = new PreAndPostMethods(pIOCM);
        pIAdderDel.addPreMethod(Interceptors, "checkRules");
        // Lets test the Add component again
        try{
            int b = pICalc.add(18,19);
            System.out.println("The \"Intercepted value\" of 18+19 = "+ b);
        }
        catch(Exception e){
            System.out.println("Contract breached");
        }   
         
        pIAdderDel.delPreMethod("checkRules");
        pIAdderDel.addPreMethod(Interceptors, "Pre0");
        pIAdderDel.addPreMethod(Interceptors, "checkRules");
        try{
            int b = pICalc.add(18,19);
            System.out.println("The \"Intercepted value\" of 18+19 = "+ b);
        }
        catch(Exception e){
            System.out.println("Contract breached");
        }   
        
    }
    
}
