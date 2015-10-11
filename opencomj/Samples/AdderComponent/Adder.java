/*
 * Adder.java
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

// OpenCOM and Java
import OpenCOM.*;
import java.util.*;

/**
 * Simple component offering methods to add integers and receive messages.
 * @author  Paul Grace
 * @version 1.3.2
 */
public class Adder extends OpenCOMComponent implements IUnknown, IAdd, IOutput, IMetaInterface, ILifeCycle {
    
    /** Creates a new instance of Adder */
    public Adder(IUnknown pRuntime) {
        super(pRuntime);
    }
    
    // IAdd interface implementation
    
    /**
     * Add two integers together.
     * @param a Operand X.
     * @param b Operand Y.
     * @return The added values.
     */
    public int add(int a, int b) {
        return a+b+8;
    }
    
    // IOutput Interface implementation
    
    /**
     * Displays the given message on Java standard output
     * @param message The text to output
     */
    public void DisplayMessage(String message){
        System.out.println("The message sent from the calculator is: "+message);
    }

    // ILifeCycle Interface
    public boolean startup(Object pIOCM) {
        return true;
    }
    
    public boolean shutdown() {
        return true;
    }
    
}
