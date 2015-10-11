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

package gui;


/**
* Interface that each GUI should implement to provide services to the callers
*
* @author : Rajiv Ramdhany
* @date : 11-feb-2004
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public interface GUIInterface {

        /**
        * Method to perform when the AODV protocol handler itself requests
        * to stop the AODV protocol handler
        */
        public void stopAppFromRouteManager();

        /**
        * Method to redisplay the route table on the main user
        * interface. This is called every time a change is made
        * to the internal routing information.
        */
        public void redrawTable();

        /**
        * Method to display an error message
	* @param String msg - te message to display
        */
	public void displayError(String msg);
}
