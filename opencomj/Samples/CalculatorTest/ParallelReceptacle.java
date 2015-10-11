/*
 * ParallelReceptacle.java
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
/**
 * This example test program illustrates how parallel receptacles execute. In this case, 11 adders
 * are connected to the calculator. When display is called the 11 components are invoked in parallel.
 *
 * @author  Paul Grace
 * @version 1.3.2
 */
public class ParallelReceptacle {
    
    /** Creates a new instance of ParallelReceptacle */
    public ParallelReceptacle() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create the OpenCOM runtime & Get the IOpenCOM interface reference
        OpenCOM runtime = new OpenCOM();
        IOpenCOM pIOCM =  (IOpenCOM) runtime.QueryInterface("OpenCOM.IOpenCOM");
         
        // Create the Subtract component
        IUnknown pSubIUnk = (IUnknown) pIOCM.createInstance("Samples.SubtractComponent.Subtract", "Subtract");
        ILifeCycle pILife =  (ILifeCycle) pSubIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        // Create the Calculator component
        IUnknown pCalcIUnk = (IUnknown) pIOCM.createInstance("Samples.CalculatorComponent.CalculatorParallel", "Calculator");
        pILife =  (ILifeCycle) pCalcIUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        
        IUnknown pAdder1IUnk = (IUnknown) pIOCM.createInstance("Samples.AdderComponent.Adder", "Adder");
        pILife =  (ILifeCycle) pAdder1IUnk.QueryInterface("OpenCOM.ILifeCycle");
        pILife.startup(pIOCM);
        // Create the Adders
        IUnknown pAdderIUnk;
        for(int i=0; i<10; i++){
            pAdderIUnk = (IUnknown) pIOCM.createInstance("Samples.AdderComponent.Adder", "Adder "+i);
            pILife =  (ILifeCycle) pAdderIUnk.QueryInterface("OpenCOM.ILifeCycle");
            pILife.startup(pIOCM);
            long ConnID3 = runtime.connect(pCalcIUnk, pAdderIUnk, "Samples.AdderComponent.IOutput");
        }
        
        // Get the Calculator Interface
        ICalculator pICalc =  (ICalculator) pCalcIUnk.QueryInterface("Samples.CalculatorComponent.ICalculator");
        long ConnID2 = runtime.connect(pCalcIUnk, pSubIUnk, "Samples.SubtractComponent.ISubtract");
        long ConnID1 = runtime.connect(pCalcIUnk, pAdder1IUnk, "Samples.AdderComponent.IAdd");

        // Lets test the Add and Subtract component
        System.out.println("The value of 18+19 = "+ pICalc.add(18,19));
        System.out.println("The value of 63-16 = "+ pICalc.subtract(63,16));
        
        pICalc.display("MultiRecp -- Parallel Test");
    }
    
}
