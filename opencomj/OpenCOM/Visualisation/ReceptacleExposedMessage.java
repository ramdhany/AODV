/*
* ReceptacleExposedMessage.java
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
package OpenCOM.Visualisation;

/**
 * Event message stating that a receptacle has been exposed
 *
 * @author Barry Porter
 * @version 1.3.3
 */
public class ReceptacleExposedMessage extends VisMessage implements java.io.Serializable{
   
    static final long serialVersionUID = 2734773588862542179L;
    
    /**
     * Component Name
     */
    public String componentName;
    
    /**
     * Receptacle Type
     */
    public String receptacleName;
    
    /**
     * Component the receptacle resides within
     */
    public String inFramework;
   
}