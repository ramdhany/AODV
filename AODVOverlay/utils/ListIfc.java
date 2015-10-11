package utils;

import jpcap.*;

class ListIfc {
	public static void main(String[] args) {
		try {
    			NetworkInterface[] devList=JpcapCaptor.getDeviceList();
    			

			System.out.println("This is a list of the network interfaces ");
			System.out.println("active in this computer. The first column ");
			System.out.println("is the description and the second is the ");
			System.out.println("name of the device ID. Select the correct ");
			System.out.println("entry and place the device ID in the parameter ");
			System.out.println("file.");
			System.out.println("");

			for(int i = 0; i < devList.length; i++) {
				System.out.print(devList[i].name);
				System.out.print("   ");
				System.out.print(devList[i].description);
				System.out.print("   ");
				System.out.println(devList[i].mac_address);
			}

		} catch(Exception e) {
			System.out.println("Either this program cannot be run on");
			System.out.println("this machine or the environment is not");
			System.out.println("set properly");
			System.out.println("The error was : " + e);

		}
   	}
}
