package interfaces.ILog;

import OpenCOM.IUnknown;

public interface ILog extends IUnknown{
	
//	 logging level definitions
	public static final int CRITICAL_LOGGING = 1;
	public static final int ACTIVITY_LOGGING = 2;
	public static final int INFO_LOGGING     = 3;
	public static final int MIN_LOGGING      = 1;
	public static final int MAX_LOGGING      = 3;
	
	/**
	 * Method to start the logger
	 * @exception Exception - thrown if error in integer & IO functions
	 */
	public void start() throws Exception;

	/**
	 * Method to start the logger
	 * @exception Exception - thrown if error in IO functions
	 */
	public void stop() throws Exception;

	/**
	 * Method to write to the log file. Whether to write or not is
	 * determined by the log level defined by the user and the log level
	 * assigned in the program (ll).
	 * @param int ll - predetermined log level
	 * @param String line - string to log to log file
	 */
	public void write(int ll, String line);

}