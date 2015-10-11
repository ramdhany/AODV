/*

AODV Overlay v0.5.3 Copyright 2007-2010  Lancaster University
Rajiv Ramdhany

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package aodvstate;

import interfaces.IConfigInfo.IConfigInfo;

import java.io.*;
import java.net.*;

import jpcap.NetworkInterface;

import log.Logging;

import OpenCOM.ILifeCycle;
import OpenCOM.IMetaInterface;
import OpenCOM.IUnknown;
import OpenCOM.OpenCOMComponent;


/**
* This class provide configuration information of the Protocol
* Handler. It provides methods to retrieve, update & validate
* parameters held in a configuration file
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
* 
* modified by: Rajiv Ramdhany
* date: 12/02/2007
* email: r.ramdhany@comp.lancs.ac.uk
*
*/
public class ConfigInfo extends OpenCOMComponent implements IConfigInfo, IUnknown,
														IMetaInterface, ILifeCycle{

	// control variables
	public boolean firstTime = false;
	public String cfgFileName = "@!$";
	public boolean infoChanged = false;
	public String NetworkIdentifier;


	// common constants
	public static final String YES = "yes";
	public static final String NO = "no";

	public static final String RERR_UNICAST = "unicast";
	public static final String RERR_MULTICAST = "multicast";
	public static final int RERR_UNICAST_VAL = 1;
	public static final int RERR_MULTICAST_VAL = 2;

	public static final String DELETE_MODE_HELLO = "hello";
	public static final String DELETE_MODE_LINKLAYER = "linklayer";
	public static final int DELETE_MODE_HELLO_VAL = 1;
	public static final int DELETE_MODE_LINKLAYER_VAL = 2;

	public static final String GUI_MODE = "gui";
	public static final String NON_GUI_MODE = "non-gui";

	public static final String LINUX_OS = "linux";
	public static final String WINDOWS_OS = "windows";
	public static final String ZAURUS_OS = "zaurus";

	public static final String IPv4_VERSION = "ipv4";
	public static final String IPv6_VERSION = "ipv6";
	public static final int IPv4_VERSION_VAL = 4;
	public static final int IPv6_VERSION_VAL = 6;

	public static final String ROUTE_DISCOVERY_ERS = "ers";
	public static final String ROUTE_DISCOVERY_NON_ERS = "non-ers";
	public static final int ROUTE_DISCOVERY_ERS_VAL = 1;
	public static final int ROUTE_DISCOVERY_NON_ERS_VAL = 2;


	// configuration constants
	public static final String MAC_ADDRESS_OF_GATEWAY = "00:00:00:00:00:00";


	// configuration variables ; application
	public String executionMode = GUI_MODE;
	public String osInUse = LINUX_OS;
	public String ipVersion = IPv4_VERSION;
	public String ipAddress = "104.101.112.230";
	public String ifaceName = "eth1";
	public String ipAddressGateway = "104.101.112.225";
	public String loIfaceName = "lo";
	public String loggingStatus = NO;
	public String loggingLevel = "1";
	public String logFile = "./jadhoc.log";
	public String pathToSystemCmds = "/sbin";
	public String onlyDestination = YES;
	public String gratuitousRREP = NO;
	public String RREPAckRequired = NO;
	public String ipAddressMulticast = "224.0.0.2";
	public String RERRSendingMode = RERR_UNICAST;
	public String deletePeriodMode = DELETE_MODE_HELLO;       // (hello or linklayer)
	public String routeDiscoveryMode = ROUTE_DISCOVERY_ERS; // ers or non-ers
	public String packetBuffering = NO;

	// configuration variables ; aodv
	public String activeRouteTimeout = "3000"; 	//   ACTIVE_ROUTE_TIMEOUT     3,000 Milliseconds
	public String allowedHelloLoss = "2";      	//   ALLOWED_HELLO_LOSS       2
   	public String helloInterval = "1000";		//   HELLO_INTERVAL           1,000 Milliseconds
	public String localAddTTL = "2";		//   LOCAL_ADD_TTL            2
	public String netDiameter = "35";		//   NET_DIAMETER             35
	public String nodeTraversalTime = "40";		//   NODE_TRAVERSAL_TIME      40 milliseconds
	public String RERRRatelimit = "10";		//   RERR_RATELIMIT           10
	public String RREQRetries = "6";		//   RREQ_RETRIES             2
	public String RREQRateLimit = "10";		//   RREQ_RATELIMIT           10
	public String timeoutBuffer = "2";		//   TIMEOUT_BUFFER           2
	public String TTLStart = "1";			//   TTL_START                1
	public String TTLIncrement = "2";		//   TTL_INCREMENT            2
	public String TTLThreshold = "10";		//   TTL_THRESHOLD            7


	//public String deletePeriod = "999";		//   DELETE_PERIOD            see note below (hello, linklayer)
	//public String blacklistTimeout = "999";     	//   BLACKLIST_TIMEOUT        RREQ_RETRIES * NET_TRAVERSAL_TIME
	//public String maxRepairTTL = "10";		//   MAX_REPAIR_TTL           0.3 * NET_DIAMETER
	//public String minRepairTTL = "999";		//   MIN_REPAIR_TTL           see note below
	//public String myRouteTimeout = "6000";		//   MY_ROUTE_TIMEOUT         2 * ACTIVE_ROUTE_TIMEOUT
	//public String netTraversalTime = "2800";	//   NET_TRAVERSAL_TIME       2 * NODE_TRAVERSAL_TIME * NET_DIAMETER
	//public String nextHopWait = "50";		//   NEXT_HOP_WAIT            NODE_TRAVERSAL_TIME + 10
	//public String pathDiscoveryTime = "5600";	//   PATH_DISCOVERY_TIME      2 * NET_TRAVERSAL_TIME
	//public String ringTraversalTime = "999";	//   RING_TRAVERSAL_TIME      2 * NODE_TRAVERSAL_TIME * (TTL_VALUE + TIMEOUT_BUFFER)
	//public String TTLValue = "999";			//   TTL_VALUE                see note below



	// configuration variable labels
	public String executionModeStr = "ExecutionMode";
	public String osInUseStr = "OSInUse";
	public String ipVersionStr = "IPVersion";
	public String ipAddressStr = "IPAddress";
	public String ifaceNameStr = "IfaceName";
	public String ipAddressGatewayStr = "IPAddressGateway";
	public String loIfaceNameStr = "LoopbackIfaceName";
	public String loggingStatusStr = "LoggingStatus";
	public String loggingLevelStr = "LoggingLevel";
	public String logFileStr = "LogFile";
	public String pathToSystemCmdsStr = "PathToSystemCmds";
	public String onlyDestinationStr = "OnlyDestination";
	public String gratuitousRREPStr = "GratuitousRREP";
	public String RREPAckRequiredStr = "RREPAckRequired";
	public String ipAddressMulticastStr = "IPAddressMulticast";
	public String RERRSendingModeStr = "RERRSendingMode";
	public String deletePeriodModeStr = "DeletePeriodMode";
	public String routeDiscoveryModeStr = "RouteDiscoveryMode";
	public String packetBufferingStr = "PacketBuffering";

	public String activeRouteTimeoutStr = "ActiveRouteTimeout";
	public String allowedHelloLossStr = "AllowedHelloLoss";
   	public String helloIntervalStr = "HelloInterval";
	public String localAddTTLStr = "LocalAddTTL";
	public String netDiameterStr = "NetDiameter";
	public String nodeTraversalTimeStr = "NodeTraversalTime";
	public String RERRRatelimitStr = "RERRRatelimit";
	public String RREQRetriesStr = "RREQRetries";
	public String RREQRateLimitStr = "RREQRateLimit";
	public String timeoutBufferStr = "TimeoutBuffer";
	public String TTLStartStr = "TTLStart";
	public String TTLIncrementStr = "TTLIncrement";
	public String TTLThresholdStr = "TTLThreshold";

	// Parameters in their real data type. These parameters will be used
	// by the protocol handler when accessing. This is done to speed the
	// activities of the protocol handler.
	public String executionModeVal;
	public String osInUseVal;
	public int ipVersionVal;
	public InetAddress ipAddressVal;
	public String ifaceNameVal;
	public InetAddress ipAddressGatewayVal;
	public String loIfaceNameVal;
	public boolean loggingStatusVal;
	public int loggingLevelVal;
	public String logFileVal;
	public String pathToSystemCmdsVal;
	public boolean onlyDestinationVal;
	public boolean gratuitousRREPVal;
	public boolean RREPAckRequiredVal;
	public InetAddress ipAddressMulticastVal;
	public int RERRSendingModeVal;
	public int deletePeriodModeVal;
	public int routeDiscoveryModeVal;
	public boolean packetBufferingVal;

	public int activeRouteTimeoutVal;
	public int allowedHelloLossVal;
	public int blacklistTimeoutVal;
	public int deletePeriodVal;
   	public int helloIntervalVal;
	public int localAddTTLVal;
	public int maxRepairTTLVal;
	public int myRouteTimeoutVal;
	public int netDiameterVal;
	public int netTraversalTimeVal;
	public int nextHopWaitVal;
	public int nodeTraversalTimeVal;
	public int pathDiscoveryTimeVal;
	public int RERRRatelimitVal;
	public int RREQRetriesVal;
	public int RREQRateLimitVal;
	public int timeoutBufferVal;
	public int TTLStartVal;
	public int TTLIncrementVal;
	public int TTLThresholdVal;
	
	// added by Rajiv Ramdhany
	public boolean RouterActive = false; 
	
	
	
	
	/**
	* Constructor to create ConfigInfo and read config
	* information from the given file.
	* @param String cfg - Name of config file
	*/
	public ConfigInfo(IUnknown pRuntime) throws Exception {
		super(pRuntime);
	}
	
	public void initialise(String cfg) throws Exception {
		
		cfgFileName = cfg;

        File cfgFile = new File(cfgFileName.trim());
        if(!cfgFile.exists()) {
        	createFileWithDefaults();
        } else {
        	if(!cfgFile.canRead() || !cfgFile.canWrite()) {
                	throw new Exception("Cannot read or write to file - " + cfgFileName);
        	}
        }

        readInfo(cfgFileName);
	}
	

	/**
	* Method to create a config file using the default
	* values.
	*
	*/
	private void createFileWithDefaults() throws Exception {
		updateInfo();
		firstTime = true;
	}

	/**
	* Method to read configuration information from
	* the given File object and place them in the
	* current object.
	* @param File cfgFile - File object of config file
	*/
        public void readInfo(String cfg) throws Exception {
                String strLine, strTag, strValue;
                cfgFileName = cfg;

                File cfgFile = new File(cfgFileName.trim());
                if(!cfgFile.exists()) {
                	createFileWithDefaults();
                } else {
                	if(!cfgFile.canRead() || !cfgFile.canWrite()) {
                        	throw new Exception("Cannot read or write to file - " + cfgFileName);
                	}
                }
                
                int lineNo = 0;

                BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFileName)));

                while((strLine = r.readLine()) != null) {
                        lineNo++;

			if(strLine.trim().length() == 0
			   || strLine.charAt(0) == '#') {
				continue;
			}

            try {
				// separate the line to tag and value
                                strLine = strLine.trim();
                                strTag = strLine.substring(0, strLine.indexOf('=')).trim();
                                strValue = strLine.substring((strLine.indexOf('=') + 1), (strLine.length())).trim();

				// check & get each config variable
				if(strTag.equals(executionModeStr)) {
					if(!strValue.toLowerCase().equals(NON_GUI_MODE)
					     && !strValue.toLowerCase().equals(GUI_MODE)) {
						throw new Exception("Invalid Execution mode ("
									+ GUI_MODE + " or "
									+ NON_GUI_MODE + ")");
					}
					executionMode = strValue;
					executionModeVal = strValue;

				} else if(strTag.equals(osInUseStr)) {
                                        if(!strValue.toLowerCase().equals(WINDOWS_OS)
                                            && !strValue.toLowerCase().equals(LINUX_OS)) {
						throw new Exception("Invalid osInUse - ("
								+ WINDOWS_OS + " or "
								+ LINUX_OS + ")");
					}
					osInUse = strValue;
					osInUseVal = strValue;

				} else if(strTag.equals(ipVersionStr)) {
                                        if(!strValue.toLowerCase().equals(IPv4_VERSION)
                                            && !strValue.toLowerCase().equals(IPv6_VERSION)) {
                                            	throw new Exception("Invalid ipVersion - ("
								+ IPv4_VERSION + " or "
								+ IPv6_VERSION + ")");
					}

					///////////// temporary code
					if(strValue.toLowerCase().equals(IPv6_VERSION)) {
                                            	throw new Exception("IPv6 not supported at this time");
					}
					///////////// end of temprorary code

					ipVersion = strValue;
                                        if(strValue.toLowerCase().equals(IPv4_VERSION))
						ipVersionVal = IPv4_VERSION_VAL;
					else
						ipVersionVal = IPv6_VERSION_VAL;

				} else if(strTag.equals(ipAddressStr)) {
                                        InetAddress adr = InetAddress.getByName(strValue);
					if(ipVersionVal == IPv4_VERSION_VAL) {
						if(!(adr instanceof Inet4Address))
                                                	throw new Exception("Invalid ipAddress - must be IPv4");
					} else if(ipVersionVal == IPv6_VERSION_VAL) {
						if(!(adr instanceof Inet6Address))
                                                	throw new Exception("Invalid ipAddress - must be IPv6");
					} else
                                        	throw new Exception("Invalid ipAddress - some inconsistancy");
                                        ipAddress = adr.getHostAddress();
                                        ipAddressVal = adr;

				} else if(strTag.equals(ifaceNameStr)) {
					if(strValue.trim().length() == 0)
                                        	throw new Exception("Invalid ifaceName - cannot be blank");
					ifaceName = strValue;
					ifaceNameVal = strValue;

				} else if(strTag.equals(ipAddressGatewayStr)) {
                                        InetAddress adr = InetAddress.getByName(strValue);
					if(ipVersionVal == IPv4_VERSION_VAL) {
						if(!(adr instanceof Inet4Address))
                                                	throw new Exception("Invalid ipAddressGateway - must be IPv4");
					} else if(ipVersionVal == IPv6_VERSION_VAL) {
						if(!(adr instanceof Inet6Address))
                                                	throw new Exception("Invalid ipAddressGateway - must be IPv6");
					} else
                                        	throw new Exception("Invalid ipAddressGateway - some inconsistancy");
                                        ipAddressGateway = adr.getHostAddress();
                                        ipAddressGatewayVal = adr;

				} else if(strTag.equals(loIfaceNameStr)) {
					if(strValue.trim().length() == 0)
                                        	throw new Exception("Invalid loIfaceName - cannot be blank");
					loIfaceName = strValue;
					loIfaceNameVal = strValue;

				} else if(strTag.equals(loggingStatusStr)) {
					if(!(strValue.toLowerCase().equals(YES)
						|| strValue.toLowerCase().equals(NO)))
						throw new Exception("Invalid LoggingStatus - Yes or No");
					loggingStatus = strValue;
					if(strValue.toLowerCase().equals(YES))
						loggingStatusVal = true;
					else
						loggingStatusVal = false;

				} else if(strTag.equals(loggingLevelStr)) {
					if(loggingStatus.toLowerCase().equals(YES)) {
						try {
							int num = Integer.parseInt(strValue);
							if(num < Logging.MIN_LOGGING || num > Logging.MAX_LOGGING)
								throw new Exception();
							loggingLevel = strValue;
							loggingLevelVal = num;
						} catch(Exception e) {
							throw new Exception("Invalid LoggingLevel - (between "
							 				+ Logging.MIN_LOGGING
										+ " and " + Logging.MAX_LOGGING + ")");
						}
					}

				} else if(strTag.equals(logFileStr)) {
					if(loggingStatus.toLowerCase().equals(YES)) {
						try {
							if(strValue.trim().length() == 0)
								throw new Exception();
							logFile = strValue;
							logFileVal = strValue;
						} catch(Exception e) {
							throw new Exception("Invalid LogFile " + strValue);
						}
					}

				} else if(strTag.equals(pathToSystemCmdsStr)) {
					//File dir = new File(strValue);
					//if(!dir.isDirectory())
					//	throw new Exception("Invalid PathToSystemCmds");
					if(pathToSystemCmdsStr.trim().length() == 0)
						throw new Exception("Invalid PathToSystemCmds");
					pathToSystemCmds = strValue;
					pathToSystemCmdsVal = strValue;

				} else if(strTag.equals(onlyDestinationStr)) {
					if(!(strValue.toLowerCase().equals(YES)
						|| strValue.toLowerCase().equals(NO)))
						throw new Exception("Invalid OnlyDestination - Yes or No");
					onlyDestination = strValue;
					if(strValue.toLowerCase().equals(YES))
						onlyDestinationVal = true;
					else
						onlyDestinationVal = false;

				} else if(strTag.equals(gratuitousRREPStr)) {
					if(!(strValue.toLowerCase().equals(YES)
						|| strValue.toLowerCase().equals(NO)))
						throw new Exception("Invalid GratuitousRREP - Yes or No");
					gratuitousRREP = strValue;
					if(strValue.toLowerCase().equals(YES))
						gratuitousRREPVal = true;
					else
						gratuitousRREPVal = false;

				} else if(strTag.equals(RREPAckRequiredStr)) {
					if(!(strValue.toLowerCase().equals(YES)
						|| strValue.toLowerCase().equals(NO)))
						throw new Exception("Invalid RREPAckRequired - Yes or No");
					RREPAckRequired = strValue;
					if(strValue.toLowerCase().equals(YES))
						RREPAckRequiredVal = true;
					else
						RREPAckRequiredVal = false;

				} else if(strTag.equals(ipAddressMulticastStr)) {
                                        InetAddress adr = InetAddress.getByName(strValue);
					if(ipVersionVal == IPv4_VERSION_VAL) {
						if(!(adr instanceof Inet4Address))
                                                	throw new Exception("Invalid ipAddressMulticast - must be IPv4");
					} else if(ipVersionVal == IPv6_VERSION_VAL) {
						if(!(adr instanceof Inet6Address))
                                                	throw new Exception("Invalid ipAddressMulticast - must be IPv6");
					} else
                                        	throw new Exception("Invalid ipAddressMulticast - some inconsistancy");
                                        ipAddressMulticast = adr.getHostAddress();
                                        ipAddressMulticastVal = adr;

				} else if(strTag.equals(RERRSendingModeStr)) {
					if(RERRSendingMode.toLowerCase().equals(RERR_UNICAST)) {
						RERRSendingModeVal = RERR_UNICAST_VAL;
					} else if(RERRSendingMode.toLowerCase().equals(RERR_MULTICAST)) {
						RERRSendingModeVal = RERR_MULTICAST_VAL;
					} else {
                                        	throw new Exception("Invalid RERRSendingMode - should be "
									+ RERR_UNICAST + " or "
									+ RERR_MULTICAST + " ");
					}

				} else if(strTag.equals(deletePeriodModeStr)) {
					try {
						if(strValue.toLowerCase().equals(DELETE_MODE_HELLO))
							deletePeriodModeVal = DELETE_MODE_HELLO_VAL;
						else if(strValue.toLowerCase().equals(DELETE_MODE_LINKLAYER))
							deletePeriodModeVal = DELETE_MODE_LINKLAYER_VAL;
						else
							throw new Exception();

						deletePeriodMode = strValue;
					} catch(Exception e) {
						throw new Exception("Invalid DeletePeriodMode ("
									+ DELETE_MODE_HELLO + " or "
									+ DELETE_MODE_LINKLAYER + ")");
					}

				} else if(strTag.equals(routeDiscoveryModeStr)) {
					try {
						if(strValue.toLowerCase().equals(ROUTE_DISCOVERY_ERS))
							routeDiscoveryModeVal = ROUTE_DISCOVERY_ERS_VAL;
						else if(strValue.toLowerCase().equals(ROUTE_DISCOVERY_NON_ERS))
							routeDiscoveryModeVal = ROUTE_DISCOVERY_NON_ERS_VAL;
						else
							throw new Exception();

						routeDiscoveryMode = strValue;
					} catch(Exception e) {
						throw new Exception("Invalid RouteDiscoveryMode ("
									+ ROUTE_DISCOVERY_ERS + " or "
									+ ROUTE_DISCOVERY_NON_ERS + ")");
					}

				} else if(strTag.equals(packetBufferingStr)) {
					try {
						if(strValue.toLowerCase().equals(YES))
							packetBufferingVal = true;
						else if(strValue.toLowerCase().equals(NO))
							packetBufferingVal = false;
						else
							throw new Exception();

						packetBuffering = strValue;
					} catch(Exception e) {
						throw new Exception("Invalid PacketBuffering ("
									+ YES + " or "
									+ NO + ")");
					}

				} else if(strTag.equals(activeRouteTimeoutStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						activeRouteTimeout = strValue;
						activeRouteTimeoutVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid activeRouteTimeout");
					}

				} else if(strTag.equals(allowedHelloLossStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						allowedHelloLoss = strValue;
						allowedHelloLossVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid AllowedHelloLoss");
					}

				} else if(strTag.equals(helloIntervalStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						helloInterval = strValue;
						helloIntervalVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid HelloInterval");
					}

				} else if(strTag.equals(localAddTTLStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						localAddTTL = strValue;
						localAddTTLVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid LocalAddTTL");
					}

				} else if(strTag.equals(netDiameterStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						netDiameter = strValue;
						netDiameterVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid NetDiameter");
					}

				} else if(strTag.equals(nodeTraversalTimeStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						nodeTraversalTime = strValue;
						nodeTraversalTimeVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid nodeTraversalTime");
					}

				} else if(strTag.equals(RERRRatelimitStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						RERRRatelimit = strValue;
						RERRRatelimitVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid RERRRatelimit");
					}

				} else if(strTag.equals(RREQRetriesStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						RREQRetries = strValue;
						RREQRetriesVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid RREQRetries");
					}

				} else if(strTag.equals(RREQRateLimitStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						RREQRateLimit = strValue;
						RREQRateLimitVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid RREQRateLimit");
					}

				} else if(strTag.equals(timeoutBufferStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						timeoutBuffer = strValue;
						timeoutBufferVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid TimeoutBuffer");
					}

				} else if(strTag.equals(TTLStartStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						TTLStart = strValue;
						TTLStartVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid TTLStart");
					}

				} else if(strTag.equals(TTLIncrementStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						TTLIncrement = strValue;
						TTLIncrementVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid TTLIncrement");
					}

				} else if(strTag.equals(TTLThresholdStr)) {
					try {
						int num = Integer.parseInt(strValue);
						if(num <= 0)
							throw new Exception();
						TTLThreshold = strValue;
						TTLThresholdVal = num;
					} catch(Exception e) {
						throw new Exception("Invalid TTLThreshold");
					}
				}

	 			netTraversalTimeVal = 2 * nodeTraversalTimeVal * netDiameterVal;
				blacklistTimeoutVal = RREQRetriesVal * netTraversalTimeVal;
	 			maxRepairTTLVal = (int) (0.3 * netDiameterVal);
	 			myRouteTimeoutVal = 2 * activeRouteTimeoutVal;
	 			nextHopWaitVal = nodeTraversalTimeVal + 10;
	 			pathDiscoveryTimeVal = 2 * netTraversalTimeVal;

				if(deletePeriodModeVal == 1) {
					deletePeriodVal = allowedHelloLossVal * helloIntervalVal;
				} else {
					deletePeriodVal = activeRouteTimeoutVal;
				}

			} catch(Exception e) {
                                throw new Exception("Problem in line " + lineNo + ": Error reported - " + e);
                        }

                }
                infoChanged = true;
        }

	/**
	* Method to update configuration info. It creates a
	* file, if no file is available and if the file is
	* available, then cxlears the file, before writing
	*
	*/
        public void updateInfo() throws Exception {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cfgFileName)));

		w.write("# ", 0, 2);
                w.newLine();
		w.write("#Read the sample-jadhoc.cfg for details ", 0, 40);
                w.newLine();
		w.write("# ", 0, 2);
                w.newLine();

                w.write(executionModeStr + "=", 0, executionModeStr.length() + 1);
                w.write(executionMode, 0, executionMode.length());
                w.newLine();

                w.write(osInUseStr + "=", 0, osInUseStr.length() + 1);
                w.write(osInUse, 0, osInUse.length());
                w.newLine();

                w.write(ipVersionStr + "=", 0, ipVersionStr.length() + 1);
                w.write(ipVersion, 0, ipVersion.length());
                w.newLine();

                w.write(ipAddressStr + "=", 0, ipAddressStr.length() + 1);
                w.write(ipAddress, 0, ipAddress.length());
                w.newLine();

                w.write(ifaceNameStr + "=", 0, ifaceNameStr.length() + 1);
                w.write(ifaceName, 0, ifaceName.length());
                w.newLine();

                w.write(ipAddressGatewayStr + "=", 0, ipAddressGatewayStr.length() + 1);
                w.write(ipAddressGateway, 0, ipAddressGateway.length());
                w.newLine();

		w.write(loIfaceNameStr + "=", 0, loIfaceNameStr.length() + 1);
                w.write(loIfaceName, 0, loIfaceName.length());
                w.newLine();

                w.write(loggingStatusStr + "=", 0, loggingStatusStr.length() + 1);
                w.write(loggingStatus, 0, loggingStatus.length());
                w.newLine();

                w.write(loggingLevelStr + "=", 0, loggingLevelStr.length() + 1);
                w.write(loggingLevel, 0, loggingLevel.length());
                w.newLine();

                w.write(logFileStr + "=", 0, logFileStr.length() + 1);
                w.write(logFile, 0, logFile.length());
                w.newLine();

                w.write(pathToSystemCmdsStr + "=", 0, pathToSystemCmdsStr.length() + 1);
                w.write(pathToSystemCmds, 0, pathToSystemCmds.length());
                w.newLine();

                w.write(onlyDestinationStr + "=", 0, onlyDestinationStr.length() + 1);
                w.write(onlyDestination, 0, onlyDestination.length());
                w.newLine();

                w.write(gratuitousRREPStr + "=", 0, gratuitousRREPStr.length() + 1);
                w.write(gratuitousRREP, 0, gratuitousRREP.length());
                w.newLine();

                w.write(RREPAckRequiredStr + "=", 0, RREPAckRequiredStr.length() + 1);
                w.write(RREPAckRequired, 0, RREPAckRequired.length());
                w.newLine();

		w.write(ipAddressMulticastStr + "=", 0, ipAddressMulticastStr.length() + 1);
                w.write(ipAddressMulticast, 0, ipAddressMulticast.length());
                w.newLine();

		w.write(RERRSendingModeStr + "=", 0, RERRSendingModeStr.length() + 1);
                w.write(RERRSendingMode, 0, RERRSendingMode.length());
                w.newLine();

                w.write(deletePeriodModeStr + "=", 0, deletePeriodModeStr.length() + 1);
                w.write(deletePeriodMode, 0, deletePeriodMode.length());
                w.newLine();

                w.write(routeDiscoveryModeStr + "=", 0, routeDiscoveryModeStr.length() + 1);
                w.write(routeDiscoveryMode, 0, routeDiscoveryMode.length());
                w.newLine();

                w.write(packetBufferingStr + "=", 0, packetBufferingStr.length() + 1);
                w.write(packetBuffering, 0, packetBuffering.length());
                w.newLine();

                w.write(activeRouteTimeoutStr + "=", 0, activeRouteTimeoutStr.length() + 1);
                w.write(activeRouteTimeout, 0, activeRouteTimeout.length());
                w.newLine();

                w.write(allowedHelloLossStr + "=", 0, allowedHelloLossStr.length() + 1);
                w.write(allowedHelloLoss, 0, allowedHelloLoss.length());
                w.newLine();

                w.write(helloIntervalStr + "=", 0, helloIntervalStr.length() + 1);
                w.write(helloInterval, 0, helloInterval.length());
                w.newLine();

                w.write(localAddTTLStr + "=", 0, localAddTTLStr.length() + 1);
                w.write(localAddTTL, 0, localAddTTL.length());
                w.newLine();

                w.write(netDiameterStr + "=", 0, netDiameterStr.length() + 1);
                w.write(netDiameter, 0, netDiameter.length());
                w.newLine();

                w.write(nodeTraversalTimeStr + "=", 0, nodeTraversalTimeStr.length() + 1);
                w.write(nodeTraversalTime, 0, nodeTraversalTime.length());
                w.newLine();

                w.write(RERRRatelimitStr + "=", 0, RERRRatelimitStr.length() + 1);
                w.write(RERRRatelimit, 0, RERRRatelimit.length());
                w.newLine();

                w.write(RREQRetriesStr + "=", 0, RREQRetriesStr.length() + 1);
                w.write(RREQRetries, 0, RREQRetries.length());
                w.newLine();

                w.write(RREQRateLimitStr + "=", 0, RREQRateLimitStr.length() + 1);
                w.write(RREQRateLimit, 0, RREQRateLimit.length());
                w.newLine();

                w.write(timeoutBufferStr + "=", 0, timeoutBufferStr.length() + 1);
                w.write(timeoutBuffer, 0, timeoutBuffer.length());
                w.newLine();

                w.write(TTLStartStr + "=", 0, TTLStartStr.length() + 1);
                w.write(TTLStart, 0, TTLStart.length());
                w.newLine();

                w.write(TTLIncrementStr + "=", 0, TTLIncrementStr.length() + 1);
                w.write(TTLIncrement, 0, TTLIncrement.length());
                w.newLine();

                w.write(TTLThresholdStr + "=", 0, TTLThresholdStr.length() + 1);
                w.write(TTLThreshold, 0, TTLThreshold.length());
                w.newLine();

                w.close();

		infoChanged = true;
        }

	/**
	* Method to validate the config information given in the
	* passed parameter.
	* @param ConfigInfo tempCfg - Confdig object with parameters
	*
	*/
        public void validateInfo(ConfigInfo tempCfg) throws Exception {
		if(!tempCfg.executionMode.toLowerCase().equals(NON_GUI_MODE)
		    && !tempCfg.executionMode.toLowerCase().equals(GUI_MODE)) {
			throw new Exception("Invalid Execution mode ("
						+ GUI_MODE + " or "
						+ NON_GUI_MODE + ")");
		}
		tempCfg.executionModeVal = tempCfg.executionMode;

		if(!tempCfg.osInUse.toLowerCase().equals(WINDOWS_OS)
		    && !tempCfg.osInUse.toLowerCase().equals(LINUX_OS)) {
			throw new Exception("Invalid osInUse - ("
						+ WINDOWS_OS + " or "
						+ LINUX_OS + ")");
		}
		tempCfg.osInUseVal = tempCfg.osInUse;

		if(tempCfg.ipVersionVal != IPv4_VERSION_VAL
		    && tempCfg.ipVersionVal != IPv6_VERSION_VAL) {
			throw new Exception("Invalid ipVersion - ("
						+ IPv4_VERSION + " or "
						+ IPv6_VERSION + ")");
		}

		///////////// temporary code
		if(tempCfg.ipVersion.toLowerCase().equals(IPv6_VERSION)) {
                	throw new Exception("IPv6 not supported at this time");
		}
		///////////// end of temprorary code


		if(tempCfg.ipVersionVal == IPv4_VERSION_VAL)
			tempCfg.ipVersionVal = IPv4_VERSION_VAL;
		else
			tempCfg.ipVersionVal = IPv6_VERSION_VAL;

		// validate ipAddress
		try {
			InetAddress adr = InetAddress.getByName(tempCfg.ipAddress);
			if(tempCfg.ipVersionVal == IPv4_VERSION_VAL) {
				if(!(adr instanceof Inet4Address))
                                	throw new Exception("must be IPv4");
			} else if(tempCfg.ipVersionVal == IPv6_VERSION_VAL) {
				if(!(adr instanceof Inet6Address))
					throw new Exception("must be IPv6");
			} else
				throw new Exception("some inconsistancy");
			tempCfg.ipAddress = adr.getHostAddress();
			tempCfg.ipAddressVal = adr;
		} catch(Exception e) {
			throw new Exception("Invalid ipAddress - " + e);
		}

		// validate ifaceName
		if(tempCfg.ifaceName.trim().length() == 0)
			throw new Exception("Invalid ifaceName - cannot be blank");
		tempCfg.ifaceNameVal = tempCfg.ifaceName;

		// validate ipAddressGateway
		try {
			InetAddress adr = InetAddress.getByName(tempCfg.ipAddressGateway);
			if(tempCfg.ipVersionVal == IPv4_VERSION_VAL) {
				if(!(adr instanceof Inet4Address))
					throw new Exception("must be IPv4");
			} else if(tempCfg.ipVersionVal == IPv6_VERSION_VAL) {
				if(!(adr instanceof Inet6Address))
					throw new Exception("must be IPv6");
			} else
				throw new Exception("some inconsistancy");
			tempCfg.ipAddressGateway = adr.getHostAddress();
			tempCfg.ipAddressGatewayVal = adr;
		} catch(Exception e) {
			throw new Exception("Invalid ipAddressGateway - " + e);
		}

		// validate loIfaceName
		if(tempCfg.loIfaceName.trim().length() == 0)
			throw new Exception("Invalid loIfaceName - cannot be blank");
		tempCfg.loIfaceNameVal = tempCfg.loIfaceName;

		// validate loggingStatus
		if(!(tempCfg.loggingStatus.toLowerCase().equals(YES)
			|| tempCfg.loggingStatus.toLowerCase().equals(NO)))
			throw new Exception("Invalid LoggingStatus - Yes or No");
		if(tempCfg.loggingStatus.toLowerCase().equals(YES))
			tempCfg.loggingStatusVal = true;
		else
			tempCfg.loggingStatusVal = false;

		// validate loggingLevel
		if(tempCfg.loggingStatus.toLowerCase().equals(YES)) {
			try {
				int num = Integer.parseInt(tempCfg.loggingLevel);
				if(num < Logging.MIN_LOGGING || num > Logging.MAX_LOGGING)
					throw new Exception();
				tempCfg.loggingLevelVal = num;
			} catch(Exception e) {
				throw new Exception("Invalid LoggingLevel - (between " + Logging.MIN_LOGGING
								+ " and " + Logging.MAX_LOGGING + ")");
			}
		}

		// validate logFile
		if(tempCfg.loggingStatus.toLowerCase().equals(YES)) {
			try {
				if(tempCfg.logFile.trim().length() == 0)
					throw new Exception();
				tempCfg.logFileVal = tempCfg.logFile.trim();
			} catch(Exception e) {
				throw new Exception("Invalid LogFile " + tempCfg.logFile);
			}
		}

		// validate pathToSystemCmds
		File dir = new File(tempCfg.pathToSystemCmds);
		if(!dir.isDirectory())
			throw new Exception("Invalid PathToSystemCmds");
		tempCfg.pathToSystemCmdsVal = tempCfg.pathToSystemCmds;

		// validate onlyDestination
		if(!(tempCfg.onlyDestination.toLowerCase().equals(YES)
						|| tempCfg.onlyDestination.toLowerCase().equals(NO)))
			throw new Exception("Invalid OnlyDestination - Yes or No");
		if(tempCfg.onlyDestination.toLowerCase().equals(YES))
			tempCfg.onlyDestinationVal = true;
		else
			tempCfg.onlyDestinationVal = false;

		// validate gratuitousRREP
		if(!(tempCfg.gratuitousRREP.toLowerCase().equals(YES)
						|| tempCfg.gratuitousRREP.toLowerCase().equals(NO)))
			throw new Exception("Invalid GratuitousRREP - Yes or No");
		if(tempCfg.gratuitousRREP.toLowerCase().equals(YES))
			tempCfg.gratuitousRREPVal = true;
		else
			tempCfg.gratuitousRREPVal = false;

		// validate RREPAckRequired
		if(!(tempCfg.RREPAckRequired.toLowerCase().equals(YES)
						|| tempCfg.RREPAckRequired.toLowerCase().equals(NO)))
			throw new Exception("Invalid RREPAckRequired - Yes or No");
		if(tempCfg.RREPAckRequired.toLowerCase().equals(YES))
			tempCfg.RREPAckRequiredVal = true;
		else
			tempCfg.RREPAckRequiredVal = false;

		// validate ipAddressMulticast
		try {
			InetAddress adr = InetAddress.getByName(tempCfg.ipAddressMulticast);
			if(tempCfg.ipVersionVal == IPv4_VERSION_VAL) {
				if(!(adr instanceof Inet4Address))
					throw new Exception("must be IPv4");
			} else if(tempCfg.ipVersionVal == IPv6_VERSION_VAL) {
				if(!(adr instanceof Inet6Address))
					throw new Exception("must be IPv6");
			} else
				throw new Exception("some inconsistancy");
			tempCfg.ipAddressMulticast = adr.getHostAddress();
			tempCfg.ipAddressMulticastVal = adr;
		} catch(Exception e) {
			throw new Exception("Invalid ipAddressMulticast - " + e);
		}

		// validate RERRSendingMode
		if(tempCfg.RERRSendingMode.toLowerCase().equals(RERR_UNICAST)) {
			tempCfg.RERRSendingModeVal = RERR_UNICAST_VAL;
		} else if(tempCfg.RERRSendingMode.toLowerCase().equals(RERR_MULTICAST)) {
			tempCfg.RERRSendingModeVal = RERR_MULTICAST_VAL;
		} else {
			throw new Exception("Invalid RERRSendingMode - should be "
						+ RERR_UNICAST + " or "
						+ RERR_MULTICAST + " ");
		}

		// validate deletePeriodMode
		try {
			if(tempCfg.deletePeriodMode.toLowerCase().equals(DELETE_MODE_HELLO))
				tempCfg.deletePeriodModeVal = DELETE_MODE_HELLO_VAL;
			else if(tempCfg.deletePeriodMode.toLowerCase().equals(DELETE_MODE_LINKLAYER))
				tempCfg.deletePeriodModeVal = DELETE_MODE_LINKLAYER_VAL;
			else
				throw new Exception();

		} catch(Exception e) {
				throw new Exception("Invalid DeletePeriodMode ("
							+ DELETE_MODE_HELLO + " or "
							+ DELETE_MODE_LINKLAYER + ")");
		}

		// validate routeDiscoveryMode
		try {
			if(tempCfg.routeDiscoveryMode.toLowerCase().equals(ROUTE_DISCOVERY_ERS))
				tempCfg.routeDiscoveryModeVal = ROUTE_DISCOVERY_ERS_VAL;
			else if(tempCfg.routeDiscoveryMode.toLowerCase().equals(ROUTE_DISCOVERY_NON_ERS))
				tempCfg.routeDiscoveryModeVal = ROUTE_DISCOVERY_NON_ERS_VAL;
			else
				throw new Exception();

		} catch(Exception e) {
				throw new Exception("Invalid RouteDiscoveryMode ("
							+ ROUTE_DISCOVERY_ERS + " or "
							+ ROUTE_DISCOVERY_NON_ERS + ")");
		}

		// validate packetBuffering
		try {
			if(tempCfg.packetBuffering.toLowerCase().equals(YES))
				tempCfg.packetBufferingVal = true;
			else if(tempCfg.packetBuffering.toLowerCase().equals(NO))
				tempCfg.packetBufferingVal = false;
			else
				throw new Exception();

		} catch(Exception e) {
				throw new Exception("Invalid PacketBuffering ("
							+ YES + " or "
							+ NO + ")");
		}

		// validate activeRouteTimeout
		try {
			int num = Integer.parseInt(tempCfg.activeRouteTimeout);
			if(num <= 0)
				throw new Exception();
			tempCfg.activeRouteTimeoutVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid activeRouteTimeout");
		}

		// validate allowedHelloLoss
		try {
			int num = Integer.parseInt(tempCfg.allowedHelloLoss);
			if(num <= 0)
				throw new Exception();
			tempCfg.allowedHelloLossVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid AllowedHelloLoss");
		}

		// validate helloInterval
		try {
			int num = Integer.parseInt(tempCfg.helloInterval);
			if(num <= 0)
				throw new Exception();
			tempCfg.helloIntervalVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid HelloInterval");
		}

		// validate localAddTTL
		try {
			int num = Integer.parseInt(tempCfg.localAddTTL);
			if(num <= 0)
				throw new Exception();
			tempCfg.localAddTTLVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid LocalAddTTL");
		}

		// validate netDiameter
		try {
			int num = Integer.parseInt(tempCfg.netDiameter);
			if(num <= 0)
				throw new Exception();
			tempCfg.netDiameterVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid NetDiameter");
		}

		// validate nodeTraversalTime
		try {
			int num = Integer.parseInt(tempCfg.nodeTraversalTime);
			if(num <= 0)
				throw new Exception();
			tempCfg.nodeTraversalTimeVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid nodeTraversalTime");
		}

		// validate RERRRatelimit
		try {
			int num = Integer.parseInt(tempCfg.RERRRatelimit);
			if(num <= 0)
				throw new Exception();
			tempCfg.RERRRatelimitVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid RERRRatelimit");
		}

		// validate RREQRetries
		try {
			int num = Integer.parseInt(tempCfg.RREQRetries);
			if(num <= 0)
				throw new Exception();
			tempCfg.RREQRetriesVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid RREQRetries");
		}

		// validate RREQRateLimit
		try {
			int num = Integer.parseInt(tempCfg.RREQRateLimit);
			if(num <= 0)
				throw new Exception();
			tempCfg.RREQRateLimitVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid RREQRateLimit");
		}

		// validate timeoutBuffer
		try {
			int num = Integer.parseInt(tempCfg.timeoutBuffer);
			if(num <= 0)
				throw new Exception();
			tempCfg.timeoutBufferVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid TimeoutBuffer");
		}

		// validate TTLStart
		try {
			int num = Integer.parseInt(tempCfg.TTLStart);
			if(num <= 0)
				throw new Exception();
			tempCfg.TTLStartVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid TTLStart");
		}

		// validate TTLIncrement
		try {
			int num = Integer.parseInt(tempCfg.TTLIncrement);
			if(num <= 0)
				throw new Exception();
			tempCfg.TTLIncrementVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid TTLIncrement");
		}

		// validate TTLThreshold
		try {
			int num = Integer.parseInt(tempCfg.TTLThreshold);
			if(num <= 0)
				throw new Exception();
			tempCfg.TTLThresholdVal = num;
		} catch(Exception e) {
			throw new Exception("Invalid TTLThreshold");
		}

	 	netTraversalTimeVal = 2 * nodeTraversalTimeVal * netDiameterVal;
		blacklistTimeoutVal = RREQRetriesVal * netTraversalTimeVal;
	 	maxRepairTTLVal = (int) (0.3 * netDiameterVal);
	 	myRouteTimeoutVal = 2 * activeRouteTimeoutVal;
	 	nextHopWaitVal = nodeTraversalTimeVal + 10;
	 	pathDiscoveryTimeVal = 2 * netTraversalTimeVal;
		if(deletePeriodModeVal == 1) {
			deletePeriodVal = allowedHelloLossVal * helloIntervalVal;
		} else {
			deletePeriodVal = activeRouteTimeoutVal;
		}

        }

	/**
	* Method to get values from a given config object
	* @param ConfigInfo tempCfg - Config object, from which to
	*				get values
	*/
        public void setValuesUsing(ConfigInfo tempCfg) {

	 	executionMode = tempCfg.executionMode;
	 	osInUse = tempCfg.osInUse;
	 	ipVersion = tempCfg.ipVersion;
	 	ipAddress = tempCfg.ipAddress;
	 	ifaceName = tempCfg.ifaceName;
	 	ipAddressGateway = tempCfg.ipAddressGateway;
	 	loIfaceName = tempCfg.loIfaceName;
	 	loggingStatus = tempCfg.loggingStatus;
	 	loggingLevel = tempCfg.loggingLevel;
	 	logFile = tempCfg.logFile;
	 	pathToSystemCmds = tempCfg.pathToSystemCmds;
	 	onlyDestination = tempCfg.onlyDestination;
	 	gratuitousRREP = tempCfg.gratuitousRREP;
		RREPAckRequired = tempCfg.RREPAckRequired;
	 	ipAddressMulticast = tempCfg.ipAddressMulticast;
		RERRSendingMode = tempCfg.RERRSendingMode;
		deletePeriodMode = tempCfg.deletePeriodMode;
		routeDiscoveryMode = tempCfg.routeDiscoveryMode;
		packetBuffering = tempCfg.packetBuffering;

	 	activeRouteTimeout = tempCfg.activeRouteTimeout;
	 	allowedHelloLoss = tempCfg.allowedHelloLoss;
   	 	helloInterval = tempCfg.helloInterval;
	 	localAddTTL = tempCfg.localAddTTL;
	 	netDiameter = tempCfg.netDiameter;
	 	nodeTraversalTime = tempCfg.nodeTraversalTime;
	 	RERRRatelimit = tempCfg.RERRRatelimit;
	 	RREQRetries = tempCfg.RREQRetries;
	 	RREQRateLimit = tempCfg.RREQRateLimit;
	 	timeoutBuffer = tempCfg.timeoutBuffer;
	 	TTLStart = tempCfg.TTLStart;
	 	TTLIncrement = tempCfg.TTLIncrement;
	 	TTLThreshold = tempCfg.TTLThreshold;

		// actual data values
	 	executionModeVal = tempCfg.executionModeVal;
	 	osInUseVal = tempCfg.osInUseVal;
	 	ipVersionVal = tempCfg.ipVersionVal;
	 	ipAddressVal = tempCfg.ipAddressVal;
	 	ifaceNameVal = tempCfg.ifaceNameVal;
	 	ipAddressGatewayVal = tempCfg.ipAddressGatewayVal;
	 	loIfaceNameVal = tempCfg.loIfaceNameVal;
	 	loggingStatusVal = tempCfg.loggingStatusVal;
	 	loggingLevelVal = tempCfg.loggingLevelVal;
	 	logFileVal = tempCfg.logFileVal;
	 	pathToSystemCmdsVal = tempCfg.pathToSystemCmdsVal;
	 	onlyDestinationVal = tempCfg.onlyDestinationVal;
	 	gratuitousRREPVal = tempCfg.gratuitousRREPVal;
		RREPAckRequiredVal = tempCfg.RREPAckRequiredVal;
	 	ipAddressMulticastVal = tempCfg.ipAddressMulticastVal;
		RERRSendingModeVal = tempCfg.RERRSendingModeVal;
		deletePeriodModeVal = tempCfg.deletePeriodModeVal;
		routeDiscoveryModeVal = tempCfg.routeDiscoveryModeVal;
		packetBufferingVal = tempCfg.packetBufferingVal;

	 	activeRouteTimeoutVal = tempCfg.activeRouteTimeoutVal;
	 	allowedHelloLossVal = tempCfg.allowedHelloLossVal;
   	 	helloIntervalVal = tempCfg.helloIntervalVal;
	 	localAddTTLVal = tempCfg.localAddTTLVal;
	 	netDiameterVal = tempCfg.netDiameterVal;
	 	nodeTraversalTimeVal = tempCfg.nodeTraversalTimeVal;
	 	RERRRatelimitVal = tempCfg.RERRRatelimitVal;
	 	RREQRetriesVal = tempCfg.RREQRetriesVal;
	 	RREQRateLimitVal = tempCfg.RREQRateLimitVal;
	 	timeoutBufferVal = tempCfg.timeoutBufferVal;
	 	TTLStartVal = tempCfg.TTLStartVal;
	 	TTLIncrementVal = tempCfg.TTLIncrementVal;
	 	TTLThresholdVal = tempCfg.TTLThresholdVal;

	 	netTraversalTimeVal = 2 * nodeTraversalTimeVal * netDiameterVal;
		blacklistTimeoutVal = RREQRetriesVal * netTraversalTimeVal;
	 	maxRepairTTLVal = (int) (0.3 * netDiameterVal);
	 	myRouteTimeoutVal = 2 * activeRouteTimeoutVal;
	 	nextHopWaitVal = nodeTraversalTimeVal + 10;
	 	pathDiscoveryTimeVal = 2 * netTraversalTimeVal;
		if(deletePeriodModeVal == 1) {
			deletePeriodVal = allowedHelloLossVal * helloIntervalVal;
		} else {
			deletePeriodVal = activeRouteTimeoutVal;
		}

		infoChanged = true;
        }



	public boolean shutdown() {
		// TODO Auto-generated method stub
		return false;
	}



	public boolean startup(Object data) {
		// TODO Auto-generated method stub
		return false;
	}
}
