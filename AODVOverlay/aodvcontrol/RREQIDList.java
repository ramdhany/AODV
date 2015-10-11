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



import java.util.*;
import java.net.*;

import aodvstate.ConfigInfo;



/**
* This class manages the list related to holding RREQID Entries. These RREQ ID
* entries are kept to manage their expiry.
*
* @author : Rajiv Ramdhany
* @date : 12-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
*/
public class RREQIDList {
	public ConfigInfo cfgInfo;
	public ArrayList idList;

	/**
	* Constructor creates the list that would hold each
	* of the RREQ ID info objects
	*/
	public RREQIDList(ConfigInfo cfg) {
		cfgInfo = cfg;
		idList = new ArrayList();
	}

	/**
	* Method to add an entry to the list. This entry is added
	* at the end as it would have the highest expiry time.
	* @param InetAddress adr - originator IP address
	* @param int id - RREQ ID number
	*/
	public synchronized void add(InetAddress adr, int id) {
		RREQIDEntry en = new RREQIDEntry(cfgInfo, adr, id);
		idList.add(idList.size(), en);
	}

	/**
	* Method to remove an entry from the RREQ ID list.
	* @param InetAddress adr - Originator IP address
	* @param int id - RREQ ID num
	* @return RREQIDEntry - returns the removed item or null
	*			 if not found
	*/
	public synchronized RREQIDEntry remove(InetAddress adr, int id) {
		ListIterator li;
		RREQIDEntry en;

		li = idList.listIterator(0);
		while((en = (RREQIDEntry) li.next()) != null) {
 			if(adr.getHostAddress().equals(en.origIPAddr.getHostAddress())
			    && id == en.RREQIDNum) {
				li.remove();
				return en;
			}
		}
		return null;
	}

	/**
	* Method to check if a certain Originator IP / RREQ ID entry
	* exists
	* @param InetAddress adr - Originator IP address
	* @param int id - RREQ ID num
	* @return boolean - returns true if exist, else false
	*/
	public synchronized boolean exist(InetAddress adr, int id) {
		ListIterator li;
		RREQIDEntry en;

		li = idList.listIterator(0);
		try {
			while((en = (RREQIDEntry) li.next()) != null) {
 				if(adr.getHostAddress().equals(en.origIPAddr.getHostAddress())
				    && id == en.RREQIDNum) {
			    		return true;
				}
			}
		}catch(Exception e) {
			return false;
		}
		return false;
	}

	/**
	* Method to get the RREQIDEntry of a given Originator IP / RREQ ID
	* entry
	* @param InetAddress adr - Originator IP address
	* @param int id - RREQ ID num
	* @return RREQIDEntry - returns the extracted object or null, if it
	*                       does not exist
	*/
	public synchronized RREQIDEntry get(InetAddress adr, int id) {
		ListIterator li;
		RREQIDEntry en;

		li = idList.listIterator(0);
		while((en = (RREQIDEntry) li.next()) != null) {
 			if(adr.getHostAddress().equals(en.origIPAddr.getHostAddress())
			    && id == en.RREQIDNum) {
			    	return en;
			}
		}
		return null;
	}

	/**
	* Method to get the first RREQIDEntry in the list. This should
	* always be the entry with the lowest expiry time.
	* @return RREQIDEntry - returns the extracted object or null, if it
	*                       does not exist
	*/
	public synchronized RREQIDEntry getFirst() {
		if(idList.size() > 0)
			return (RREQIDEntry) idList.get(0);
		return null;
	}
}
