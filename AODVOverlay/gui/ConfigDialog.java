/*

AODV Overlay v0.5.3 Copyright 2007-2010  Lancaster University

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

package gui;

import interfaces.IConfigInfo.IConfigInfo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import OpenCOM.IOpenCOM;
import OpenCOM.IUnknown;
import aodvstate.ConfigInfo;

/**
* Class to display the configuration user interface and update
* the changes to the configuration.
*
* @author : Rajiv Ramdhany
* @date : 28-jul-2007
* @email : r.ramdhany@lancaster.ac.uk
*
*/
public class ConfigDialog extends JDialog {
        ConfigInfo cfgInfo, tempCfgInfo;
        JPanel pnlCfgPanel, pnlBtnPanel;
        JButton btnUpdate, btnCancel;

	public static final int DIALOG_WIDTH = 500;
	public static final int DIALOG_HEIGHT = 280;


        /**
        * Constructs a configuration information display and shows it.
        * @param ConfigInfo cfg - the configuration object
        * @param GUI gui - main user interface
        */
        public ConfigDialog(ConfigInfo cfg, JFrame gui, IUnknown pIOCM) {
                super(gui, "J-Adhoc Configuration", true);

                cfgInfo = cfg;

                try {

                	tempCfgInfo = new ConfigInfo(pIOCM); // not a very legal way of instantiating a component
                	
					
				} catch (Exception e) {
					e.printStackTrace();
				}
                tempCfgInfo.setValuesUsing(cfgInfo);

                pnlCfgPanel = new JPanel();
                pnlCfgPanel.setLayout(new BorderLayout());
                pnlBtnPanel = new JPanel();

                btnUpdate = new JButton("Update");
                btnUpdate.addActionListener(
                                new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                                updateConfigInfo();
                                        }
                                });

                btnCancel = new JButton("Cancel");
                btnCancel.addActionListener(
                                new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                                cancelConfigUpdate();
                                        }
                                });

                pnlBtnPanel.add(btnUpdate);
                pnlBtnPanel.add(btnCancel);

                pnlCfgPanel.add(new JScrollPane(new JTable(new CfgTblModel())));

                addWindowListener(
                                new WindowAdapter() {
                                        public void windowClosing(WindowEvent e) {
                                                cancelConfigUpdate();
                                        }
                                });

                getContentPane().setLayout(new BorderLayout());
                getContentPane().add("Center", pnlCfgPanel);
                getContentPane().add("South", pnlBtnPanel);
                getContentPane().setBackground(Color.gray);
                setResizable(false);
                int x = (int) (gui.getLocation().getX() + ((gui.getSize().getWidth() - DIALOG_WIDTH) / 2));
                int y = (int) (gui.getLocation().getY() + ((gui.getSize().getHeight() - DIALOG_HEIGHT) / 2));
                setLocation(x, y);
                setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
                setVisible(true);
        }

        /**
        * Inner class that extends the default table model to handle
        * the JTable associated with displaying and updating configuration
        * information
        */
        class CfgTblModel extends DefaultTableModel {

                /**
                * Method to return the row count of the config info
                * table
                * @return int - row count
                */
                public int getRowCount() {
                        return 32;
                }

                /**
                * Method to return the column count of the config info
                * table
                * @return int - column count
                */
                public int getColumnCount() {
                        return 2;
                }

                /**
                * Method to return the value related to given row and
                * column
                * @param int row - the row of the value
                * @param int column - the column of the value
                * @return Object - the data value (string)
                */
                public Object getValueAt(int row, int column) {
                        if(row == 0 && column == 0)
                                return tempCfgInfo.executionModeStr;
                        else if(row == 0 && column == 1)
                                return tempCfgInfo.executionMode;
                        else if(row == 1 && column == 0)
                                return tempCfgInfo.osInUseStr;
                        else if(row == 1 && column == 1)
                                return tempCfgInfo.osInUse;
                        else if(row == 2 && column == 0)
                                return tempCfgInfo.ipVersionStr;
                        else if(row == 2 && column == 1)
                                return tempCfgInfo.ipVersion;
                        else if(row == 3 && column == 0)
                                return tempCfgInfo.ipAddressStr;
                        else if(row == 3 && column == 1)
                                return tempCfgInfo.ipAddress;
                        else if(row == 4 && column == 0)
                                return tempCfgInfo.ifaceNameStr;
                        else if(row == 4 && column == 1)
                                return tempCfgInfo.ifaceName;
                        else if(row == 5 && column == 0)
                                return tempCfgInfo.ipAddressGatewayStr;
                        else if(row == 5 && column == 1)
                                return tempCfgInfo.ipAddressGateway;
                        else if(row == 6 && column == 0)
                                return tempCfgInfo.loIfaceNameStr;
                        else if(row == 6 && column == 1)
                                return tempCfgInfo.loIfaceName;
			else if(row == 7 && column == 0)
                                return tempCfgInfo.loggingStatusStr;
                        else if(row == 7 && column == 1)
                                return tempCfgInfo.loggingStatus;
			else if(row == 8 && column == 0)
                                return tempCfgInfo.loggingLevelStr;
                        else if(row == 8 && column == 1)
                                return tempCfgInfo.loggingLevel;
                        else if(row == 9 && column == 0)
                                return tempCfgInfo.logFileStr;
                        else if(row == 9 && column == 1)
                                return tempCfgInfo.logFile;
                        else if(row == 10 && column == 0)
                                return tempCfgInfo.pathToSystemCmdsStr;
                        else if(row == 10 && column == 1)
                                return tempCfgInfo.pathToSystemCmds;
			else if(row == 11 && column == 0)
                                return tempCfgInfo.onlyDestinationStr;
                        else if(row == 11 && column == 1)
                                return tempCfgInfo.onlyDestination;
                        else if(row == 12 && column == 0)
                                return tempCfgInfo.gratuitousRREPStr;
                        else if(row == 12 && column == 1)
                                return tempCfgInfo.gratuitousRREP;
                        else if(row == 13 && column == 0)
                                return tempCfgInfo.RREPAckRequiredStr;
                        else if(row == 13 && column == 1)
                                return tempCfgInfo.RREPAckRequired;
                        else if(row == 14 && column == 0)
                                return tempCfgInfo.ipAddressMulticastStr;
                        else if(row == 14 && column == 1)
                                return tempCfgInfo.ipAddressMulticast;
                        else if(row == 15 && column == 0)
                                return tempCfgInfo.RERRSendingModeStr;
                        else if(row == 15 && column == 1)
                                return tempCfgInfo.RERRSendingMode;
                        else if(row == 16 && column == 0)
                                return tempCfgInfo.deletePeriodModeStr;
                        else if(row == 16 && column == 1)
                                return tempCfgInfo.deletePeriodMode;
                        else if(row == 17 && column == 0)
                                return tempCfgInfo.routeDiscoveryModeStr;
                        else if(row == 17 && column == 1)
                                return tempCfgInfo.routeDiscoveryMode;
                        else if(row == 18 && column == 0)
                                return tempCfgInfo.packetBufferingStr;
                        else if(row == 18 && column == 1)
                                return tempCfgInfo.packetBuffering;


			else if(row == 19 && column == 0)
                                return tempCfgInfo.activeRouteTimeoutStr;
                        else if(row == 19 && column == 1)
                                return tempCfgInfo.activeRouteTimeout;
                        else if(row == 20 && column == 0)
                                return tempCfgInfo.allowedHelloLossStr;
                        else if(row == 20 && column == 1)
                                return tempCfgInfo.allowedHelloLoss;
                        else if(row == 21 && column == 0)
                                return tempCfgInfo.helloIntervalStr;
                        else if(row == 21 && column == 1)
                                return tempCfgInfo.helloInterval;
                        else if(row == 22 && column == 0)
                                return tempCfgInfo.localAddTTLStr;
                        else if(row == 22 && column == 1)
                                return tempCfgInfo.localAddTTL;
                        else if(row == 23 && column == 0)
                                return tempCfgInfo.netDiameterStr;
                        else if(row == 23 && column == 1)
                                return tempCfgInfo.netDiameter;
                        else if(row == 24 && column == 0)
                                return tempCfgInfo.nodeTraversalTimeStr;
                        else if(row == 24 && column == 1)
                                return tempCfgInfo.nodeTraversalTime;
			else if(row == 25 && column == 0)
                                return tempCfgInfo.RERRRatelimitStr;
                        else if(row == 25 && column == 1)
                                return tempCfgInfo.RERRRatelimit;
			else if(row == 26 && column == 0)
                                return tempCfgInfo.RREQRetriesStr;
                        else if(row == 26 && column == 1)
                                return tempCfgInfo.RREQRetries;
                        else if(row == 27 && column == 0)
                                return tempCfgInfo.RREQRateLimitStr;
                        else if(row == 27 && column == 1)
                                return tempCfgInfo.RREQRateLimit;
                        else if(row == 28 && column == 0)
                                return tempCfgInfo.timeoutBufferStr;
                        else if(row == 28 && column == 1)
                                return tempCfgInfo.timeoutBuffer;
                        else if(row == 29 && column == 0)
                                return tempCfgInfo.TTLStartStr;
                        else if(row == 29 && column == 1)
                                return tempCfgInfo.TTLStart;
                        else if(row == 30 && column == 0)
                                return tempCfgInfo.TTLIncrementStr;
                        else if(row == 30 && column == 1)
                                return tempCfgInfo.TTLIncrement;
                        else if(row == 31 && column == 0)
                                return tempCfgInfo.TTLThresholdStr;
                        else if(row == 31 && column == 1)
                                return tempCfgInfo.TTLThreshold;
			else
                                return " ";
                }

                /**
                * Method to get the column names. Either 'Parameter'
                * or 'Value'
                * @param int column - the column number
                * @return String - the column name
                */
                public String getColumnName(int column) {
                        if(column == 0)
                                return "Parameter";
                        else
                                return "Value";
                }

                /**
                * Method to update the parameter info object, when the
                * user makes some changes.
                * @param Object val - the data object changed
                * @param int row - the row of the data
                * @param int column - the column of the data
                */
                public void setValueAt(Object val, int row, int column) {
                        if(row == 0 && column == 1)
                                tempCfgInfo.executionMode = new String((String) val);
                        else if(row == 1 && column == 1)
                                tempCfgInfo.osInUse = new String((String) val);
                        else if(row == 2 && column == 1)
                                tempCfgInfo.ipVersion = new String((String) val);
                        else if(row == 3 && column == 1)
                                tempCfgInfo.ipAddress = new String((String) val);
                        else if(row == 4 && column == 1)
                                tempCfgInfo.ifaceName = new String((String) val);
                        else if(row == 5 && column == 1)
                                tempCfgInfo.ipAddressGateway = new String((String) val);
                        else if(row == 6 && column == 1)
                                tempCfgInfo.loIfaceName = new String((String) val);
			else if(row == 7 && column == 1)
                                tempCfgInfo.loggingStatus = new String((String) val);
                        else if(row == 8 && column == 1)
                                tempCfgInfo.loggingLevel = new String((String) val);
			else if(row == 9 && column == 1)
                                tempCfgInfo.logFile = new String((String) val);
                        else if(row == 10 && column == 1)
                                tempCfgInfo.pathToSystemCmds = new String((String) val);
                        else if(row == 11 && column == 1)
                                tempCfgInfo.onlyDestination = new String((String) val);
                        else if(row == 12 && column == 1)
                                tempCfgInfo.gratuitousRREP = new String((String) val);
			else if(row == 13 && column == 1)
				tempCfgInfo.RREPAckRequired = new String((String) val);
			else if(row == 14 && column == 1)
				tempCfgInfo.ipAddressMulticast = new String((String) val);
			else if(row == 15 && column == 1)
				tempCfgInfo.RERRSendingMode = new String((String) val);
			else if(row == 16 && column == 1)
                                tempCfgInfo.deletePeriodMode = new String((String) val);
                        else if(row == 17 && column == 1)
				tempCfgInfo.routeDiscoveryMode = new String((String) val);
                        else if(row == 18 && column == 1)
				tempCfgInfo.packetBuffering = new String((String) val);


			else if(row == 19 && column == 1)
                                tempCfgInfo.activeRouteTimeout = new String((String) val);
                        else if(row == 20 && column == 1)
                                tempCfgInfo.allowedHelloLoss = new String((String) val);
                        else if(row == 21 && column == 1)
                                tempCfgInfo.helloInterval = new String((String) val);
                        else if(row == 22 && column == 1)
                                tempCfgInfo.localAddTTL = new String((String) val);
                        else if(row == 23 && column == 1)
                                tempCfgInfo.netDiameter = new String((String) val);
                        else if(row == 24 && column == 1)
                                tempCfgInfo.nodeTraversalTime = new String((String) val);
			else if(row == 25 && column == 1)
                                tempCfgInfo.RERRRatelimit = new String((String) val);
                        else if(row == 26 && column == 1)
                                tempCfgInfo.RREQRetries = new String((String) val);
                        else if(row == 27 && column == 1)
                                tempCfgInfo.RREQRateLimit = new String((String) val);
                        else if(row == 28 && column == 1)
                                tempCfgInfo.timeoutBuffer = new String((String) val);
                        else if(row == 29 && column == 1)
                                tempCfgInfo.TTLStart = new String((String) val);
                        else if(row == 30 && column == 1)
                                tempCfgInfo.TTLIncrement = new String((String) val);
                        else if(row == 31 && column == 1)
                                tempCfgInfo.TTLThreshold = new String((String) val);
		}

                /**
                * Method to return whether a certain column is editable or
                * not. In this case only the Value column (column 2) is
                * editable
                * @param int row - the data row
                * @param int column - the data column
                * @return boolean - whether editable or not
                */
                public boolean isCellEditable(int row, int column) {
                        if(column == 0)
                                return false;
                        else
                                return true;
                }
        }

        /**
        * Method to validate and update the changes to the configuration.
        * This method is associated with the Update button
        */
        void updateConfigInfo() {
                try {
                        cfgInfo.validateInfo(tempCfgInfo);
                        cfgInfo.setValuesUsing(tempCfgInfo);
                        cfgInfo.updateInfo();
                        setVisible(false);
                } catch(Exception e) {
                        JOptionPane.showMessageDialog(this, e.toString(),
                                        "J-Adhoc - Error Message", JOptionPane.ERROR_MESSAGE);
                }
        }

        /**
        * Method to leave the configuration user interface without making
        * any changes to the config info. This is associated with the
        * Cancel button.
        */
        void cancelConfigUpdate() {
                setVisible(false);
        }
}
