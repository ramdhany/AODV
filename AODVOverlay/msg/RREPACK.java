/*

AODV Overlay v0.5.3 Copyright 2007-2010  Lancaster University
Rajiv Ramdhany

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package msg;

import interfaces.IState.IAodvState;

import java.net.*;

import aodvstate.*;
import jpcap.*;
import jpcap.packet.UDPPacket;



/**
* This Class represents a Route Reply Acknowledgement Message (RREPACK) in AODV as
* defined in RFC 3561. It includes the DatagramPacket that represents
* the UDP packet.
*
* @author : Koojana Kuladinithi
* @date : 08-aug-2007
* @email : koo@comnets.uni-bremen.de
*
*/
public class RREPACK extends AODVMessage {

	// variables of AODV RREPACK message


	/**
	* Constructor to create a RREPACK using the contents of a
	* DatagramPacket. This is how a RREPACK is created as a
	* response to RREP message with the A bit set.
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo cur - the current info object
	* @param jpcap.UDPPacket up - the Datagram from which this RREPACK
	* is created
	* @param String iface - the interface on whiuch the RREPACK was received
	*/
	public RREPACK(ConfigInfo cfg, IAodvState cur, UDPPacket up, String iface) throws Exception {
		super(cfg, cur, up, iface);
	}

	/**
	* Constructor to create a RREPACK. This is required when the protocol handler
	* requires to generate a RREPACK
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo cur - the current info object
	* @param boolean mf - when sending, multicast or unicast
	* @param InetAddress sendto - the address to which this is sent
	* @param short ttl - TTL value on the message
	*
	*/
	public RREPACK(ConfigInfo cfg, IAodvState cur, boolean mf, InetAddress sendto, short ttl)
					 throws Exception {

		super(cfg, cur, mf, sendto, ttl);

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			buildDgramUsingIPv4RREPACK();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			buildDgramUsingIPv6RREPACK();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv4 RREPACK
	*
	*/
	private void buildDgramUsingIPv4RREPACK() throws Exception {
		byte msg[] = new byte[2];

		msg[0] = AODV_RREPACK_MSG_CODE;

		javaUDPDgram = new DatagramPacket(msg, msg.length, toIPAddr, AODV_PORT);
	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv6 RREPACK
	* @exception Exception - if error, exception thrown
	*/
	private void buildDgramUsingIPv6RREPACK() throws Exception {

	}

	/**
	* Method to return the values in RREPACK message
	* @return String - RREPACK Message contents as a string
	*/

	public String toString() {
		String str = super.toString();

		str = "RREP-ACK Message " + ", " + str;
		return str;
	}
}
