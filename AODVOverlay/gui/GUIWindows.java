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

import interfaces.IState.IAodvState;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import OpenCOM.IOpenCOM;
import OpenCOM.IUnknown;
import aodvstate.ConfigInfo;



/**
 * Class that manages the main user interface of the Protocol
 * Handler in a Windows environment.
 *
 * @author : Rajiv Ramdhany
 * @date : 11-feb-2004
 * @email : r.ramdhany@lancaster.ac.uk
 *
 */
public class GUIWindows extends JFrame implements GUIInterface {

	ConfigInfo cfgInfo;
	GUIComponent pGUIComp;	// parent GUI Component ref
	IUnknown runtime;				//OpenCOM runtime reference



	// graphical components
	TblModel mdlTable;
	JTable tblTable;
	JPanel pnlTable;
	JPanel pnlInfo, pnlButton, pnlInfoSubA, pnlInfoSubB;
	JButton btnStart, btnStop, btnConfig, btnQuit, btnInfo;
	JLabel lblOSInUse, lblIPVersion, lblInterfaceName, lblIPAddr, lblLastSeqNum, lblLastRREQID;

	ConfigDialog cfgDialog;

	public static final int FRAME_POSITION_TOP = 100;
	public static final int FRAME_POSITION_LEFT = 100;
	public static final int FRAME_WIDTH = 1000;
	public static final int FRAME_HEIGHT = 300;


	/**
	 * Contructor creates and displays the main screen. If the
	 * configuration info read status indicates that the config
	 * file was not present, then the configuration user interface
	 * is also called.
	 * @param ConfigInfo cfg - config info object
	 * @int readStat - status to indicate whether config file
	 *                 is available or not
	 */
	public GUIWindows(ConfigInfo cfg, IUnknown pIOCM, GUIComponent pGUI) {
		cfgInfo = cfg;
		pGUIComp = pGUI;
		runtime = pIOCM;


		// build the graphical components

		// build the table panel
		pnlTable = new JPanel();
		tblTable = new JTable();
		mdlTable = new TblModel();
		tblTable.setModel(mdlTable);
		pnlTable.setBackground(Color.gray);
		pnlTable.setLayout(new BorderLayout());
		pnlTable.add(new JScrollPane(tblTable));

		// build the information panel
		pnlInfo = new JPanel();
		pnlInfoSubA = new JPanel();
		pnlInfoSubB = new JPanel();
		pnlInfo.setLayout(new BorderLayout());

		lblOSInUse = new JLabel();
		lblIPVersion = new JLabel();
		lblIPAddr = new JLabel();
		lblInterfaceName = new JLabel();
		lblLastSeqNum = new JLabel();
		lblLastRREQID = new JLabel();

		setPrimaryHeaderInfo();
		setSecondaryHeaderInfo();

		pnlInfoSubA.add(lblOSInUse);
		pnlInfoSubA.add(new JLabel(" - "));
		pnlInfoSubA.add(lblIPVersion);
		pnlInfoSubA.add(new JLabel(" - "));
		pnlInfoSubA.add(lblIPAddr);
		pnlInfoSubA.add(new JLabel(" - "));
		pnlInfoSubA.add(lblInterfaceName);

		pnlInfoSubB.add(lblLastSeqNum);
		pnlInfoSubB.add(new JLabel(" - "));
		pnlInfoSubB.add(lblLastRREQID);

		pnlInfo.add("North", pnlInfoSubA);
		pnlInfo.add("South", pnlInfoSubB);


		// build the button panel
		btnStart = new JButton("Start");
		btnStart.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						startApplication();
					}
				});

		btnStop = new JButton("Stop");
		btnStop.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						stopApplication();
					}
				});

		btnStop.setEnabled(false);
		btnConfig = new JButton("Configure");
		btnConfig.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						configApplication();
					}
				});

		btnQuit = new JButton("Quit");
		btnQuit.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitApplication();
					}
				});

		btnInfo = new JButton("Information");
		btnInfo.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispInformation();
					}
				});

		pnlButton = new JPanel();
		pnlButton.add(btnStart);
		pnlButton.add(btnStop);
		pnlButton.add(btnConfig);
		pnlButton.add(btnQuit);
		pnlButton.add(btnInfo);

		// set the frame components
		addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						exitApplication();
					}
				});
		getContentPane().add("North", pnlInfo);
		getContentPane().add("Center", pnlTable);
		getContentPane().add("South", pnlButton);
		getContentPane().setBackground(Color.gray);
		setTitle("OpenAODV - AODV Protocol Handler (ver 0.1)");
		setLocation(FRAME_POSITION_TOP, FRAME_POSITION_LEFT);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);

		setVisible(true);
		
		// router initially started by application creating overlay
		if (cfgInfo.RouterActive)
			setButtonsForStart();
			
		
		// show the configuration screen if it the first
		// invocation of the application
		if(cfgInfo.firstTime) {
			cfgDialog = new ConfigDialog(cfgInfo, this, pIOCM);
			if(cfgInfo.infoChanged) {
				setPrimaryHeaderInfo();
				cfgInfo.infoChanged = false;
			}
			cfgInfo.firstTime = false;
		}
		
		
		
	}

	/**
	 * Method to perform when the user requests a quit application
	 */
	void exitApplication() {
		stopApplication();
		pGUIComp.exitApplication();
	}

	/**
	 * Method to perform when the user requests a start AODV protocol
	 * handler
	 */
	void startApplication() {
		boolean success;
		try {
			// enable/disable buttons for start operation
			setButtonsForStart();
			
			success = pGUIComp.startApplication();
			
			if(!success) {
				setButtonsForStop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to perform when the user requests a stop AODV protocol
	 * handler.
	 */
	void stopApplication() {
		setButtonsForStop();
		pGUIComp.stopApplication();
	}

	/**
	 * Method to perform when the AODV protocol handler itself requests
	 * to stop the AODV protocol handler
	 */
	public void stopAppFromRouteManager() {
		setButtonsForStop();
	}

	/**
	 * Method to perform when the user requests to configure the
	 * the application. This will result in showing the configuration
	 * user interface.
	 */
	void configApplication() {
		cfgDialog = new ConfigDialog(cfgInfo, this, runtime);
		if(cfgInfo.infoChanged) {
			setPrimaryHeaderInfo();
			cfgInfo.infoChanged = false;
		}
	}

	/**
	 * Method to display information about this AODV protocol
	 * handler
	 */
	void dispInformation() {
		String info =
			"This is an AODV Overlay developed by Rajiv Ramdhany \n(r.ramdhany@comp.lancs.ac.uk), " +
			"\nComputing Department, Lancaster University \n" +
			"The original AODV implementation is by the Communications Department (ikom) of \n" +
			"the University of Bremen, Germany - 2007\n\n" +
			"This software conforms to the IETF's AODV protocol \n" +
			"as specified in RFC 3561.";

		JOptionPane.showMessageDialog(this, info,
				"AODV Overlay- Information", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Method to set the primary header information in the main user
	 * interface.
	 */
	void setPrimaryHeaderInfo() {
		lblOSInUse.setText("Operating System : " + cfgInfo.osInUseVal);
		lblIPVersion.setText("IP Version : IPv" + cfgInfo.ipVersionVal);
		lblIPAddr.setText("IP Address : " + cfgInfo.ipAddress);
		lblInterfaceName.setText("Interface Name : " + cfgInfo.ifaceNameVal);
	}

	/**
	 * Method to set the secondary header information in the main user
	 * interface.
	 */
	void setSecondaryHeaderInfo() {


		IAodvState pState = (IAodvState) ((IOpenCOM) runtime).getComponentPIUnknown("AODVState");
		lblLastSeqNum.setText("Last Seq Num : " + pState.getLastSeqNum());
		lblLastRREQID.setText("Last RREQ ID : " + pState.getLastRREQID());
	}

	/**
	 * Method to set the button status of the main user interface
	 * when the user starts AODV protocol handler
	 */
	void setButtonsForStart() {
		btnStart.setEnabled(false);
		btnConfig.setEnabled(false);
		btnStop.setEnabled(true);
	}

	/**
	 * Method to set the button status of the main user interface
	 * when the user stops AODV protocol handler
	 */
	void setButtonsForStop() {
		btnStart.setEnabled(true);
		btnConfig.setEnabled(true);
		btnStop.setEnabled(false);
	}

	/**
	 * Method to redisplay the route table on the main user
	 * interface. This is called every time a change is made
	 * to the internal routing information.
	 */
	public void redrawTable() {
		setSecondaryHeaderInfo();
		mdlTable.fireTableChanged(new TableModelEvent(mdlTable));
	}

	public void displayError(String msg) {
		JOptionPane.showMessageDialog(this, msg,
				"J-Adhoc - Error Message", JOptionPane.ERROR_MESSAGE);

	}

	/**
	 * Inner class to handle the displaying of the route table on the
	 * main user interface. All information related to the internal
	 * routing table are obtained from the GUIComponent.
	 */
	class TblModel extends DefaultTableModel {

		/**
		 * Method to return the row count of the internal route
		 * table.
		 * @return int - row count
		 */
		public int getRowCount() {
			try {
				return pGUIComp.getRouteCount();
			} catch(Exception e) {
				return 0;
			}
		}

		/**
		 * Method to return the column count of the internal route
		 * table.
		 * @return int - column count
		 */
		public int getColumnCount() {
			return pGUIComp.getFieldCount();
		}

		/**
		 * Method to return the value in a particular row/column
		 * in the internal route table.
		 * @param int row - the row number
		 * @param int column - the column number
		 * @return Object - value object containing a string
		 */
		public Object getValueAt(int row, int column) {
			return pGUIComp.getRouteValueAt(row, column);
		}

		/**
		 * Method to return the name of a given column in the internal
		 * route table.
		 * @param int column - the column number
		 * @return String - column name as string
		 */
		public String getColumnName(int column) {
			return pGUIComp.getFieldName(column);
		}

		/**
		 * Method to return whether a cell is editable or not. No
		 * cell is editable in the table displayed
		 * @param int row - the row number
		 * @param int column - the column number
		 * @return boolean - always returns false
		 */
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
