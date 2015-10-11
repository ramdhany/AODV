/*
 * IFoward.java
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
package interfaces.IForward;
import interfaces.IDeliver.*;
/**
 * IForward is provided by every overlay implementation as a mechanism to send and receive
 * data to and from a running overlay network.
 * @author  Paul Grace
 * @version 1.0 require OpenCOM v1.2.3
 */
public interface IForward {
    /**
     * Send sends data to the given identifier within the overlay implementation.
     * @param locationIdentifier The string identifier of the recipient. This coould be
     * an individual node ID, a group ID, a multicast ID, a set ID ....
     * @param msg The GridKit.Core.Packet message as byte array.
     * @param parameter An additional parameter to describe requirements of the send. These
     * can be overlay specific or not. Similar to socket parameters. 
     * @return A byte[] potentially filled with exception, completion message, or even a reply message;
     * it is dependent on the operation of the overlay.
     */
      public byte[] Send(String locationIdentifier, byte[] msg, int parameter);
      
      /**
       * Receive is a blocking operation that requests to receive a packet of data from
       * a particular network identifier (group, person, node ...). Once the packet is
       * received execution will continue.
       * @param locationIdentifier The identifier of the element you wish to receive a packet from.
       * @return The received message as a byte[].
       */
      public byte[] Receive(String locationIdentifier);
      
      
      /**
       * Event receive provides an asynchronous programming model to receive one or
       * more events from a particular network identifier. The interface that is passed
       * is upcalled every time an event for that identifier is received
       * @param locationIdentifier The identifier of the element you wish to receive a packets from.
       * @param evHandler The service interface reference to be upcalled upon matched event
       * @see GridKit.Interfaces.IReceiveEvent
       */
      public void EventReceive(String locationIdentifier, IDeliver evHandler);
}
