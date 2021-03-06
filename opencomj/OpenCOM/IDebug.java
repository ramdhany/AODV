/*
 * IDebug.java
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

package OpenCOM;
import OpenCOM.Visualisation.RemoteVisClient;
import OpenCOM.IUnknown;
/**
 * Description: Debug interface of the runtime. Supports one operation
 * to visualise the current graph.
 * @author  Paul Grace
 * @version 1.3.2
 */

public interface IDebug extends IUnknown{
    /*
     * Method : Dump
     * Description: Dump runtime graph contents to console output
     */
    void dump();
    
    /*
     * Method: Visualise 
     * Description: Output a graphical representation of the OpenCOM runtime
     */
    void visualise();
    
    /**
     * Get the remote visualisation reference for this runtime. From this you can then
     * make calls that will be visualised on the remote screen.
     * @return The remote reference class
     *
     * @see OpenCOM.Visualisation.RemoteVisClient
     */
    RemoteVisClient getRVClient();
}
