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
 * org.reroutlab.code.auav.drivers.LocationManagerDriver is an AUAV service.
 * gotoXYZ and getLocation are the only supported commands
 * 
 * 
 *
 * @author  Christopher Stewart
 * @version 0.01
 * @since   2017-05-01 
 */

// LocationManagerDriver contains the code for behavior specified in the AUAVDrivers, plus this thread is accessible globally
public class LocationManagerDriver implements Runnable,org.reroutlab.code.auav.drivers.AuavDrivers {

// Initializing the variables, used only in this module, no public access (outside of the method)
		private int LM_PORT = 0;
		private float X = 0;
		private float Y = 0;
		private float Z = 0;

// Main program: creating the new thread & new Location Manager Driver plus a thread to start the whole program
		public static void main(String[] args) {
				LocationManagerDriver lm = new LocationManagerDriver();
				//System.out.println("LocalPort: " + lm.getLocalPort() );
				Thread lmT = new Thread(lm);
				lmT.start();
				
		}

// Return the name of the entity represented by the entity represented by the class object
// Get the logger object & set the level of the logger		
    private static Logger lmLogger =
				Logger.getLogger(LocationManagerDriver.class.getName());
		public void setLogLevel(Level l) {
				lmLogger.setLevel(l);
		}		

// Declare the server socket and check the existence of the local port. If non-existence, returning -1, else returning the local port of server sockets.	
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
		}

// Instructing the users the necessary commands to get the Usage Info.		
		private String usageInfo=";gotoXYZ X=## Y=## Z=##; getLocation; ";
		public String getUsageInfo() {
				return usageInfo;
		}

		
// The HashMap is set to control the drivers and the associated information with that driver. If no existence HashMap, it will be created;
		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

// Try & catch pair here is going to generate a server socket (Local Port detection) and see if there is an exception (i.e. unable to generate the server socket), 
		public LocationManagerDriver() {
				lmLogger.log(Level.FINEST, "In Constructor");
				try {
						serverSocket = new ServerSocket(LM_PORT);
						System.out.println(getLocalPort());
				} catch (Exception e){
						lmLogger.log(Level.WARNING, "Unable to create server socket");
				}
				
		}

// This one keep running based on the input of the user.
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
								lmLogger.log(Level.WARNING, "Problem: " + e.toString() );
						}
				}
		}
		

// This function processes the commands from the user. Think about it like a mini web server when a request is sent and a result is expected to turn back.
// Additionally, this one is used to process the input and output stream.
		private String processCommands(String inputLine) {
				// Split on \n then on ' '
				String outLine = "";
				String[] args = inputLine.split(" ");
						if (args[0].equals("help")) {
								return(getUsageInfo());
						}
						else if (args[0].equals("gotoXYZ")) {
								X = Float.parseFloat(args[1].substring(2));
								Y = Float.parseFloat(args[2].substring(2));
								Z = Float.parseFloat(args[3].substring(2));
								return ("OK");
						}
						else if (args[0].equals("getLocation")) {
								return ("X="+ String.format("%.2f",X) +
												"Y="+ String.format("%.2f",Y) +
												"Z="+ String.format("%.2f",Z) );
						}
						else {
								return("Error: LocationManagerDriver unknown command\n");
						}
		}

}

