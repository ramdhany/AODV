/*
 * IDeliver.java
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

package interfaces.IDeliver;

/**
 * The IDeliver interface is used to deliver overlay network events from component to component.
 * Hence, it acts as an event interface. A component implementing an IDeliver receptacle will produce events
 * of type: GridKit.Core.Packet. To receive these events, a component implements this interface. The Deliver
 * operation is called to produce an event operation.
 * 
 * @author  Paul Grace
 * @version 1.0 requires OpenCOM v1.2.3
 */

public interface IDeliver {
    
    /**
     * Deliver is implemented by components when they wish to receive GridKit overlay network events. 
     * e.g. a message has been sent to the network (or their node), a new node has joined, or a node
     * has left or failed. They receive this information within the following parameters.
     * @param networkIdentifier Identifier of the overlay network that the event originates from. Note,
     * this will always be the top level network, even if the event is from a low-level overlay building
     * a larger overlay network.
     * @param msg The packet of data. This is the GridKit.Core.Packet information containing all the 
     * pertinent data about the role of the message.
     * @param length The length of the message.
     * @param from data about where the information has come from. This can be various types of information
     * IP address, Overlay Identifier, user identifier etc.. it needs to be meaningful to the level of
     * recepient. Therefore, careful implementation must be followed to convert formats if necessary.
     * @return A reply message embedded in a packet of bytes.
     */
    public byte[] Deliver(String networkIdentifier, byte[] msg, int length, Object from);
}
