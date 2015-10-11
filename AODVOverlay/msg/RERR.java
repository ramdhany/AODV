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
import jpcap.packet.*;



/**
* This Class represents a Route Error Message (RERR) in AODV as
* defined in RFC 3561. It includes the DatagramPacket that represents
* the UDP packet.
*
* @author : Koojana Kuladinithi
* @date : 08-aug-2007
* @email : koo@comnets.uni-bremen.de
*
*/
public class RERR extends AODVMessage {

	// variables of AODV RERR message
	public boolean noDelFlag;
	public byte destCount;
	public InetAddress destIPAddr[];
	public int destSeqNum[];


	/**
	* Constructor to create a RERR using the contents of a
	* DatagramPacket. This is how a RERR is created for a
	* RERR message received as UDP message. This is mostly
	* used by the listener who listens to incomming AODV
	* messages.
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo cur - the current info object
	* @param jpcap.UDPPacket up - the Datagram from which this RERR
	*				is created
	* @param String iface - the interface on whiuch the RERR was received
	*/
	public RERR(ConfigInfo cfg, IAodvState cur, UDPPacket up, String iface) throws Exception {
		super(cfg, cur, up, iface);

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			breakDgramToIPv4RERR();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			breakDgramToIPv6RERR();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Constructor to create a RERR giving the different values
	* of the fields. This is required when the protocol handler
	* requires to generate a RERR
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo cur - the current info object
	* @param boolean mf - when sending, multicast or unicast
	* @param InetAddress sendto - the address to which this is sent
	* @param short ttl - TTL value on the message
	* @param boolean nf - no delete flag
	* @param byte dc - destination count
	* @param InetAddress da[] - destination IP address in an array
	* @param int dsn[] - destination sequence number in an array
	*
	*/

	public RERR(ConfigInfo cfg, IAodvState cur, boolean mf, InetAddress sendto, short ttl,
		boolean nf, byte dc, InetAddress da[], int dsn[]) throws Exception {

		super(cfg, cur, mf, sendto, ttl);


		noDelFlag = nf;
		destCount = dc;

		destIPAddr = da;
		destSeqNum = dsn;

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			buildDgramUsingIPv4RERR();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			buildDgramUsingIPv6RERR();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Method to build the IPv4 RERR using the information in
	* the DatagramPacket.
	* @exception - return in case of errors, when using contents of RERR
	*/
	private void breakDgramToIPv4RERR() throws Exception {

		// extract AODV RERR values

		//get no delete flag status
		if((jpcapUDPDgram.data[1] & 0x80) == 0x80)
			noDelFlag = true;

		// get dest count
		destCount = jpcapUDPDgram.data[3];


		// get destination IP address & destination seq number
		destIPAddr = new InetAddress[destCount];
		destSeqNum = new int[destCount];
		for(int j = 0; j < destCount; j++){

			// get destination IP address
			byte addr1[] = new byte[4];
			for(int i = 0; i < 4; i++)
		 		addr1[i] = jpcapUDPDgram.data[4 + j*8 + i];
			destIPAddr[j] = InetAddress.getByAddress(addr1);

			// destination seq number
			destSeqNum[j] = 0;
			//for(int i = 0; i < 4; i++)
			//	destSeqNum[j] += ((int) jpcapUDPDgram.data[8 + j*8 + i] << ((3 - i) * 8));
			destSeqNum[j] |= (int) ((jpcapUDPDgram.data[8 + j*8] << 24) & 0xFF000000);
			destSeqNum[j] |= (int) ((jpcapUDPDgram.data[8 + j*8 + 1] << 16) & 0x00FF0000);
			destSeqNum[j] |= (int) ((jpcapUDPDgram.data[8 + j*8 + 2] << 8) & 0x0000FF00);
			destSeqNum[j] |= (int) (jpcapUDPDgram.data[8 + j*8 + 3] & 0x000000FF);
		}
	}

	/**
	* Method to build the IPv4 RERR using the information in
	* the DatagramPacket.
	*
	*/
	private void breakDgramToIPv6RERR() throws Exception {

	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv4 RERR
	* @exception - return in case of errors, when building RERR
	*/
	private void buildDgramUsingIPv4RERR() throws Exception {
		byte msg[] = new byte[4 + (destCount*8)];


		msg[0] = AODV_RERR_MSG_CODE;

		// build flags
		msg[1] = 0x00;
		if(noDelFlag)
			msg[1] = (byte) (msg[1] | 0x80);

		// hop count
		msg[3] = destCount;

		// place destination IP address & destination seq. number
		for(int j = 0; j < destCount; j++){
			// place destination IP address
			byte addr1[] = destIPAddr[j].getAddress();
			for(int i = 0; i < 4; i++)
				msg[4 + (8*j) + i] = addr1[i];

			// place destination seq num
			//for(int i = 0; i < 4; i++)
			//	msg[8 + (8*j) + i] =  (byte) ((destSeqNum[j] >>> (8 * (3 - i))) & (byte) 0xFF);
			msg[8 + (8*j)] = msg[8 + (8*j) + 1] = msg[8 + (8*j) + 2] = msg[8 + (8*j) + 3] = (byte) 0;
			msg[8 + (8*j)] |= (byte) ((destSeqNum[j] & 0xFF000000) >>> 24);
			msg[8 + (8*j) + 1] |= (byte) ((destSeqNum[j] & 0x00FF0000) >>> 16);
			msg[8 + (8*j) + 2] |= (byte) ((destSeqNum[j] & 0x0000FF00) >>> 8);
			msg[8 + (8*j) + 3] |= (byte) (destSeqNum[j] & 0x000000FF);
		}

		javaUDPDgram = new DatagramPacket(msg, msg.length, toIPAddr, AODV_PORT);
	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv6 RERR
	*
	*/
	private void buildDgramUsingIPv6RERR() throws Exception {

	}

	/**
	* Method to return the values in RREQ message
	* @return String - RERR Message contents as a string
	*/
	public String toString() {
		String str = super.toString();

		str = "RERR Message " + ", " + str;
		str = str + "noDelFlag : " + noDelFlag + ", "
			+ "destCount : " + destCount + ", ";
		for(int j = 0; j < destCount; j++) {
			str = str + "Destination Address-" + j + " : "  + destIPAddr[j].getHostAddress() + ", ";
			str = str + "Dest Seq Num-" + j + " : " + destSeqNum[j] + ", ";
		}
		return str;
	}
}
