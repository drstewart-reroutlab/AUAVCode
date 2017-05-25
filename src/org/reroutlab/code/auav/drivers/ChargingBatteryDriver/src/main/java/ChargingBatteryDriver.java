package org.reroutlab.code.auav.drivers;

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.System.*;
//import org.reroutlab.code.auav.interfaces.*;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * org.reroutlab.code.auav.drivers.ChargingBatteryDriver is an AUAV service.
 * 
 * 
 *
 * @author  Gavin Chau Nguyen
 * @version 0.01
 * @since   2017-05-24 
 */

// ChargingBatteryDriver contains the code for behavior specified in the AUAVDrivers, plus this thread is accessible globally
public class ChargingBatteryDriver implements Runnable,org.reroutlab.code.auav.drivers.AuavDrivers {

// Initializing the variables, used only in this module, no public access (outside of the method)
		private int BATTERY_PORT = 0;
		private int BATTERY_STATUS = 0; // Percentage of battery remaining
		private int TIME_REMAINING = 0; // Assuming the DJI battery has duration of 25 minutes, which means 1500 seconds. This will estimate the remaining time of the battery, so the user can control when to charge.
		private int CHARGING_TIME = 0; // Assuming the DJI battery takes 1.5 hours to charge from 0% to 100%, which means 5400 seconds. This will estimate the time to fully charge the battery.

// Main program: creating the new thread & new Charging Battery Driver
		public static void main(String[] args) {
				ChargingBatteryDriver cb = new ChargingBatteryDriver();
				//System.out.println("LocalPort: " + cb.getLocalPort() );
				Thread cbT = new Thread(cb);
				cbT.start();
				
		}

		
    private static Logger cbLogger =
				Logger.getLogger(ChargingBatteryDriver.class.getName());
		public void setLogLevel(Level l) {
				cbLogger.setLevel(l);
		}		
		
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
		}
		
		private String usageInfo=";batteryPercentage ##; getRemainingDuration; getChargingTime";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		public ChargingBatteryDriver() {
				cbLogger.log(Level.FINEST, "In Constructor");
				try {
						serverSocket = new ServerSocket(BATTERY_PORT);
						System.out.println(getLocalPort());
				} catch (Exception e){
						cbLogger.log(Level.WARNING, "Unable to create server socket");
				}
				
		}

		public void run() {
				BufferedReader bufferedReader;
				PrintWriter printWriter;
				while (true) {
						Socket clientSocket=null;
						try {
								clientSocket = serverSocket.accept();
								bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
								printWriter = new PrintWriter(clientSocket.getOutputStream(),true);

								String inputLine = bufferedReader.readLine();
								if (inputLine != null) {
										inputLine = inputLine.trim();
										String outputLine = processCommands(inputLine);
										printWriter.println(outputLine);
								}
								clientSocket.close();
						}
						catch (Exception e) {
								cbLogger.log(Level.WARNING, "Problem: " + e.toString() );
						}
				}
		}
		

		private String processCommands(String inputLine) {
				// Split on \n then on ' '
				String outLine = "";
				String[] args = inputLine.split(" ");
						if (args[0].equals("help")) {
								return(getUsageInfo());
						}
						else if (args[0].equals("batteryPercentage")) {
								BATTERY_STATUS = Integer.parseInt(args[1]);
								if(BATTERY_STATUS <= 100) {
									return ("The percentage of the battery is: " + BATTERY_STATUS + " %.");
								} else {
									return("Error: ChargingBatteryDriver invalid command\n");
								}
						}
						else if (args[0].equals("getRemainingDuration")) {
								TIME_REMAINING = BATTERY_STATUS*15;
								return ("The remaining duration of the battery is: " + TIME_REMAINING/60 + " minutes  and " + 										TIME_REMAINING%60 + " seconds.");
						}
						else if (args[0].equals("getChargingTime")) {
								CHARGING_TIME = (100-BATTERY_STATUS)*54;
								return ("The remaining charging time of the battery is: " + CHARGING_TIME/60 + " minutes  and " + 										CHARGING_TIME%60 + " seconds.");
						}
						else {
								return("Error: ChargingBatteryDriver invalid command\n");
						}
		}

}

