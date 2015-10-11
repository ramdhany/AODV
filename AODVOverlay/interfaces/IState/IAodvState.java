package interfaces.IState;

import OpenCOM.IUnknown;
import aodvstate.DeleteMinder;
import aodvstate.RouteMinder;
import aodvstate.RouteEntry;

import interfaces.IConfigInfo.IConfigInfo;
import interfaces.IGui.IGui;
import interfaces.ILog.ILog;
import interfaces.IOSOperations.IOSOperations;
import interfaces.IPacketSender.IPacketSender;

import java.net.InetAddress;




/**
 * The State component of the AODV protocol
 * Contains the route table and route discovery list for the protocol. Used to 
 * allow the Control and Forward components to be replaced at run time while 
 * keeping the existing state
 * 
 * @author : Rajiv Ramdhany, 
 * @date : 12/02/2006
 * @email : r.ramdhany@comp.lancaster.ac.uk
 * 
 */
/**
 * @author Rajiv Ramdhany
 *
 */
public interface IAodvState extends IUnknown{
	
	// constants
	public static final int GREATER = 1;
	public static final int EQUAL =	0;
	public static final int LESS = (-1);
	
	
	/**
	 * Gets the route entry for the given destination address <code>key</code>
	 * from the routing table.
	 * 
	 * @param key the destination node's IP address
	 * @return RouteEntry containing the next hop IP address
	 * @throws Exception
	 */
	public RouteEntry getRoute(InetAddress key) throws Exception;
	
	/**
	 * Adds a new route or modifies an existing route in the routing table. Changes are 
	 * reflected in the OS kernel routing table.  
	 * 
	 * @param key the destination address whose route will be added or modified.
	 * @param entry the
	 * @throws Exception
	 */
	public void updateRouteTable(InetAddress key, RouteEntry entry) throws Exception;
	
	/**
	 * Checks the lifetime and status of the active route for the destination address <code>
	 * da</code>. If the route lifetime < 0, the route is expired (invalidated) and allowed to
	 * enter a delete period. A <code>DeleteMinder</code> thread takes over as the route's active
	 * minder to proceed to delete the route (and remove it from the kernel route table as well)
	 * 
	 * @param da the destination address for the route being monitored
	 * @param rtMinder	the active thread monitoring the route
	 * @return an integer representing the current lifetime of the route
	 * @throws Exception
	 */
	public int checkActiveRouteLifetime(InetAddress da, RouteMinder rtMinder) throws Exception;
	
	/**
	 * Checks the delete lifetime of a route. The previously invalidated route is kept in the 
	 * routing table for DELETE_PERIOD (see RFC 3561) before being removed from the routing table.
	 * The route is deleted from the OS kernel route table as well.
	 * 
	 * @param da the destination address for the route being deleted
	 * @param delMinder the active thread deleting the route
	 * @return an integer representing the delete lifetime of the route
	 * @throws Exception
	 */
	public int checkDeleteRouteLifetime(InetAddress da, DeleteMinder delMinder) throws Exception;

	/**
	 * Invalidates the routes for all destinations that can no longer be reached because of a
	 * link break to the next hop.
	 *  
	 * @param destRte the next hop that cannot be reached because of the link break
	 * @throws Exception
	 */
	public void invalidateDestinations(RouteEntry destRte) throws Exception;
	
	/**
	 * Removes a route from the framework's routing table as well as the kernel-level routing
	 * table.
	 * 
	 * @param key destination address for the route to be deleted
	 * @return the route entry being removed
	 */
	public RouteEntry removeRoute(InetAddress key);
	
	
	
	/**
	* Method to increment the local Sequence Number
	* @return int - the incremented sequence number
	*/
	public int incrementOwnSeqNum();

	/**
	* Method to increment the local RREQ ID
	* @return int - the incremented RREQ ID
	*/
	public int incrementOwnRREQID(); 


	/**
	* Method to compare Destination Sequence Numbers. Returns
	* 1, 0 or -1 based on whether first number is greater, equal
	* or less than the second number, respectively.
	* @param int firstNum - 1st number to compare
	* @param int secondNum - 2nd number to compare
	* @return int - 1, 0 or -1 based on greater, equal or less
	*/
	public int destSeqCompare(int firstNum, int secondNum);
	
	
	/**
	 * @return Returns the lastRREQID.
	 */
	public int getLastRREQID();

	/**
	 * @param lastRREQID The lastRREQID to set.
	 */
	public void setLastRREQID(int lastRREQID);

	/**
	 * @return Returns the lastSeqNum.
	 */
	public int getLastSeqNum();

	/**
	 * @param lastSeqNum The lastSeqNum to set.
	 */
	public void setLastSeqNum(int lastSeqNum);
	
	
	/**
	 * @return Returns the log.
	 */
	public ILog getLog();
	
	
	/**
	 * @return Returns a reference to the component connected to the m_PSR_IConfigInfo
	 * receptacle
	 */
	public IConfigInfo getConnectedConfigInfo();

	/**
	 * @return Returns a reference to the component connected to the m_PSR_IOSOperations
	 * receptacle.
	 */
	public IOSOperations getConnectedOSOperations();
	
	
	/**
	 * @return Returns a reference to the component connected to the m_PSR_IGui
	 * receptacle
	 */
	public IGui getConnectedGUI();
	

	/**
	 * @return Returns a reference to the component connected to the m_PSR_IPktSender
	 * receptacle.
	 */
	public IPacketSender getConnectedPktSender();
	
	/**
	 * removes all route entries from the AODV route table. The corresponding
	 * route entries in the kernel route table are removed as well.
	 */
	public void clearRoutingTable();
	
	/**
	 * @return returns an array of route entries found in the AODV route table
	 */
	public RouteEntry[] getRouteArray() throws Exception;
	
	
	/**
	 * @return returns number of route entries found in the AODV route table
	 */
	public int getRouteCount();

	/**
	* Method to check whether there exist atleast one
	* unexpired routes.
	* @return boolean - true if atleast one unexpired
	*			route else returns false
	*/
	public boolean doUnexpiredRoutesExist();
	
	/**
	 * Method to initialise component member variables after component has been
	 * connected to the ConfigInfo component containing protocol parameters  
	 * 
	 */
	public void start();
	
	
	/**
	 * Method to stop the implementing component and its associated threads
	 * 
	 */
	public void stop() throws Exception;
	
	
	
	
}	


