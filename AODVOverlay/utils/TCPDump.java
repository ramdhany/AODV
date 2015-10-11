package utils;

/*

TCPDump ver 0.2 - Java TCPDump
Copyright 2007 ComNets, University of Bremen

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
import jpcap.*;
import jpcap.NetworkInterface;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

import java.util.*;
import java.net.*;
import java.text.*;

/**
* Class/Program dumps details of packets in a given network
* interface. This uses the Jpcap class library an implements
* the Packet Handler interface
*
* @author : Rajiv Ramdhany
* @date : 10-OCT-2007
* @email : r.ramdhany@lancaster.ac.uk
*
* updated by Rajiv Ramdhany
*/
class TCPDump implements PacketReceiver
{
	SimpleDateFormat timeFormatter;
	long seq;

	/**
	* Constructs object and the time formatter.
	*
	*/
	public TCPDump() {
		timeFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
		seq = 0;
	}

	/**
	* Handler called for each packet
	*
	* @param Packet pkt - each packet received
	*/
	public void receivePacket(Packet pkt){
		System.out.println("Dump Info");
		System.out.print("  Seq=" + (seq++));
		System.out.println("  Time=" + timeFormatter.format(new Date()));

		printPacketDetails(pkt);

		printMACDetails((EthernetPacket) pkt.datalink);

		if(pkt instanceof ARPPacket) {
			printARPDetails((ARPPacket) pkt);

		} else if(pkt instanceof IPPacket) {
			System.out.println("TTL: " + ((IPPacket) pkt).hop_limit);
			if(pkt instanceof UDPPacket) {
				printUDPDetails((UDPPacket) pkt);

			} else if(pkt instanceof TCPPacket) {
				printTCPDetails((TCPPacket) pkt);

			} else if(pkt instanceof ICMPPacket) {
				printICMPDetails((ICMPPacket) pkt);

			} else {
				printUnknownIPPacket((IPPacket) pkt);
			}

		} else {
			printUnknownPacket(pkt);
		}
		System.out.println("");
		System.out.println("");
	}

	/**
	* Displays general packet details
	*
	* Packet pkt - received packet
	*/
	void printPacketDetails(Packet pkt) {
		System.out.println("Packet Info");
		System.out.print("  HeaderLen=" + pkt.header.length + " ");
		System.out.print("  DataLen=" + pkt.data.length + " ");
		System.out.println("");
	}

	/**
	* Displays MAC details of a packet
	*
	* EthernetPacket pkt - received packet
	*/
	void printMACDetails(EthernetPacket ethPkt) {
		System.out.println("MAC Info");
		System.out.print("  Src=" + ethPkt.getSourceAddress());
		System.out.print("  Dst=" + ethPkt.getDestinationAddress() + " ");
		System.out.println("");
	}

	/**
	* Displays ARP details of a packet, assuming the packet is an ARP
	*
	* ARPPacket pkt - received packet
	*/
	void printARPDetails(ARPPacket arpPkt) {
		System.out.println("Protocol Info");
		System.out.print("  Proto=ARP ");
		System.out.print("  Op=" + arpPkt.operation + " ");
		System.out.print("  SndMACAddr=" + getHexVal(arpPkt.sender_hardaddr[0])
						+ ":" + getHexVal(arpPkt.sender_hardaddr[1])
						+ ":" + getHexVal(arpPkt.sender_hardaddr[2])
						+ ":" + getHexVal(arpPkt.sender_hardaddr[3])
						+ ":" + getHexVal(arpPkt.sender_hardaddr[4])
						+ ":" + getHexVal(arpPkt.sender_hardaddr[5])
						+ " ");

		System.out.print("  TgtMACAddr=" + getHexVal(arpPkt.target_hardaddr[0])
						+ ":" + getHexVal(arpPkt.target_hardaddr[1])
						+ ":" + getHexVal(arpPkt.target_hardaddr[2])
						+ ":" + getHexVal(arpPkt.target_hardaddr[3])
						+ ":" + getHexVal(arpPkt.target_hardaddr[4])
						+ ":" + getHexVal(arpPkt.target_hardaddr[5])
						+ " ");

		try {
			System.out.print("  SndIPAddr="
				+ InetAddress.getByAddress(arpPkt.sender_protoaddr).getHostAddress()
				+ " ");
		} catch(Exception e) {
			System.out.print("  SndIPAddr=unknown ");
		}

		try {
			System.out.print("  TgtIPAddr="
				+ InetAddress.getByAddress(arpPkt.target_protoaddr).getHostAddress()
				+ " ");
		} catch(Exception e) {
			System.out.print("  TgtIPAddr=unknown ");
		}
		System.out.println("");
	}

	/**
	* Displays UDP details of a packet, assuming the packet is an UDP
	*
	* UDPPacket pkt - received packet
	*/
	void printUDPDetails(UDPPacket pkt) {
		System.out.println("Protocol Info");
		System.out.print("  Proto=UDP ");
		System.out.print("  SrcIPAddr=" + pkt.src_ip.getHostAddress() + " ");
		System.out.print("  DstIPAddr=" + pkt.dst_ip.getHostAddress() + " ");
		System.out.print("  SrcPort=" + pkt.src_port + " ");
		System.out.print("  DstPort=" + pkt.dst_port + " ");
		System.out.print("  DataLen=" + pkt.length + " ");
		System.out.println("");
	}

	/**
	* Displays TCP details of a packet, assuming the packet is an TCP
	*
	* TCPPacket pkt - received packet
	*/
	void printTCPDetails(TCPPacket pkt) {
		System.out.println("Protocol Info");
		System.out.print("  Proto=TCP ");
		System.out.print("  SrcIPAddr=" + pkt.src_ip.getHostAddress() + " ");
		System.out.print("  DstIPAddr=" + pkt.dst_ip.getHostAddress() + " ");
		System.out.print("  SrcPort=" + pkt.src_port + " ");
		System.out.print("  DstPort=" + pkt.dst_port + " ");
		System.out.println("");
	}

	/**
	* Displays ICMP details of a packet, assuming the packet is an ICMP
	*
	* ICMPPacket pkt - received packet
	*/
	void printICMPDetails(ICMPPacket pkt) {
		System.out.println("Protocol Info");
		System.out.print("  Proto=ICMP ");
		System.out.print("  SrcIPAddr=" + pkt.src_ip.getHostAddress() + " ");
		System.out.print("  DstIPAddr=" + pkt.dst_ip.getHostAddress() + " ");
		System.out.print("  Type=" + pkt.type + " ");
		System.out.println("");
	}

	/**
	* Displays unknown IP packet
	*
	* IPPacket pkt - received packet
	*/
	void printUnknownIPPacket(IPPacket pkt) {
		System.out.println("Protocol Info");
		System.out.println("  Proto=IP/Unknown ");
	}

	/**
	* Displays unknown packet
	*
	* Packet pkt - received packet
	*/
	void printUnknownPacket(Packet pkt) {
		System.out.println("Protocol Info");
		System.out.println("  Proto=Unknown ");
	}

	/**
     	* Method to convert the contents of a byte to a string
	* that gives the hex value of the contents
	*
	* @param  byte b - byte to convert
	* @return String - the string representing the hex value
	*/
    	String getHexVal(byte b) {
		char charVal[] = new char[2];
		byte comp;

		comp = (byte) ((b >>> 4) & 0x0F);
		if(comp >= 0 && comp <= 9)
			charVal[0] = (char) ('0' + comp);
		else
			charVal[0] = (char) ('a' + (comp - 10));

		comp = (byte) (b & 0x0F);
		if(comp >= 0 && comp <= 9)
			charVal[1] = (char) ('0' + comp);
		else
			charVal[1] = (char) ('a' + (comp - 10));

		return (new String(charVal));
	}
    	

	/**
	* Main method that invokes the packet capture interface
	* for the given interface
	*
	* @param String[] args - command line parameters
	*/
	public static void main(String[] args) {

//		if(args.length != 1) {
//			System.out.println("");
//			System.out.println("TCPDump - Dump the packets that are sent and");
//			System.out.println("received by the given network interface");
//			System.out.println("");
//			System.out.println("Usage : TCPDump <device> ");
//			System.out.println("        The <device> is the ID of the network ");
//			System.out.println("        interface. In Linux, it is something ");
//			System.out.println("        like eth1, eth2, etc., and for Windows");
//			System.out.println("        use ListIfc.class to find the device name");
//			System.exit(1);
//		}

		try {
			NetworkInterface[] devList=JpcapCaptor.getDeviceList();
			System.out.println("Start capturing on " + devList[1].name);
			JpcapCaptor jpcap=JpcapCaptor.openDevice(devList[1], 1000, false, 20);
			jpcap.loopPacket(-1, new TCPDump());

		} catch(Exception e) {
			System.out.println("Some error : " + e);
		}
	}


}
