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
 * org.reroutlab.code.ExternalCommands is an AUAV service.
 * The service accepts commands that could go to *any* module.
 * It also supports a "list" command that tells of all available modules.
 * It also supports a "help" command that explains syntax.
 *
 * @author  Christopher Stewart
 * @version 0.01
 * @since   2017-05-01 
 */

public class ExternalCommandsDriver implements Runnable,org.reroutlab.code.auav.drivers.AuavDrivers {
		private int ECD_PORT = 5117;

    private static Logger ecdLogger =
				Logger.getLogger(ExternalCommandsDriver.class.getName());
		public void setLogLevel(Level l) {
				ecdLogger.setLevel(l);
		}		
		
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
		}
		
		private String usageInfo="CMD param=value param=value param=value; list ";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		public ExternalCommandsDriver() {
				ecdLogger.log(Level.FINEST, "In Constructor");
				try {
						serverSocket = new ServerSocket(ECD_PORT);
				} catch (Exception e){
						ecdLogger.log(Level.WARNING, "Unable to create server socket");
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
								String lastLine = "";
								if (inputLine.equals("mult")) {
										do {
												lastLine = bufferedReader.readLine();
												inputLine = inputLine + "\n" + lastLine;
										} while (lastLine.equals("mult") == false);
								}
								if (inputLine != null) {
										inputLine = inputLine.trim();
										String outputLine = processCommands(inputLine);
										printWriter.println(outputLine);
								}
								clientSocket.close();
						}
						catch (Exception e) {
								ecdLogger.log(Level.WARNING, "Problem: " + e.toString() );
						}
				}
		}
		
		public static void main(String[] args) {
				ExternalCommandsDriver ecd = new ExternalCommandsDriver();
				//System.out.println("LocalPort: " + ecd.getLocalPort() );
				
		}


		private String processCommands(String inputLine) {
				// Split on \n then on ' '
				String outLine = "";
				String[] cmds = inputLine.split("\n");
				for (String cmd: cmds) {
						String[] args = cmd.split(" ");
						// Format: driver_name driver_cmd [driver_prm=driver_arg]*
						if (args[0].equals("list")) {
								Set keys = driver2port.keySet();
								String output = "";
								for (Iterator i = keys.iterator(); i.hasNext(); ) {
										String name = (String) i.next();
										String value = (String) driver2port.get(name);
										output = output + name + "-->" + value + "\n";
								}
								outLine = outLine + "Active Drivers\n"+output;
						}
						else if (driver2port.containsKey(args[0]) )  {
								String c = "";
								for (int i=1; i < args.length;i++) {
										c = c + args[i] + " ";
								}
								c = c.trim();
								ecdLogger.log(Level.WARNING, "Send the cmd: " + c );
								outLine = outLine + sendTo((String)driver2port.get(args[0]), c);
						}
						else {
								outLine = outLine+ "Error\n";
						}
				}
				return outLine;
		}

		private String sendTo(String p, String c) {
				String[] portInfo = p.split(":");
				try {
						Socket echoSocket = new Socket("127.0.0.1", Integer.parseInt(portInfo[1].trim()) );
						PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
						out.println(c);
						String retVal = in.readLine();
						echoSocket.close();
						return(retVal);
				}
				catch(Exception e) {
								ecdLogger.log(Level.WARNING, "Unable to sendTo : " + p +"  " + e.toString() );
				}
				return("Error in sendTo prevented communication");
		}
}

