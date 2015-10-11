package interfaces.IConfigInfo;

import OpenCOM.IUnknown;
import aodvstate.ConfigInfo;

public interface IConfigInfo extends IUnknown{
	
	
	public void initialise(String cfg) throws Exception;
	
	/**
	 * Method to read configuration information from
	 * the given File object and place them in the
	 * current object.
	 * @param File cfgFile - File object of config file
	 */
	public void readInfo(String cfg) throws Exception;

	/**
	 * Method to update configuration info. It creates a
	 * file, if no file is available and if the file is
	 * available, then cxlears the file, before writing
	 *
	 */
	public void updateInfo() throws Exception;

	/**
	 * Method to validate the config information given in the
	 * passed parameter.
	 * @param ConfigInfo tempCfg - Confdig object with parameters
	 *
	 */
	public void validateInfo(ConfigInfo tempCfg) throws Exception;

	/**
	 * Method to get values from a given config object
	 * @param ConfigInfo tempCfg - Config object, from which to
	 *				get values
	 */
	public void setValuesUsing(ConfigInfo tempCfg);

}