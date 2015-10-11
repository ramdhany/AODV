/*
 * IOSOperations.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	11-Feb-2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: IOSOperations.java, interfaces.OSOperations 
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

package interfaces.IOSOperations;

import OpenCOM.IUnknown;
import aodvstate.RouteEntry;

/**
 * @author ramdhany
 *
 */
public interface IOSOperations extends IUnknown {
	
	/**
	* Method to implement to initialize the routing
	* environment to perform AODV protocol handling
	* @param int level - The initialization level
	*			0 = full initialization level
	*			1 - 100 = other init levels
	* @return int - returns the success or failure
	*/
	public int initializeRouteEnvironment(int level);

	/**
	* Method to implement to add a route entry in the
	* routing environment.
	* @param RouteEntry rtEntry - the route entry from which to get
	*				information
	* @return int - returns the success or failure
	*/
	public int addRoute(RouteEntry rtEntry);

	/**
	* Method to implement to remove a route entry in the
	* routing environment.
	* @param RouteEntry rtEntry - the route entry from which to get
	*				information
	* @return int - returns the success or failure
	*/
	public int deleteRoute(RouteEntry rtEntry);

	/**
	* Method to implement to put the route environment
	* to the original state before terminating the
	* protocol handler.
	* @return int - returns the success or failure
	*/
	public int finalizeRouteEnvironment();
	

}
