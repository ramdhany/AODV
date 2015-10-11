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
* This Class represents a Route Request Message (RREQ) in AODV as
* defined in RFC 3561. It includes the DatagramPacket that represents
* the UDP packet.
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
* @modified to add toString by Koo
* @date : 11-aug-2007
*
*/
public class RREQ extends AODVMessage {

	// variables of AODV RREQ message
	public boolean joinFlag;
	public boolean repairFlag;
	public boolean gratRREPFlag;
	public boolean destOnlyFlag;
	public boolean unknownSeqNumFlag;
	public byte hopCount;
	public int RREQID;
	public InetAddress destIPAddr;
	public int destSeqNum;
	public InetAddress origIPAddr;
	public int origSeqNum;

	/**
	* Constructor to create a RREQ using the contents of a
	* DatagramPacket. This is how a RREQ is created for a
	* RREQ message received as UDP message. This is mostly
	* used by the listener who listens to incomming AODV
	* messages.
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo cur - the current info object
	* @param jpcap.UDPPacket up - the Datagram from which this RREQ
	*				is created
	*@exception Exception, in case of errors
	*/

	public RREQ(ConfigInfo cfg, IAodvState cur, UDPPacket up, String iface) throws Exception {
		super(cfg, cur, up, iface);

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			breakDgramToIPv4RREQ();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			breakDgramToIPv6RREQ();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Constructor to create a RREQ giving the different values
	* of the fields. This is required when the protocol handler
	* requires to generate a RREQ
	*
	* @param ConfigInfo cfg - the config info object
	* @param CurrentInfo cur - the current info object
	* @param boolean mf - when sending, multicast or unicast
	* @param InetAddress sendto - the address to which this is sent
	* @param short ttl - TTL value on the message
	* @param boolean jf - join flag
	* @param boolean rf - repaire flag
	* @param boolean gf - gratuious RREP flag
	* @param boolean df - destination only flag
	* @param boolean usnf - unknown sequence number flag
	* @param byte hc - hop count
	* @param int ri - RREQ ID
	* @param InetAddress da - destination IP address
	* @param int dsn - destination sequence number
	* @param InetAddress oa - originator IP address
	* @param int osn - originator sequence number
	*
	*/
	public RREQ(ConfigInfo cfg, IAodvState cur, boolean mf, InetAddress sendto, short ttl,
		boolean jf, boolean rf, boolean gf, boolean df, boolean usnf, byte hc,
		int ri, InetAddress da, int dsn, InetAddress oa, int osn) throws Exception {

		super(cfg, cur, mf, sendto, ttl);

		joinFlag = jf;
		repairFlag = rf;
		gratRREPFlag = gf;
		destOnlyFlag = df;
		unknownSeqNumFlag = usnf;
		hopCount = hc;
		RREQID = ri;
		destIPAddr = da;
		destSeqNum = dsn;
		origIPAddr = oa;
		origSeqNum = osn;

		if(cfgInfo.ipVersionVal == ConfigInfo.IPv4_VERSION_VAL)
			buildDgramUsingIPv4RREQ();
		else if(cfgInfo.ipVersionVal == ConfigInfo.IPv6_VERSION_VAL)
			buildDgramUsingIPv6RREQ();
		else
			throw new Exception("Invalid IP version");
	}

	/**
	* Method to build the IPv4 RREQ using the information in
	* the DatagramPacket.
	* @exception - return errors in case of extracting RREQ values
	*   0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |     Type      |J|R|G|D|U|   Reserved          |   Hop Count   |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                            RREQ ID                            |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                    Destination IP Address                     |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                  Destination Sequence Number                  |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                    Originator IP Address                      |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                  Originator Sequence Number                   |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	*/
	private void breakDgramToIPv4RREQ() throws Exception {

		// extract AODV RREQ values

		//get join flag status
		if((jpcapUDPDgram.data[1] & 0x80) == 0x80)
			joinFlag = true;

		// get repair flag status
		if((jpcapUDPDgram.data[1] & 0x40) == 0x40)
			repairFlag = true;

		// get gratuious RREP flag status
		if((jpcapUDPDgram.data[1] & 0x20) == 0x20)
			gratRREPFlag = true;

		// get destination only flag status
		if((jpcapUDPDgram.data[1] & 0x10) == 0x10)
			destOnlyFlag = true;

		// get unknown seq num flag status
		if((jpcapUDPDgram.data[1] & 0x08) == 0x08)
			unknownSeqNumFlag = true;

		// get hop count
		hopCount = jpcapUDPDgram.data[3];

		// get RREQ ID
		RREQID = 0;
		//for(int i = 0; i < 4; i++)
		//	RREQID += (jpcapUDPDgram.data[4 + i] << ((3 - i) * 8));
		RREQID |= (int) ((jpcapUDPDgram.data[4] << 24) & 0xFF000000);
		RREQID |= (int) ((jpcapUDPDgram.data[5] << 16) & 0x00FF0000);
		RREQID |= (int) ((jpcapUDPDgram.data[6] << 8) & 0x0000FF00);
		RREQID |= (int) (jpcapUDPDgram.data[7] & 0x000000FF);

		// get destination IP address
		byte addr1[] = new byte[4];
		for(int i = 0; i < 4; i++)
		 	addr1[i] = jpcapUDPDgram.data[8 + i];
		destIPAddr = InetAddress.getByAddress(addr1);

		// get the destination seq number
		destSeqNum = 0;
		//for(int i = 0; i < 4; i++)
		//	destSeqNum += ((int) jpcapUDPDgram.data[12 + i] << ((3 - i) * 8));
		destSeqNum |= (int) ((jpcapUDPDgram.data[12] << 24) & 0xFF000000);
		destSeqNum |= (int) ((jpcapUDPDgram.data[13] << 16) & 0x00FF0000);
		destSeqNum |= (int) ((jpcapUDPDgram.data[14] << 8) & 0x0000FF00);
		destSeqNum |= (int) (jpcapUDPDgram.data[15] & 0x000000FF);

		// get originator IP address
		byte addr2[] = new byte[4];
		for(int i = 0; i < 4; i++)
			addr2[i] = jpcapUDPDgram.data[16 + i];
		origIPAddr = InetAddress.getByAddress(addr2);


		// get the originator seq number
		origSeqNum = 0;
		//for(int i = 0; i < 4; i++)
		//	origSeqNum += ((int) jpcapUDPDgram.data[20 + i] << ((3 - i) * 8));
		origSeqNum |= (int) ((jpcapUDPDgram.data[20] << 24) & 0xFF000000);
		origSeqNum |= (int) ((jpcapUDPDgram.data[21] << 16) & 0x00FF0000);
		origSeqNum |= (int) ((jpcapUDPDgram.data[22] << 8) & 0x0000FF00);
		origSeqNum |= (int) (jpcapUDPDgram.data[23] & 0x000000FF);

	}

	/**
	* Method to build the IPv4 RREQ using the information in
	* the DatagramPacket.
	*
	*/
	private void breakDgramToIPv6RREQ() throws Exception {

	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv4 RREQ
	* @exception return errors in case of building IPv4 RREQ
	*/
	private void buildDgramUsingIPv4RREQ() throws Exception {
		byte msg[] = new byte[24];


		msg[0] = AODV_RREQ_MSG_CODE;

		// build flags
		msg[1] = 0x00;
		if(joinFlag)
			msg[1] = (byte) (msg[1] | 0x80);

		if(repairFlag)
			msg[1] = (byte) (msg[1] | 0x40);

		if(gratRREPFlag)
			msg[1] = (byte) (msg[1] | 0x20);

		if(destOnlyFlag)
			msg[1] = (byte) (msg[1] | 0x10);

		if(unknownSeqNumFlag)
			msg[1] = (byte) (msg[1] | 0x08);

		// hop count
		msg[3] = hopCount;

		// place RREQ ID
		//for(int i = 0; i < 4; i++)
		//	msg[4 + i] =  (byte) ((RREQID >>> (8 * (3 - i))) & 0xFF);
		msg[4] = msg[5] = msg[6] = msg[7] = (byte) 0;
		msg[4] |= (byte) ((RREQID & 0xFF000000) >>> 24);
		msg[5] |= (byte) ((RREQID & 0x00FF0000) >>> 16);
		msg[6] |= (byte) ((RREQID & 0x0000FF00) >>> 8);
		msg[7] |= (byte) (RREQID & 0x000000FF);

		// place destination IP address
		byte addr1[] = destIPAddr.getAddress();
		for(int i = 0; i < 4; i++)
			msg[8 + i] = addr1[i];

		// place destination seq num
		//for(int i = 0; i < 4; i++)
		//	msg[12 + i] =  (byte) ((destSeqNum >>> (8 * (3 - i))) & (byte) 0xFF);
		msg[12] = msg[13] = msg[14] = msg[15] = (byte) 0;
		msg[12] |= (byte) ((destSeqNum & 0xFF000000) >>> 24);
		msg[13] |= (byte) ((destSeqNum & 0x00FF0000) >>> 16);
		msg[14] |= (byte) ((destSeqNum & 0x0000FF00) >>> 8);
		msg[15] |= (byte) (destSeqNum & 0x000000FF);

		// place originator IP address
		byte addr2[] = origIPAddr.getAddress();
		for(int i = 0; i < 4; i++)
			msg[16 + i] = addr2[i];

		// place originator seq num
		//for(int i = 0; i < 4; i++)
		//	msg[20 + i] =  (byte) ((origSeqNum >>> (8 * (3 - i))) & 0xFF);
		msg[20] = msg[21] = msg[22] = msg[23] = (byte) 0;
		msg[20] |= (byte) ((origSeqNum & 0xFF000000) >>> 24);
		msg[21] |= (byte) ((origSeqNum & 0x00FF0000) >>> 16);
		msg[22] |= (byte) ((origSeqNum & 0x0000FF00) >>> 8);
		msg[23] |= (byte) (origSeqNum & 0x000000FF);

		javaUDPDgram = new DatagramPacket(msg, msg.length, toIPAddr, AODV_PORT);
	}

	/**
	* Method to build the DatagramPacket using the information
	* in the IPv6 RREQ
	*
	*/
	private void buildDgramUsingIPv6RREQ() throws Exception {

	}

	/**
	* Method to return the values in RREQ message
	* @return String - RREQ Message contents as a string
	*/

	public String toString() {
		String str = super.toString();

		str = "RREQ Message " + ", " + str;
		str = str + "repairFlag : " + repairFlag + ", "
			+ "gratRREPFlag : " + gratRREPFlag + ", "
			+ "destOnlyFlag : " + destOnlyFlag + ", "
			+ "unknownSeqNumFlag : " + unknownSeqNumFlag + ", "
			+ "hopCount : " + hopCount + ", "
			+ "RREQID : " + RREQID + ", "
			+ "Destination Address : " + destIPAddr.getHostAddress() + ", "
			+ "Dest Seq Num : " + destSeqNum + ", "
			+ "Orginating Address : " + origIPAddr.getHostAddress() + ", "
			+ "Orginating Seq Num : " + origSeqNum + " ";
		return str;

	}
}
