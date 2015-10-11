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
* This Class represents a HELLO message
*
* @author : Rajiv Ramdhany
* @date : 28-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public class HELLO extends RREP {

	public HELLO(ConfigInfo cfg, IAodvState cur, UDPPacket up, String iface) throws Exception {
		super (cfg, cur, up, iface);
	}

	public HELLO (ConfigInfo cfg, IAodvState cur) throws Exception {
		super(cfg, cur, true, cfg.ipAddressMulticastVal, (short) 1,
				false, false, (byte) 0, (byte) 0, cfg.ipAddressVal,
				cur.getLastSeqNum(), cfg.ipAddressVal,
				(cfg.allowedHelloLossVal * cfg.helloIntervalVal));
	}
}
