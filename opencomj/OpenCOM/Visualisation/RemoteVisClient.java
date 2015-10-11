 /*
  * RemoteVisServer.java
  * OpenCOMJ is a flexible component model for reconfigurable reflection developed at Lancaster University.
  * Copyright (C) 2005 Paul Grace
  * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
  * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with this program; if not, 
  * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package OpenCOM.Visualisation;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import java.util.Vector;

import OpenCOM.*;

/*
* A TCP client module to send visualisation data to a remote server for display
*
*
*/

//class to hold info about what's going on inside a framework
class Framework{
    public String frameworkName;
   
    //a list of component names that a framework was intending to create (not necessarily successful)
    // (this can probably be improved...will leak memory with failed creation attempts)
    public Vector<String> createdComponents;
   
    public Framework(String name){
        this.frameworkName = name;
        createdComponents = new Vector<String>();
    }
   
    public boolean isCreatingComponent(String componentName){
        for (int i = 0; i < createdComponents.size(); i++){
            if (createdComponents.elementAt(i).equals(componentName))
                return true;
        }
      
        return false;
    }
}

public class RemoteVisClient{
    private int serverPort = 10004;
    private SocketChannel socketChannel;
    private Vector<Framework> frameworkList;
   
    public RemoteVisClient() throws IOException{
        try{
            connect(new InetSocketAddress(InetAddress.getLocalHost(), serverPort));
            frameworkList = new Vector<Framework>();
        }
        catch(IOException e){
            throw new IOException("No Server");
        }
    }
   
    public RemoteVisClient(String serverAddress, int port) throws IOException{
        serverPort = port;
        try{
            if (serverAddress.equalsIgnoreCase("localhost"))
                connect(new InetSocketAddress(InetAddress.getLocalHost(), serverPort));
            else
                connect(new InetSocketAddress(InetAddress.getByName(serverAddress), serverPort));

            frameworkList = new Vector<Framework>();
        }
        catch(IOException e){
            throw new IOException("No Server");
        }
      }
   
    public void connect(SocketAddress remoteAddress) throws IOException{
        socketChannel = SocketChannel.open(remoteAddress);
    }
   
    public byte[] getAsByteStream(Object o){
        byte[] result = null;

        try{
            // - serialize these into a byte stream
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream ow = new ObjectOutputStream(bout);
            ow.writeObject(o);
            ow.flush();
            result = bout.toByteArray();
            ow.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return result;
    }
   
    private byte[] intToByteArray (final int integer){
        int byteNum = (40 - Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (integer >>> (n * 8));

        return (byteArray);
    }

    public void writeArrayToServer(byte[] msgLength, byte[] byteMsg){
        try{
            ByteBuffer outBB = ByteBuffer.allocate(msgLength.length + byteMsg.length);
            outBB.put(msgLength);
            outBB.put(byteMsg);

            outBB.rewind();
            socketChannel.write(outBB);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public String inFramework(String ComponentName){
        //check if it's in a framework
        for (int i = 0; i < frameworkList.size(); i++){
            if (frameworkList.elementAt(i).isCreatingComponent(ComponentName)){
                return frameworkList.elementAt(i).frameworkName;
            }
        } 
        return null;
    }
    
    //events from the OCJ runtime
    public void componentCreated(IUnknown comp, String name){
        //get the name, interfaces and receptacles of the component
        String componentName = name;

        IMetaInterface meta = (IMetaInterface) comp.QueryInterface("OpenCOM.IMetaInterface");
        Vector<Class> intfs = new Vector<Class>();
        int size = meta.enumIntfs(intfs);

        String[] interfaces = new String[size];

        for(int i=0; i < size; i++)
            interfaces[i] = intfs.get(i).toString();

        Vector<OCM_RecpMetaInfo_t> recps = new Vector<OCM_RecpMetaInfo_t>(); 
        size = meta.enumRecps(recps);

        String[] receptacles = new String[size];

        for(int i=0; i < size; i++)
            receptacles[i] = recps.get(i).iid;

        //framework?
        ICFMetaInterface iFramework = (ICFMetaInterface) comp.QueryInterface("OpenCOM.ICFMetaInterface");

        //create a CC message and serialize it, getting its serialized length
        ComponentCreatedMessage msg = new ComponentCreatedMessage();
        msg.componentName = componentName;
        msg.interfaces = interfaces;
        msg.receptacles = receptacles;

        //see if this component (by name) has been created inside a framework
        msg.inFramework = inFramework(componentName);
    
        if (iFramework == null)
            msg.type = ComponentCreatedMessage.TYPE_COMPONENT;
        else{
            msg.type = ComponentCreatedMessage.TYPE_FRAMEWORK;
            frameworkList.addElement(new Framework(componentName));
        }

        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);

        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
   
    //framework components call this method just before they attempt to create a new component with the OCJ runtime
    // - so, here we just note that an attempt is being made, and if createComponent (above) is called then the creation
    //   was successful and we should notify the RV server that the component is inside this framework
    // - I guess this assumes component names are globally unique within an OpenCOM instance
    public void creatingComponentInFramework(String frameworkName, String componentName){
        for (int i = 0; i < frameworkList.size(); i++){
            if (frameworkList.elementAt(i).frameworkName.equals(frameworkName)){
                frameworkList.elementAt(i).createdComponents.addElement(componentName);
                break;
            }
        }
    }
   
    public void componentDeleted(IUnknown comp, String name){
        //create a message and serialize it, getting its serialized length
        ComponentDeletedMessage msg = new ComponentDeletedMessage();
        msg.componentName = name;

        msg.inFramework = inFramework(name);

        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);

        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
   
   //frameworks can dynamically expose interfaces and receptacles, so allow this
    public void interfaceExposed(String componentName, String interfaceName){
        //create a message and serialize it, getting its serialized length
        InterfaceExposedMessage msg = new InterfaceExposedMessage();
        msg.componentName = componentName;
        msg.interfaceName = interfaceName;

        //check if it's in a framework
        msg.inFramework = inFramework(componentName);

        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);

        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
    
    public void interfaceRemove(String componentName, String interfaceName){
        //create a message and serialize it, getting its serialized length
        InterfaceUnExposedMessage msg = new InterfaceUnExposedMessage();
        msg.componentName = componentName;
        msg.interfaceName = interfaceName;

        //check if it's in a framework
        msg.inFramework = inFramework(componentName);

        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);

        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
   
    public void receptacleExposed(String componentName, String receptacleName){
        
        //create a message and serialize it, getting its serialized length
        ReceptacleExposedMessage msg = new ReceptacleExposedMessage();
        msg.componentName = componentName;
        msg.receptacleName = receptacleName;
      
        //check if it's in a framework
        msg.inFramework = inFramework(componentName);

        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);
      
        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
    
    public void receptacleRemoved(String componentName, String receptacleName){
        //create a message and serialize it, getting its serialized length
        ReceptacleUnExposedMessage msg = new ReceptacleUnExposedMessage();
        msg.componentName = componentName;
        msg.receptacleName = receptacleName;
      
        //check if it's in a framework
        msg.inFramework = inFramework(componentName);

        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);
      
        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
   
    public void connectionMade(String sourceName, String sinkName, String iid){
        //create a message and serialize it, getting its serialized length
        ConnectionMadeMessage msg = new ConnectionMadeMessage();
        msg.sourceComponentName = sourceName;
        msg.sinkComponentName = sinkName;
        msg.iid = iid;
      
        //check if it's in a framework
        msg.inFramework = inFramework(sourceName);
      
        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);
      
        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
   
    public void connectionBroken(String sourceName, String sinkName, String iid){
        //create a message and serialize it, getting its serialized length
        ConnectionBrokenMessage msg = new ConnectionBrokenMessage();
        msg.sourceComponentName = sourceName;
        msg.sinkComponentName = sinkName;
        msg.iid = iid;
      
        //check if it's in a framework
        msg.inFramework = inFramework(sourceName);
      
        byte[] byteMsg = getAsByteStream(msg);
        byte[] msgLength = intToByteArray(byteMsg.length);
      
        //send the length of the message, then the message
        writeArrayToServer(msgLength, byteMsg);
    }
   
}