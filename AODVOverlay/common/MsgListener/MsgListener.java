/**
 * MsgListener.java
 *
 * GridKit is a configurable and dynamically reconfigurable middleware for Grid and pervasive computing.
 * It is the intention that all individual elements (components and frameworks) are resuable within other
 * networking and middleware software. However, please retain this original license for individual component
 * re-use.
 *
 * Copyright (C) 2005 Paul Grace
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package common.MsgListener;
import interfaces.IDeliver.*;

/**
 * The MsgListener is a simple data structure to store information about
 * event listeners. What the listener is interested in (element ID) and their
 * callback interface.
 *
 * Notably, this provides a separate programming style for callbacks within
 * the GridKit middleware. IDeliver is an alternative, but you can retrieve
 * fine-grained messages directly from the forwarding components without having
 * to implement a complete component.
 *
 * @author  Paul Grace
 * @version Gridkit 1.1
 */
public class MsgListener {
    
    /** Element listening to - could be an event service, endpoint, group, resource, etc. */
    public String ElementIdenetifier;
    /** The listener that will be signalled. */
    public IDeliver ElementListener;
    
    /** Creates a new instance of MsgListener */
    public MsgListener(String elemID, IDeliver elListener) {
        ElementIdenetifier = elemID;
        ElementListener = elListener;
    }
    
}
