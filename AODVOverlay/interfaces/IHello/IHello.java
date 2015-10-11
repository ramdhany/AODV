/*
 * IHello.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	13-Mar-2007
 * AUTHOR
 * 		Rajiv Ramdhany
 * LOG
 * 		Log: IHello.java, interfaces.IHello 
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

package interfaces.IHello;


import java.net.InetAddress;

import msg.HELLO;
import OpenCOM.IUnknown;
import aodvstate.HelloReceiptMinder;

/**
 * @author Rajiv Ramdhany
 *
 */
public interface IHello extends IUnknown {
	
	/**
	* Method called by the HelloReceiptMinder thread to check whether a route is
	* expired due to not receiving hello messages.
	*
	* @param InetAddress da - the destination for which this minder is active
	* @param HelloReceiptMinder hrMinder - the minder that calls this method
	* @exception Exception - thrown in case of errors
	* @return int - returns the lifetime
	*/
	public int checkHelloReceived(InetAddress da, HelloReceiptMinder hrMinder)
								throws Exception;
	
	/**
	* Method called by the hello minder to send a hello message. Sends
	* it only if there are unexpired routes.
	*
	* @param HELLO hm - the hello message to send
	*/
	public boolean sendHello(HELLO hm) throws Exception;
	
	public void startHello();
	
	
	

}
