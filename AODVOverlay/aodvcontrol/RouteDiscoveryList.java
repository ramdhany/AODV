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

import interfaces.IState.IAodvState;

import java.util.*;
import java.net.*;

import aodvstate.ConfigInfo;



/**
* Class privide all the functions related to managing
* the collection that hold route discovery entries
*
* @author : Rajiv Ramdhany
* @date : 15-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public class RouteDiscoveryList {
	public ConfigInfo cfgInfo;
	public IAodvState pStateComp;
	public AODVControl pcrtlCompRef;

	public Map routeDiscList;

	/**
	* Constructor that creates the collection object hold the
	* destination addresses for which route discovery is being
	* done
	*
	* @param ConfigInfo cfg - config info object
	* @param CurrentInfo cur - current info object
	*/
	public RouteDiscoveryList(AODVControl pCrtl) {
		pcrtlCompRef = pCrtl;
		routeDiscList = new HashMap();
	}

	/**
	* Method to updte(add) the route discovery entry.
	*
	* @param InetAddress key - the key of the route discovery entry
	* @param RouteDiscoveryEntry entry - the route discovery entry
	*/
	public synchronized void update(InetAddress key, RouteDiscoveryEntry entry) {
		routeDiscList.put(key, entry);
	}

	/**
	* Method to remove a route discovery entry given the detination
	*
	* @param InetAddress key - the key of the entry to remove
	* @return RouteDiscoveryEntry - removed entry
	*/
	public synchronized RouteDiscoveryEntry remove(InetAddress key) {
		return (RouteDiscoveryEntry) routeDiscList.remove(key);
	}

	/**
	* Method to get a route discovery entry given the destination
	* address.
	*
	* @param InetAddress key - the key to search for
	* @return RouteDiscoveryEntry - the found entry or null
	*/
	public synchronized RouteDiscoveryEntry get(InetAddress key) {
		return (RouteDiscoveryEntry) routeDiscList.get(key);
	}
}
