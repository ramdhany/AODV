/*
 * OCM_MultiReceptacle.java
  *
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

package OpenCOM;
import java.util.*;

/**
 * Programming abstraction for a multi-receptacle. Multiple components all
 * implementing the same interface type can be connected to this receptacle.
 * An index is then use to invoke one or more of the connections e.g.
 * <p>
 * public OCM_MultiReceptacle<IInterfaceType> m_PSR_IIntfType 
 *    = new OCM_MultiReceptacle<IInterfaceType>(IInterfaceType.class);
 * <p>
 * m_PSR_IIntfType.interfaceList.get(index).foo(params);
 *
 * @author  Paul Grace
 * @version 1.3.2 
 */

public class OCM_MultiReceptacle<InterfaceType> implements IReceptacle{
    /** Interface type of this receptacle. */
    public String iid;
    
    public Class class_type;
    /** List of interface pointers this receptacle is connected to. */
    public Vector<InterfaceType> interfaceList;
    
    /** List of connIDS for each connection of this receptacle. */
    public Vector<Long> connIDS;
    
    /** List of components that this receptacle is connected to. */
    private Vector<IUnknown> components;
    
    private int numberOfConnections;    
    private Hashtable<String, TypedAttribute> metaData;         // Meta-data stored on this receptacle
    
    /** 
     * Constructor creates a new instance of OCM_MultiReceptacle object. Usually called
     * from within OpenCOM component constructors.
     * @param cls_type The type of interface to initialse this receptacle to
     */
    public OCM_MultiReceptacle(Class<InterfaceType> cls_type) {
        interfaceList = new Vector<InterfaceType>();
        connIDS = new Vector<Long>();
        components = new Vector<IUnknown>();
        numberOfConnections = 0;
        class_type = cls_type;
        iid = cls_type.toString();
    }
    
    //! Implementation of IReceptacle interface
    /**
     * This method connects the recpetacle to given component on the given interface type.
     * @param pIUnkSink Reference to the sink component who hosts the interface that the receptacle is to be connected to.
     * @param riid A string representing the interface type of the connection.
     * @param provConnID A long representing the generated unqiue ID of this particular connection.
     * @return A boolean indicating the success of this operation
     **/
    public boolean connectToRecp(IUnknown pIUnkSink, String riid, long provConnID) {
        // Get the reference to the component hosting the interface
        try{
            InterfaceType pIntfRef = (InterfaceType) pIUnkSink.QueryInterface(riid);
            // Add the component, reference and id to the receptacles object stores
            components.add(pIUnkSink);       
            interfaceList.add(pIntfRef);
            connIDS.add(new Long( provConnID));
            numberOfConnections++;
            return true;
        }
        catch(ClassCastException e){
            System.err.println("Connect Failed: Connecting Receptacle and Interface of different types");
            return false;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * This method disconnects a given receptacle
     * @param connID A long representing the generated unqiue ID of this particular connection.
     * @return A boolean indicating the success of this operation
     **/
    public boolean disconnectFromRecp(long connID) {
        // Traverse the receptacle data looking for the required connection ID
        for(int i = 0; i < numberOfConnections ; i++) {
            Long vecConnID = connIDS.elementAt(i);
            if(vecConnID.longValue() == connID) {
                // Found it - now remove all pieces of information about that connection
                numberOfConnections--;
                interfaceList.remove(i);
                connIDS.remove(i);
                return true;
            }
	}

	return false;
    }
    
    /**
     * This method adds meta-data name-value pair attributes to the receptacle instance.
     * @param name The attribute name.
     * @param type The attribute name.
     * @param value An Object holding the attribute value.
     * @return A boolean describing if the pair was added or not.
     */
    public boolean putData(String name, String type, Object value){
        try{
            TypedAttribute aNewAttr= new TypedAttribute(type, value);
            metaData.put(name,aNewAttr);
        }
        catch(NullPointerException n){
            return false;
        }
        return true;
    }
    
    /**
     * This method gets the value of a named meta-data attribute.
     * @param name The attribute name.
     * @return The TypedAttribute object storing the value.
     */
    public TypedAttribute getValue(String name){
        return  (TypedAttribute) metaData.get(name);
    }
    
    /**
    * This method returns all name-value meta-data pairs on this receptacle instance.
    * @return A Hashtable storing the pairs.
    */
    public Hashtable<String, TypedAttribute> getValues() {
        return metaData;
    }
     
}
