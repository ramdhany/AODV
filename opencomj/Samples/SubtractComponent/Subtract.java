/*
 * Subtract.java
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

package Samples.SubtractComponent;
import OpenCOM.*;
import java.util.*;
/**
 * Component implementing a single subtract operation.
 * @author  Paul Grace
 * @version 1.3.2
 */
public class Subtract extends OpenCOMComponent implements IUnknown, ISubtract, IMetaInterface, ILifeCycle {
    
    /** Creates a new instance of Subtract */
    public Subtract(IUnknown pRuntime) {
        super(pRuntime);
    }
    
    // Interface ISubtract
    /**
     * Subtract operand y from x.
     * @param a Operand X.
     * @param b Operand Y.
     * @return The result of the subtraction.
     */
    public int subtract(int a, int b) {
        return a-b;
    }
     
    // ILifeCycle Interface
    public boolean startup(Object pIOCM) {
        return true;
    }
    
    public boolean shutdown() {
        return true;
    }
    
    
}


