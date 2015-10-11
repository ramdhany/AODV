/*
 * IConfigure.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	7 Mar 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: IConfigure.java, configurator 
 *
 * GridKit is a configurable and dynamically reconfigurable middleware for Grid and pervasive computing.
 * It is the intention that all individual elements (components and frameworks) are resuable within other
 * networking and middleware software. However, please retain this original license for individual component
 * re-use.
 *
 * Copyright (C) 2005 Paul Grace
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package interfaces.IConfigure;

import OpenCOM.ICFMetaInterface;
import OpenCOM.IUnknown;

/**
 * @author ramdhany
 *
 */
public interface IConfigure extends IUnknown {

	
	/**
     * Configure takes an XML configuration and generates a new Component Framework.
     * @param XMLFileName The filename of the XML document describing the new framework.
     * @return A reference to the newly created component framework instance.
     */
     public boolean Configure(String XMLFileName, String cfgInfoFile);
     
     /**
     * ReConfigure takes an XML configuration and applies it to the existing framework instance.
     * @param pMeta Reference to the meta architecture interface of the component framework to reconfigure.
     * @param XMLFileName The filename of the XML document describing the new framework.
     * @return An indication of whether the reconfiguration completed successfully.
     */
     public boolean ReConfigure(ICFMetaInterface pMeta, String XMLFileName);
	
	     
}
