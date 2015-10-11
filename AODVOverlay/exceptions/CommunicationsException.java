/**
 * 
 */
package exceptions;

/**
 * @author Rajiv Ramdhany
 *
 */
@SuppressWarnings("serial")
public class CommunicationsException extends Exception {
	
	public static final byte UNREACHABLE_DESTINATION 	= 1;
	public static final byte IOEXCEPTION 				= 2;
	public static final byte UNKNOWN_HOST 				= 2;
	public static final byte INVALID_ENDPOINT_TYPE		= 3;
	public static final byte NO_SUCH_CONSTRUCTOR		= 4;
	public static final byte CONSTRUCTOR_ILLEGAL_ACCESS	= 5;
	public static final byte ABSTRACT_CLASS				= 6;
	public static final byte MALFORMED_ENDPOINT_PATTERN	= 7;
	public static final byte INVALID_NETWORK_INTERFACE	= 8;
	
	private final byte kind;

	  public byte getKind() {
	    return kind;
	  }
	
	public CommunicationsException(byte kind)
	{
			this.kind = kind;
	}

}
