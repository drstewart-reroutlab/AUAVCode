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



public class CatchImageDriver implements Runnable,org.reroutlab.code.auav.drivers.AuavDrivers {
		private int PORT_NUMBER = 0;
		private int PIC_LABEL = 0;

		public static void main(String[] args) {
				CatchImageDriver pic = new CatchImageDriver();
				//System.out.println("LocalPort: " + lm.getLocalPort() );
				Thread picT = new Thread(pic);
				picT.start();
				
		}

		
    		private static Logger picLogger =
				Logger.getLogger(CatchImageDriver.class.getName());
		public void setLogLevel(Level l) {
				picLogger.setLevel(l);
		}		
		
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
		}
		
		private String usageInfo=";getImage PIC_LABEL=##; ";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		public CatchImageDriver() {
				picLogger.log(Level.FINEST, "In Constructor");
				try {
						serverSocket = new ServerSocket(PORT_NUMBER);
						System.out.println(getLocalPort());
				} catch (Exception e){
						picLogger.log(Level.WARNING, "Unable to create server socket");
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
								picLogger.log(Level.WARNING, "Problem: " + e.toString() );
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
						else if (args[0].equals("getImage")) {
								PIC_LABEL =Integer.parseInt(args[1].substring(10)); //PIC_LABEL=##(THE 3rd string)
								//or add in the local path to access to the local files?
								//or remote server path?
								return ("Catch Image: "+ PIC_LABEL);
						}
						else {
								return("Error: CatchImageDriver unknown command\n");
						}
		}

}



