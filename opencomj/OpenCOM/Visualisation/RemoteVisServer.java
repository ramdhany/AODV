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

// Import Java Class Libraries
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.Set;

/**
 * A TCP server that receives connections from multiple OpenCOM runtime
 * kernels. OpenCOM events (component create, destroy, connect etc..)
 * are sent along these connections. The server receives the events
 * and converts them to visual display information.
 * 
 * @author Barry Porter
 * @version 1.3.3
 */

/**
 * class to hold a reference to a visualization window (1-1 with an OpenCOM runtime kernel 
 * instance, and its corresponding client address.
 */
class VisWindow{
    /** The visualiation window */
    VisualGraph visWindow;
   
    /** The address of the client sending events to this window */
    SocketAddress clientAddress;
   
    public int componentCount;
   
    public VisWindow(VisualGraph graphWindoww, SocketAddress clientSocket){
      visWindow = graphWindoww;
      clientAddress = clientSocket;
      componentCount = 0;
    }
}

/**
 * Server Class
 */
public class RemoteVisServer implements Runnable{
    
    private static int serverPort = 10004;
    private ServerSocket masterSocket;
    private ServerSocketChannel masterChannel;
   
    private Vector<VisWindow> activeWindows;
   
    public RemoteVisServer(){
        //create a selector to handle multiple concurrent connections
        try{
            masterChannel = ServerSocketChannel.open();
            masterSocket = masterChannel.socket();
            masterSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), serverPort));
            masterChannel.configureBlocking(false);
        }
        catch(IOException e){
            e.printStackTrace();
        }
      
        activeWindows = new Vector<VisWindow>();
      
        new Thread(this).start();
    }

    public byte[] readFully(SocketChannel channel, int bytes){
      
        int readBytes = 0;
        ByteBuffer inBB = ByteBuffer.allocate(bytes);
      
        while (readBytes < bytes){
            try{
                //read that number of bytes
                readBytes += channel.read(inBB);
            }
            catch(ClosedChannelException f){
                System.out.println("The socket has been closed but I am continuing");
                return inBB.array();
            }
            catch(IOException e){
                System.out.println("The socket has exception return null");
                return inBB.array();
            }
        }
        return inBB.array();
    }
   
    //http://www.bigbold.com/snippets/posts/show/94
    public int byteArrayToInt(byte[] b, int offset){
        int value = 0;
        for (int i = 0; i < 4; i++){
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
   
    //selector loop
    public void run(){
        try{
            Selector mySelector = Selector.open();
            SelectionKey acceptKey = masterChannel.register(mySelector, SelectionKey.OP_ACCEPT);
         
            while (true){
                try{
                    //for each new connection, create a graph window and update it as more data arrives from the client
                    int readyKeys = mySelector.select();
               
                    if (readyKeys > 0){
                        Set<SelectionKey> keySet = mySelector.selectedKeys();
                        SelectionKey[] selectedKeys = (SelectionKey[]) keySet.toArray(new SelectionKey[0]);
                  
                        for (int i = 0; i < selectedKeys.length; i++){
                            if (selectedKeys[i].equals(acceptKey)){
                                //new connection
                                SocketChannel newClientChannel = masterChannel.accept();
                                newClientChannel.configureBlocking(false);

                                newClientChannel.register(mySelector, SelectionKey.OP_READ);

                                handleConnection(newClientChannel.socket().getRemoteSocketAddress());

                                //notify JVM that I've dealt with this event:
                                keySet.remove(selectedKeys[i]);
                            }
                            else{
                                //some data
                                SocketChannel clientChannel = (SocketChannel) selectedKeys[i].channel();

                                //catch EOF exceptions & remove the key from the selector
                                try{
                                    //get how many bytes the message contains
                                    byte[] size = readFully(clientChannel, 4);

                                        int msgSize = byteArrayToInt(size, 0);

                                        byte[] data = readFully(clientChannel, msgSize);

                                        //convert it to a VisMessage object
                                        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(data));
                                        VisMessage msg = null;
                                        try{
                                            msg = (VisMessage) inStream.readObject();
                                            handleData(clientChannel.socket().getRemoteSocketAddress(), msg);
                                        }
                                        catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    //notify JVM that I've dealt with this event:
                                    keySet.remove(selectedKeys[i]);
                                }
                                catch(EOFException e){
                                    //assume client was killed, unreg. its key (close() does this)
                                    keySet.remove(selectedKeys[i]);
                                    clientChannel.close();
                                }
                            }
                        }
                    }
                else
                    try{
                        Thread.sleep(150);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        catch(IOException e){
            System.out.println("Fatal:");
            e.printStackTrace();
        }
    }
   
   
    //method called when a new client connects
    public void handleConnection(SocketAddress clientAddress){
        //prepare a new visualization window
        VisWindow newWindow = new VisWindow(new VisualGraph("OpenCOM on " + clientAddress.toString()), clientAddress);
        activeWindows.addElement(newWindow);
        newWindow.visWindow.setVisible(true);
    }

    private String getAsStringList(String[] array){
        String result = "";
        if (array.length > 0)
            result = array[0];
      
        for (int i = 1; i < array.length; i++){
            result += ", " + array[i];
        }
        
        return result;
    }
   
    //method called when data arrives from one of the connected clients
    public void handleData(SocketAddress sender, VisMessage data){
        //update the relevant visualization window
        for (int i = 0; i < activeWindows.size(); i++){
            if (activeWindows.elementAt(i).clientAddress.equals(sender)){
                if (data instanceof ComponentCreatedMessage){
                    String componentName = ((ComponentCreatedMessage) data).componentName;
                    String inFramework = ((ComponentCreatedMessage) data).inFramework;
                    String[] interfaces = ((ComponentCreatedMessage) data).interfaces;
                    String[] receptacles = ((ComponentCreatedMessage) data).receptacles;
                    
                    System.out.println("Component = "+componentName);
                    
                    if (((ComponentCreatedMessage) data).type == ComponentCreatedMessage.TYPE_COMPONENT)
                        activeWindows.elementAt(i).visWindow.componentCreated(componentName, interfaces, receptacles, inFramework);
                    else if (((ComponentCreatedMessage) data).type == ComponentCreatedMessage.TYPE_FRAMEWORK)
                        activeWindows.elementAt(i).visWindow.frameworkCreated(componentName, interfaces, receptacles, inFramework);
                  
                    activeWindows.elementAt(i).componentCount++;
               
                    if (activeWindows.elementAt(i).componentCount > 100)
                        System.out.println("W     A     R     N     I     N     G:    >  1 0 0");
                }
                else if (data instanceof ComponentDeletedMessage){
                    String componentName = ((ComponentDeletedMessage) data).componentName;
                    String inFramework = ((ComponentDeletedMessage) data).inFramework;
                    activeWindows.elementAt(i).visWindow.componentDeleted(componentName, inFramework);
                }
                else if (data instanceof ConnectionMadeMessage){
                    String sourceComponentName = ((ConnectionMadeMessage) data).sourceComponentName;
                    String sinkComponentName = ((ConnectionMadeMessage) data).sinkComponentName;
                    String inFramework = ((ConnectionMadeMessage) data).inFramework;
                    String iid = ((ConnectionMadeMessage) data).iid;
                    activeWindows.elementAt(i).visWindow.connectionMade(sourceComponentName, sinkComponentName, iid, inFramework);
                }
                else if (data instanceof ConnectionBrokenMessage){
                    String sourceComponentName = ((ConnectionBrokenMessage) data).sourceComponentName;
                    String sinkComponentName = ((ConnectionBrokenMessage) data).sinkComponentName;
                    String inFramework = ((ConnectionBrokenMessage) data).inFramework;
                    String iid = ((ConnectionBrokenMessage) data).iid;
                    activeWindows.elementAt(i).visWindow.connectionBroken(sourceComponentName, sinkComponentName, iid, inFramework);
                }
                else if (data instanceof InterfaceExposedMessage){
                    String componentName = ((InterfaceExposedMessage) data).componentName;
                    String interfaceName = ((InterfaceExposedMessage) data).interfaceName;
                    String inFramework = ((InterfaceExposedMessage) data).inFramework;
                    activeWindows.elementAt(i).visWindow.interfaceExposed(componentName, interfaceName, inFramework);
                }
                else if (data instanceof InterfaceUnExposedMessage){
                    String componentName = ((InterfaceUnExposedMessage) data).componentName;
                    String interfaceName = ((InterfaceUnExposedMessage) data).interfaceName;
                    String inFramework = ((InterfaceUnExposedMessage) data).inFramework;
                    activeWindows.elementAt(i).visWindow.interfaceRemoved(componentName, interfaceName, inFramework);
                }
                else if (data instanceof ReceptacleExposedMessage){
                    String componentName = ((ReceptacleExposedMessage) data).componentName;
                    String receptacleName = ((ReceptacleExposedMessage) data).receptacleName;
                    String inFramework = ((ReceptacleExposedMessage) data).inFramework;
                    activeWindows.elementAt(i).visWindow.receptacleExposed(componentName, receptacleName, inFramework);
                }
                else if (data instanceof ReceptacleUnExposedMessage){
                    String componentName = ((ReceptacleUnExposedMessage) data).componentName;
                    String receptacleName = ((ReceptacleUnExposedMessage) data).receptacleName;
                    String inFramework = ((ReceptacleUnExposedMessage) data).inFramework;
                    activeWindows.elementAt(i).visWindow.receptacleRemoved(componentName, receptacleName, inFramework);
                }
            
                return;
            }
        }
    }

   public static void main(String[] args)
      {
      new RemoteVisServer();
      System.out.println("Ready for client connections...");
      }
   
   }