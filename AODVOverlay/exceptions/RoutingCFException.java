/**
 * 
 */
package exceptions;

/**
 * @author Rajiv Ramdhany
 *
 */
@SuppressWarnings("serial")
public class RoutingCFException extends Exception {
	
	public static final byte NO_CONFIGINFO_CONNECTED 			= 1;
	public static final byte IOEXCEPTION 						= 2;
	public static final byte NO_LOGGER_CONNECTED 				= 3;
	public static final byte NO_CONTROL_COMPONENT_CONNECTED 	= 4;
	public static final byte NO_FORWARD_COMPONENT_CONNECTED 	= 5;
	public static final byte NO_STATE_COMPONENT_CONNECTED 		= 6;
	public static final byte NO_PACKET_SENDER_CONNECTED 		= 7;
	public static final byte NO_PACKET_LISTENER_CONNECTED 		= 8;
	public static final byte NO_OSOPERATIONS_CONNECTED 			= 9;
	
	private final byte kind;

	  public byte getKind() {
	    return kind;
	  }
	
	public RoutingCFException(byte kind)
	{
			this.kind = kind;
	}

}
