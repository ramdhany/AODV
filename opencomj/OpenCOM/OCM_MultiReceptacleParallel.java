/*
 * OCM_MultiReceptacle.java
 *
 * Created on 23 July 2004, 13:52
 */

package OpenCOM;
import java.util.*;
import java.lang.reflect.*;

/**
 * Programming abstraction for a multi-receptacle with parallel invocations. Multiple components all
 * implementing the same interface type can be connected to this receptacle.
 * When invoked each connection executes in a separate thread. Note, there are no return values; hence
 * void methods are appropriate. We advocate the use of callbacks to handle the return of results
 * from multiple executing methods.
 * 
 * <p>
 * public OCM_MultiReceptacleParallel<IInterfaceType> m_PSR_IIntfType 
 *    = new OCM_MultiReceptacleParallel<IInterfaceType>(IInterfaceType.class);
 * <p>
 * m_PSR_IIntfType.m_pIntf.foo(params);
 *
 * @author  Paul Grace
 * @version 1.3.2
 */

public class OCM_MultiReceptacleParallel<InterfaceType> implements IReceptacle{
    
    class DebugProxy implements java.lang.reflect.InvocationHandler {

        class invocationThread extends Thread{
            int index;
            Method method;
            Object[] args;
            
            public invocationThread(int val, Method m, Object[] arguments){
                this.index=val;
                this.method=m;
                this.args=arguments;
            }
            
            public void run(){
                try{
                    method.invoke(interfaceList.get(0), args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        private Object obj;

        public Object newInstance(Object obj) {
            return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new DebugProxy(obj));
        }

        private DebugProxy(Object obj) {
            this.obj = obj;
        }

        public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable
            {
            Object result=null;
            try {
                for(int i=0; i<interfaceList.size();i++ ){
                    invocationThread newThr = new invocationThread(i, m, args);
                    newThr.start();
                }
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: " +
				       e.getMessage());
            }  
            return result;
        }
    }   
    
    
    /* The reference point of the interface this receptacle is connected to. */
    public String iid;
    
    public Class class_type;
    public InterfaceType m_pIntf;
    
    /** List of interface pointers this receptacle is connected to. */
    private Vector<Object> interfaceList;
    
    /** List of connIDS for each connection of this receptacle. */
    private Vector<Long> connIDS;
    
    private int numberOfConnections;    
    private Hashtable<String, TypedAttribute> metaData;         // Meta-data stored on this receptacle
    
    /** 
     * Constructor creates a new instance of OCM_MultiReceptacle object. Usually called
     * from within OpenCOM component constructors.
     * @param cls_type The type of interface to initialse this receptacle to
     */ 
    public OCM_MultiReceptacleParallel(Class<InterfaceType> cls_type) {
        interfaceList = new Vector<Object>();
        connIDS = new Vector<Long>();
        numberOfConnections = 0;
        class_type = cls_type;
        ClassLoader cl2 = cls_type.getClassLoader();
        m_pIntf = (InterfaceType) Proxy.newProxyInstance(cl2,
                new Class[] {cls_type}, new DebugProxy((InterfaceType) this));
        iid = cls_type.toString();        
    }
    
    //! Implementation of IReceptacle interface
    ////////////////////////////////////////////////////////////////////////////////
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
            interfaceList.add(pIntfRef);
        }
        catch(ClassCastException e){
            System.err.println("Connect Failed: Connecting Receptacle and Interface of different types");
            return false;
        }

        // Add the component, reference and id to the receptacles object stores      
        connIDS.add(new Long( provConnID));

        numberOfConnections++;
        return true;
    }
    
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
            }
            if(numberOfConnections ==0) {
                m_pIntf = null;
                return true;
            }
            return true;
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
