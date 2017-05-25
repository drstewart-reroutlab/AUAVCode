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
 * org.reroutlab.code.auav.drivers.CaptureImageDriver is an AUAV service.
 * gotoXYZ and getLocation are the only supported commands
 * 
 * 
 *
 * @author  Gavin Chau Nguyen
 * @version 0.01
 * @since   2017-05-24 
 */

// CaptureImageDriver contains the code for behavior specified in the AUAVDrivers, plus this thread is accessible globally
public class CaptureImageDriver implements Runnable,org.reroutlab.code.auav.drivers.AuavDrivers {

// Initializing the variables, used only in this module, no public access (outside of the method)
		private int IMAGE_PORT = 0;
		private int PIC_NUM = 0;

// Main program: creating the new thread & new CaptureImageDriver
		public static void main(String[] args) {
				CaptureImageDriver cids = new CaptureImageDriver();
				//System.out.println("LocalPort: " + lm.getLocalPort() );
				Thread cidsT = new Thread(cids);
				cidsT.start();
				
		}

		
    private static Logger cidsLogger =
				Logger.getLogger(CaptureImageDriver.class.getName());
		public void setLogLevel(Level l) {
				cidsLogger.setLevel(l);
		}		
		
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
		}
		
		private String usageInfo=";PicLabel is ##;GetImage;";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		public CaptureImageDriver() {
				cidsLogger.log(Level.FINEST, "In Constructor");
				try {
						serverSocket = new ServerSocket(IMAGE_PORT);
						System.out.println(getLocalPort());
				} catch (Exception e){
						cidsLogger.log(Level.WARNING, "Unable to create server socket");
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
								cidsLogger.log(Level.WARNING, "Problem: " + e.toString() );
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
						else if (args[0].equals("PicLabel")) {
								PIC_NUM = Integer.parseInt(args[2]);
								return ("CapturedImage");
						}
						else if (args[0].equals("GetImage")) {
								return ("The label of the image is " + PIC_NUM + ".");
						}
						else {
								return("Error: CaptureImageDriver unknown command\n");
						}
		}

}

