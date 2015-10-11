

package aodvstate;

import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;
import interfaces.IState.IAodvState;

import java.util.*;
import java.net.*;




/**
* Class provide all the functions related to managing
* the collection that hold route information
*
* @author : Rajiv Ramdhany, 
* @date : 11-aug-2007
* @email : r.ramdhany@lancaster.ac.uk
* 
* Modified by Rajiv Ramdhany
* Date: 14/02/2007
*
*/
public class RouteList {
	public ConfigInfo cfgInfo;
	public IAodvState pStateComp;
	

	private IOSOperations osOps;
	private Map<InetAddress, RouteEntry> routeList;
	private int currUnexpiredRouteCount;

	/**
	* Constructor that creates the map object to hold data
	*
	* @param ConfigInfo cfg - config info object
	* @param CurrentInfo cfg - current info object
	*/
	public RouteList(IAodvState cur) {
		cfgInfo = (ConfigInfo) cur.getConnectedConfigInfo();
		pStateComp = cur;
		osOps = cur.getConnectedOSOperations();
		routeList = new HashMap<InetAddress, RouteEntry>();
		currUnexpiredRouteCount = 0;
	}

	/**
	* Method to update a route entry object, given the key and the
	* object. If object is present, the existing is removed and new
	* object is inserted. Else inserted. Before inserting, a new
	* object is cloned from the given object
	*
	* @param InetAddress key - the key to search, i.e. IP address
	* @param RouteEntry entry - the entry to update
	*/
	public synchronized void update(InetAddress key, RouteEntry entry) throws Exception {
		RouteEntry oldEntry, clonedEntry;
		
		osOps = pStateComp.getConnectedOSOperations();
		// remove the entry ( if there is an entry already, adjust
		//			the unexpired route count )
		oldEntry = (RouteEntry) routeList.get(key);
		if(oldEntry != null && oldEntry.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {
			currUnexpiredRouteCount--;
		}


		// update kernel routing table

		// if kernel route is SET in old entry and also should SET for new entry
		if(oldEntry != null && oldEntry.kernelRouteSet
		    && entry.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

			entry.kernelRouteSet = true;

		// if kernel route is NOT SET in old entry but should SET for new entry
		} else if((oldEntry == null || !oldEntry.kernelRouteSet)
		          && entry.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {

		    	// set kernel route
			entry.kernelRouteSet = true;
			osOps.addRoute(entry);

		// if kernel route is SET in old entry but should NOT SET in new entry
		} else if(oldEntry != null && oldEntry.kernelRouteSet
		           && (entry.routeStatusFlag != RouteEntry.ROUTE_STATUS_FLAG_VALID)) {

			// remove the kernel route entry
			entry.kernelRouteSet = false;
			osOps.deleteRoute(entry);

		// if kernel route is NOT SET in old entry and also should NOT SET for new entry
		} else {

			entry.kernelRouteSet = false;
		}

		// clone the entry and add to list and adjust the unexpired route
		// count
		clonedEntry = entry.getCopy();
		routeList.put(key, clonedEntry);
		if(clonedEntry.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {
			currUnexpiredRouteCount++;
		}
		
		// update display
		pStateComp.getConnectedGUI().updateDisplay();
		
		// log
		pStateComp.getLog().write(ILog.ACTIVITY_LOGGING,
				"Route List - Route updated, " +  entry.toString());
	}

	/**
	* Method to remove a route entry given the key. The method
	* removes the object and returns it;
	*
	* @param InetAddress key - the key to use to retrieve and delete
	* @return RouteEntry - the deleted object
	*/
	public synchronized RouteEntry remove(InetAddress key) {
		RouteEntry oldEntry;
		
		osOps = pStateComp.getConnectedOSOperations();
		// if there is an entry, adjust the unexpired route count
		oldEntry = (RouteEntry) routeList.get(key);
		if(oldEntry != null && oldEntry.routeStatusFlag == RouteEntry.ROUTE_STATUS_FLAG_VALID) {
			currUnexpiredRouteCount--;
		}

		// update kernel routing table
		// if kernel route is SET in old entry then delete
		if(oldEntry != null && oldEntry.kernelRouteSet) {

			// remove the kernel route entry
			osOps.deleteRoute(oldEntry);
		}

		routeList.remove(key);
		
		// update display
		try {
			pStateComp.getConnectedGUI().updateDisplay();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// log
		pStateComp.getLog().write(ILog.ACTIVITY_LOGGING,
				"Route List - Route removed, " +  oldEntry.toString());

		return oldEntry;
	}

	/**
	* Method to get a route entry object. Always returns a
	* clon of the original route entry object.
	*
	* @param InetAddress key - the key to use to retrieve
	* @return RouteEntry - the retrieved (cloned) object
	*/
	public synchronized RouteEntry get(InetAddress key) throws Exception {
		RouteEntry rte;

		rte = (RouteEntry) routeList.get(key);
		if(rte == null)
			return null;
		else {
			// always return a copy
			return rte.getCopy();
		}
	}

	/**
	* Method to check whether there exist atleast one
	* unexpired routes.
	* @return boolean - true if atleast one unexpired
	*			route else returns false
	*/
	public synchronized boolean doUnexpiredRoutesExist() {
		if(currUnexpiredRouteCount > 0)
			return true;
		return false;
	}
	
	

	public synchronized int getRouteCount() {
		return routeList.size();
	}

	public synchronized RouteEntry[] getRouteArray() throws Exception {
		Object array[];
		RouteEntry rtArray[];
		int i;

		array = (routeList.values()).toArray();
		if(array.length == 0) {
			return null;
		}

		rtArray = new RouteEntry[array.length];

		for(i = 0; i < array.length; i++) {
			rtArray[i] = ((RouteEntry) array[i]).getCopy();
		}

		return rtArray;
	}

	/**
	 * 
	 * stop clean up kernel route table by removing routing entries discoevred using AODV
	 *
	 */
	public synchronized void stop() {
		Object array[];
		RouteEntry rte;
		int i;

		array = (routeList.values()).toArray();
		for(i = 0; i < array.length; i++) {
			rte = (RouteEntry) array[i];

			// remove kernel oute entry, if exist
			if(rte.kernelRouteSet) {
				osOps.deleteRoute(rte);
			}

			routeList.remove(rte.destIPAddr);
		}
		currUnexpiredRouteCount = 0;
	}
}
