/*

AODV Overlay v0.5.3 Copyright 2007-2010  Lancaster University

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

package aodvcontrol;

import java.net.*;
import java.util.*;

import aodvstate.ConfigInfo;


/**
* Objects of this class holds information ralated to a
* single RREQ ID. These objects are used to manage the
* expiry of these RREQ IDs.
*
* @author : Rajiv Ramdhany
* @date : 11-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
*/
public class RREQIDEntry {
	public ConfigInfo cfgInfo;
	

	// RREQ ID info
	public InetAddress origIPAddr;
	public int RREQIDNum;
	public long expiryTime;

	/**
	* Constructor to create a RREQ ID entry to be placed in the list.
	* @param ConfigInfo cfg - config object
	* @param InetAddress adr - Originator IP address of RREQ
	* @param int id - RREQ ID number in the RREQ
	*/
	public RREQIDEntry(ConfigInfo cfg, InetAddress adr, int id) {
		cfgInfo = cfg;
		

		origIPAddr = adr;
		RREQIDNum = id;
		try {
			expiryTime = new Date().getTime() + cfgInfo.pathDiscoveryTimeVal;

		} catch(Exception e) {
			expiryTime = new Date().getTime();
		}
	}
}
