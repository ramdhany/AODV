/*
 * IControl.java
 * GridKit 1.0
 * Created on 09 August 2004, 16:53
 */
package interfaces.IControl;

import OpenCOM.IUnknown;
import common.ResultCode.ResultCode;


/**
 * The IControl Interface is common to all overlay implementations. It is implemented
 * by the instance of the Control component that manages the network configuration
 * of overlays. IControl provides operations to tell the node to create, join or leave
 * individual instances of the overlay type.
 *
 * @author  Paul Grace
 * @version GridKit 1.0
 */
public interface IControl extends IUnknown{
    /**
     * The create operation is used to initiate an overlay network. Therefore, it
     * is executed by the primary node in the configuration. Subsequent members
     * of the overlay use join to integrate with the existing nodes. Generally, this
     * operation need not always be provided, as certain overlays can rely on first 
     * join being the create. It is useful if you want to implement explicit advertisement
     * of a newly creating overlay network instance though.
     * @param networkIdentifier The String Identifier of overlay instance to create.
     * @return An error code indicating if the overlay network instance was created.
     */
    ResultCode Create(String networkIdentifier, Object additionalParameters);
    
    /**
     * Join allows a node to join to a particular overlay instance given its identifier.
     * @param networkIdentifier The String Identifier of overlay instance to join to.
     * @return An error code indicating if the overlay network instance was joined to.
     */
    ResultCode Join(String networkIdentifier, Object additionalParameters);
    
    /**
     * Leave removes the node from the overlay network.
     * @param networkIdentifier The String Identifier of overlay instance to leave.
     * @return A error code indicating if the node left the overlay network or not.
     */
    ResultCode Leave(String networkIdentifier) ;
}
