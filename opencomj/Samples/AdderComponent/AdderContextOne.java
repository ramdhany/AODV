/*
 * AdderContextOne.java
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

package Samples.AdderComponent;
import OpenCOM.*;
import java.util.*;
/**
 * Simple component offering methods to add integers and receive messages. In this case we
 * give the component context (an owner name), which is then used as a selection method
 * by the calculator component to choose which component's methods to call.
 *
 * @author  Paul Grace
 * @version 1.3.2
 */
public class AdderContextOne extends OpenCOMComponent implements IUnknown, IAdd, IOutput, IMetaInterface, ILifeCycle {
    
    /**
     * Local context data - which will be attached as meta data to the component interfaces.
     */
    private String Owner;

    /** Creates a new instance of Adder */
    public AdderContextOne(IUnknown pRuntime) {
        super(pRuntime);
        
        // Hand set the context
        Owner = "Paul Grace";
    }
    
    // IAdd interface implementation
    
    /**
     * Add two integers together.
     * @param a Operand X.
     * @param b Operand Y.
     * @return The added values.
     */
    public int add(int a, int b) {
        return a+b;
    }
    
    // IOutput Interface implementation
    
    /**
     * Displays the given message on Java standard output
     * @param message The text to output
     */
    public void DisplayMessage(String message){
        System.out.println("The Owner is: "+Owner);
        System.out.println("The message sent from the calculator is: "+message);
    }

    // ILifeCycle Interface
    public boolean startup(Object pIOCM) {
        // Attach the local context as a metadata attribute of the IOutput interface
        // This can then be discovered using the meta interface MOP
        SetAttributeValue("Samples.AdderComponent.IOutput", "Interface", "Owner", "String", Owner);
        return true;
    }
    
    public boolean shutdown() {
        return true;
    }
}
