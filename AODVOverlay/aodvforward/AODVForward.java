/*
 * AODVForward.java
 *  
 * VERSION
 * 		v1.0  
 * DATE
 *    	25 Apr 2007
 * AUTHOR
 * 		ramdhany
 * LOG
 * 		Log: AODVForward.java, aodvforward 
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

package aodvforward;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;


import common.MsgListener.MsgListener;

import interfaces.IDeliver.IDeliver;
import interfaces.IForward.IForward;
import OpenCOM.IConnections;
import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OCM_MultiReceptacle;
import OpenCOM.OpenCOMComponent;

/**
 * Uses UDP
 * @author ramdhany
 *
 */
public class AODVForward extends OpenCOMComponent implements ILifeCycle,
		IMetaInterface, IUnknown, IConnections, IForward {
	
	
	// Local static constants
    public static final int MULTICAST =1;
    public static final int UNICAST =0;
	Vector<MsgListener> EvHandlers;
	private Hashtable<String, SocketType> Sockets;
	public OCM_MultiReceptacle<IDeliver> m_PSR_IDeliver;
	
	
	public AODVForward(IUnknown mpIOCM) {
		super(mpIOCM);
		 EvHandlers = new Vector<MsgListener>();
		 m_PSR_IDeliver = new OCM_MultiReceptacle<IDeliver>(IDeliver.class);
		 Sockets = new Hashtable<String, SocketType>();
	}
	
	
	 /**
     * Local class for storing socket information.
     */
    private class SocketType extends Thread{
        public Endpoint endPoint;
        public DatagramSocket sock;
        public String ID;
        private boolean shutdown;
        
        public SocketType(String ID, Endpoint ep, DatagramSocket socket){
            this.ID=ID;
            endPoint=ep;
            sock = socket;
        }
        
        public void shutdown(){
            shutdown = true;
            notify();
        }
        
        public void run(){
            while(!shutdown){
                try{
                    DatagramPacket packet;
                    byte[] data = new byte[65032];
                    packet = new DatagramPacket(data,data.length);

                    // receive the packets 
                    sock.receive(packet); 
                    System.out.println("TR PORT=="+packet.getPort());
                    byte[] returnData = new byte[packet.getLength()];
                    data = packet.getData();
                    for(int i=0; i<packet.getLength(); i++){
                        returnData[i] = data[i];
                    }
                    Object from = packet.getAddress();
                    NotifyMsgHandlers(ID, data, from);     // for
                }
                catch(Exception e){
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
        
    private class Endpoint{
        public String IP;
        public int port;
        
        public Endpoint(String addr, int portNo){
            IP=addr;
            port=portNo;
        }
        
    }
    

    
    public Endpoint ExtractIPInfo(String URL){
        int index = URL.indexOf("://");
        String Temp="";
        if(index<0){
            Temp=URL;
        }
        else{
             Temp= URL.substring(index+3);
        }
        int index2 = Temp.indexOf(":");
        String IP = Temp.substring(0, index2);
        int port = Integer.parseInt(Temp.substring(index2+1));
        return new Endpoint(IP, port);
    }

    public String getLocalAddress() {
        String address = "";
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    public synchronized DatagramSocket newSocket(String locationAddress, String IP, int port, int type){
        DatagramSocket createdSocket=null;
        try{
            InetAddress address; 
            address = InetAddress.getByName(IP); 
            if(type==MULTICAST){
                MulticastSocket cSocket = new MulticastSocket(port);
                cSocket.joinGroup(address);
                createdSocket = cSocket;
            }
            else{
                createdSocket = new DatagramSocket(port);
                createdSocket.setReuseAddress(true);
            }
            SocketType tp = new SocketType(locationAddress, new Endpoint(IP, port), createdSocket);
            Sockets.put(locationAddress, tp);
            //tp.start();
        }
        catch(Exception e){
           return null;
        }
        return createdSocket;
    }
    
    
    
        
    public synchronized DatagramSocket getSocket(String locationAddress, int type){
        
        SocketType tp = Sockets.get(locationAddress);
        if(tp!=null)
            return tp.sock;
        else{
            Endpoint ep = ExtractIPInfo(locationAddress);
            return newSocket(locationAddress, ep.IP, ep.port, type);
        }   
    }
        
    public synchronized boolean removeSocket(String locationAddress){
        SocketType sp =  Sockets.remove(locationAddress);
        return (sp!=null);
    }
	
	
//  ------------ IForward interface operations --------------
	public void EventReceive(String locationIdentifier, IDeliver evHandler) {
		MsgListener nodeHandler = new MsgListener(locationIdentifier, evHandler);
        EvHandlers.add(nodeHandler);
		
	}

	
    public void NotifyMsgHandlers(String id, byte[] data, Object from){
        System.out.println("Notify="+data[0]);
      for(int i=0; i<EvHandlers.size();i++){
          MsgListener a = EvHandlers.get(i);
          
          try{
        	  a.ElementListener.Deliver(a.ElementIdenetifier, data,data.length,from);
          }
          catch(Exception e){
        	  
          }
      }
        
  }


	public byte[] Receive(String URLEndpoint) {
       try{
        	DatagramSocket pSock = getSocket(URLEndpoint, 0);

        	byte[] data = new byte[65032];
        	DatagramPacket packet = new DatagramPacket(data,data.length);

        	// receive the packets 
        	pSock.receive(packet); 
        	byte[] returnData = new byte[packet.getLength()];
        	data = packet.getData();
        	
        	System.arraycopy(data, 0, returnData, 0, packet.getLength());
        	
        	
        	return returnData;        
        }
        catch(Exception e){
        	e.printStackTrace();
        	return null;
        }
        
        
       
		
	}


	public byte[] Send(String URLEndpoint, byte[] msg, int parameter) {
		DatagramSocket  socket;
        DatagramPacket  packet;
        InetAddress     address;
        byte[] message = new byte[1400];
        
        // Extract the address and the port number
        Endpoint ep = ExtractIPInfo(URLEndpoint);
        //
        // Send  request
        //
        
        try{
            address = InetAddress.getByName(ep.IP);
            
            if (parameter == MULTICAST)
            {
            	socket = new MulticastSocket(ep.port);
            	((MulticastSocket) socket).joinGroup(address);
            }
            else
            {
            	socket = new DatagramSocket();
            }
            
            packet = new DatagramPacket(msg, msg.length, address, ep.port);
            socket.send(packet);
            socket.close();
            }
        catch(Exception e){
            System.out.println("URL = "+URLEndpoint);
            e.printStackTrace();
        }
        return message;
		
	}
	
	
	// --------- ILifeCycle Interface Operations ------------
	/* (non-Javadoc)
	 * @see OpenCOM.ILifeCycle#shutdown()
	 */
	public boolean shutdown() {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see OpenCOM.ILifeCycle#startup(java.lang.Object)
	 */
	public boolean startup(Object data) {
		
		return false;
	}

	
	// ------------ IConnections Interface Operations -------------
	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#connect(OpenCOM.IUnknown, java.lang.String, long)
	 */
	public boolean connect(IUnknown pSinkIntf, String riid, long provConnID) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see OpenCOM.IConnections#disconnect(java.lang.String, long)
	 */
	public boolean disconnect(String riid, long connID) {
		
		return false;
	}

	
	
	

	

}
