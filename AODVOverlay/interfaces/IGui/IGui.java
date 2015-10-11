package interfaces.IGui;

import OpenCOM.IUnknown;

public interface IGui extends IUnknown{

	public abstract void loadGUI() throws Exception;

	/**
	 * Method to stop the protocol handler. This is done
	 * by requesting route manager to stop activities and
	 * then performs an exit with success return code. This
	 * method is called by the GUI
	 */
	public abstract void exitApplication() throws Exception;

	/**
	 * Method to start the protocol handler. This is done by
	 * calling route manager. This method is request by the
	 * GUI.
	 */
	public abstract boolean startApplication()throws Exception;

	/**
	 * Method to stop the protocol handler. Requested by
	 * the GUI. Calls the route manager to stop the
	 * protocol handler.
	 */
	public abstract void stopApplication() throws Exception;

	/**
	 * Method to update the GUI when information
	 * changes. Requested by the route manager.
	 */
	public abstract void updateDisplay() throws Exception;
	
	
	public boolean isGUILoaded() throws Exception;

}