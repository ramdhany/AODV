/*
 * IPacketSender.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	21 Feb 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: IPacketSender.java, interfaces.IPacketSender 
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

package interfaces.IPacketSender;

import OpenCOM.IUnknown;
import msg.AODVMessage;
import msg.IPPkt;


/**
 * @author ramdhany
 *
 */
public interface IPacketSender extends IUnknown{
	/**
	* Method to send a AODV message through a multicast socket.
	* @param AODVMessage msg - AODV message to send
	* @exception Exception - thrown if error
	*/
	public void sendMessage(AODVMessage msg) throws Exception;
	
	
	/**
	* Method to send a IP packet out through jpcap.
	* @param IPPkt pkt - the IP packet to be sent
	* @exception Exception - thrown if errors encountered
	*/
	public void sendPkt(IPPkt pkt) throws Exception;
	
	
	public void start() throws Exception;
	

}
