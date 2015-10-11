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
* This Class represents a Route Reply Message (RREP) in AODV as
* defined in RFC 3561. It includes the DatagramPacket that represents
* the UDP packet.
*
* @author : Koojana Kuladinithi
* @date : 07-aug-2007
* @email : koo@comnets.uni-bremen.de
*
*/
public class RREP extends AODVMessage {

	// variables of AODV RREP message
	public boolean repairFlag;
	public boolean ackFlag;
	public byte prefixSize;
	public byte hopCount;
	public InetAddress destIPAddr;
	public int destSeqNum;
	public InetAddress origIPAddr;
	public int lifeTime;



	/**
	* Constructor to create a RREP using the contents of a
	* DatagramPacket. This is how a RREP is created for a
	* RREP message received as UDP message. This is listened by
	* the intermediate nodes, when forwarding RREP to the orginating
	* node
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo ccur - the current info object
	* @param jpcap.UDPPacket up - the Datagram from which this RREP
	*				is created
	* @param String iface - the interface on which the packet arrived
	*/
	public RREP(ConfigInfo cfg, IAodvState cur, UDPPacket up, String iface) throws Exception {
		super(cfg, cur, up, iface);

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			breakDgramToIPv4RREP();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			breakDgramToIPv6RREP();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Constructor to create a RREP giving the different values
	* of the fields. This is required when the protocol handler
	* requires to generate a RREP
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo ccur - the current info object
	* @param boolean mf - when sending, multicast or unicast
	* @param InetAddress sendto - the address to which this is sent
	* @param short ttl - TTL value on the message
	* @param boolean rf - repaire flag
	* @param boolean af - acknowledgement flag
	* @param byte ps - prefix size
	* @param byte hc - hop count
	* @param InetAddress da - destination IP address
	* @param int dsn - destination sequence number
	* @param InetAddress oa - originator IP address
	* @param int lt - lifetime
	*
	*/
	public RREP(ConfigInfo cfg, IAodvState cur, boolean mf, InetAddress sendto, short ttl,
		boolean rf, boolean af, byte ps, byte hc, InetAddress da,
		int dsn, InetAddress oa, int lt) throws Exception {

		super(cfg, cur, mf, sendto, ttl);


		repairFlag = rf;
		ackFlag = af;
		prefixSize = ps;
		hopCount = hc;
		destIPAddr = da;
		destSeqNum = dsn;
		origIPAddr = oa;
		lifeTime = lt;

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			buildDgramUsingIPv4RREP();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			buildDgramUsingIPv6RREP();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Method to build the IPv4 RREP using the information in
	* the DatagramPacket.
	* @exception - return in case of errors, when getting
	* information from RREP
	*/

	private void breakDgramToIPv4RREP() throws Exception {

		// extract AODV RREP values

		// get repair flag status
		if((jpcapUDPDgram.data[1] & 0x80) == 0x80)
			repairFlag = true;

		// get acknowledgement RREP flag status
		if((jpcapUDPDgram.data[1] & 0x40) == 0x40)
			ackFlag = true;

		// get Prefix size
		prefixSize = (byte) (jpcapUDPDgram.data[2] & 0x1f);

		// get hop count
		hopCount = jpcapUDPDgram.data[3];

		// get destination IP address
		byte addr1[] = new byte[4];
		for(int i = 0; i < 4; i++)
		 	addr1[i] = jpcapUDPDgram.data[4 + i];
		destIPAddr = InetAddress.getByAddress(addr1);


		// get the destination seq number
		destSeqNum = 0;
		//for(int i = 0; i < 4; i++)
		//	destSeqNum += ((int) jpcapUDPDgram.data[8 + i] << ((3 - i) * 8));
		destSeqNum |= (int) ((jpcapUDPDgram.data[8] << 24) & 0xFF000000);
		destSeqNum |= (int) ((jpcapUDPDgram.data[9] << 16) & 0x00FF0000);
		destSeqNum |= (int) ((jpcapUDPDgram.data[10] << 8) & 0x0000FF00);
		destSeqNum |= (int) (jpcapUDPDgram.data[11] & 0x000000FF);

		// get originator IP address
		byte addr2[] = new byte[4];
		for(int i = 0; i < 4; i++)
			addr2[i] = jpcapUDPDgram.data[12 + i];
		origIPAddr = InetAddress.getByAddress(addr2);


		// get the Lifetime
		lifeTime = 0;
		//for(int i = 0; i < 4; i++)
		//	lifeTime += ((int) jpcapUDPDgram.data[16 + i] << ((3 - i) * 8));
		lifeTime |= (int) ((jpcapUDPDgram.data[16] << 24) & 0xFF000000);
		lifeTime |= (int) ((jpcapUDPDgram.data[17] << 16) & 0x00FF0000);
		lifeTime |= (int) ((jpcapUDPDgram.data[18] << 8) & 0x0000FF00);
		lifeTime |= (int) (jpcapUDPDgram.data[19] & 0x000000FF);

	}

	/**
	* Method to build the IPv6 RREP using the information in
	* the DatagramPacket.
	*
	*/
	private void breakDgramToIPv6RREP() throws Exception {

	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv4 RREP
	* @excepttion - return in case of errors, when building RREP
	*/
	private void buildDgramUsingIPv4RREP() throws Exception {
		byte msg[] = new byte[20];


		msg[0] = AODV_RREP_MSG_CODE;

		// build flags
		msg[1] = 0x00;

		if(repairFlag)
			msg[1] = (byte) (msg[1] | 0x80);

		if(ackFlag)
			msg[1] = (byte) (msg[1] | 0x40);

		// Prefix Size
		msg[2] = prefixSize;

		// hop count
		msg[3] = hopCount;

		// place destination IP address
		byte addr1[] = destIPAddr.getAddress();
		for(int i = 0; i < 4; i++)
			msg[4 + i] = addr1[i];

		// place destination seq num
		//for(int i = 0; i < 4; i++)
		//	msg[8 + i] =  (byte) ((destSeqNum >>> (8 * (3 - i))) & (byte) 0xFF);
		msg[8] = msg[9] = msg[10] = msg[11] = (byte) 0;
		msg[8] |= (byte) ((destSeqNum & 0xFF000000) >>> 24);
		msg[9] |= (byte) ((destSeqNum & 0x00FF0000) >>> 16);
		msg[10] |= (byte) ((destSeqNum & 0x0000FF00) >>> 8);
		msg[11] |= (byte) (destSeqNum & 0x000000FF);

		// place originator IP address
		byte addr2[] = origIPAddr.getAddress();
		for(int i = 0; i < 4; i++)
			msg[12 + i] = addr2[i];

		// place lifetime
		//for(int i = 0; i < 4; i++)
		//	msg[16 + i] =  (byte) ((lifeTime >>> (8 * (3 - i))) & 0xFF);
		msg[16] = msg[17] = msg[18] = msg[19] = (byte) 0;
		msg[16] |= (byte) ((lifeTime & 0xFF000000) >>> 24);
		msg[17] |= (byte) ((lifeTime & 0x00FF0000) >>> 16);
		msg[18] |= (byte) ((lifeTime & 0x0000FF00) >>> 8);
		msg[19] |= (byte) (lifeTime & 0x000000FF);

		javaUDPDgram = new DatagramPacket(msg, msg.length, toIPAddr, AODV_PORT);
	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv6 RREP
	*
	*/
	private void buildDgramUsingIPv6RREP() throws Exception {

	}

	/**
	* Method to return the values in RREP message
	* @return String - RREP Message contents as a string
	*/

	public String toString() {
		String str = super.toString();

		str = "RREP Message " + ", " + str;
		str = str + "ackFlag : " + ackFlag + ", "
			+ "prefixSize : " + prefixSize + ", "
			+ "hopCount : " + hopCount + ", "
			+ "Destination Address : " + destIPAddr.getHostAddress() + ", "
			+ "Dest Seq Num : " + destSeqNum + ", "
			+ "Orginating Address : " + origIPAddr.getHostAddress() + ", "
			+ "Lifetime : " + lifeTime + " ";
		return str;
	}
}
