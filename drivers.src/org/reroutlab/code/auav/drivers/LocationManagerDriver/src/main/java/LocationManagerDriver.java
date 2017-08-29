package org.reroutlab.code.auav.drivers;

import java.util.*;
import java.lang.System.*;
//import org.reroutlab.code.auav.interfaces.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;



/**
 * org.reroutlab.code.auav.drivers.ExternalCommandsDriver is an AUAV service.
 * The service accepts commands that could go to *any* module.
 * It also supports a "list" command that tells of all available modules.
 * It also supports a "help" command that explains syntax.
 *
 * @author  Christopher Stewart
 * @version 0.01
 * @since   2017-05-01 
 */

public class LocationManagerDriver implements org.reroutlab.code.auav.drivers.AuavDrivers {
		// Initializing the variables, used only in this module, no public access (outside of the method)
		private float X = 0;
		private float Y = 0;
		private float Z = 0;
		
		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}
		//start a server
		public static void main(String[] args) {
				try { 
						LocationManagerDriver lmd = new LocationManagerDriver();
						lmd.getCoapServer().start();
				}
				catch (Exception e) {
						lmdLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}




		//implement allows to use Thread
		//indicate port number
		private static int LISTEN_PORT = 0;
		private int driverPort = 0;
		//???
		
    		private static Logger lmdLogger =
				Logger.getLogger(LocationManagerDriver.class.getName());//return the name of the entity represented by this class object
    				//get Logger object by calling getLogger receive the name of the ExternalCommandDriver.class'name
		/**
		 *
		 *
		 * This specifying which message levels will be logged by this logger
		 *
		 */
		public void setLogLevel(Level l) {
				lmdLogger.setLevel(l);
						}		
		
		
		public int getLocalPort() {
						return driverPort; //???
		}
		
		private String usageInfo=";gotoXYZ X=## Y=## Z=##; getLocation;";

		/**
		 *
		 * This function return the design string to respond to call
		 *
		 *
		 * @return usageInfo 
		 *
		 */
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}

		/**
		 *
		 * This function help construct Map
		 *
		 * @param HashMap<String, String> m
		 *
		 */
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		//constructor???
		public LocationManagerDriver() throws Exception {
				lmdLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer(); //initilize the server
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);//get the address
				CoapEndpoint tmp = new CoapEndpoint(bindToAddress); //create endpoint
				cs.addEndpoint(tmp);//add endpoint to server				
				tmp.start();//Start this endpoint and all its components.
				driverPort = tmp.getAddress().getPort();
				
				cs.add(new lmdResource());
				
		}



		//extends CoapResource class
		private class lmdResource extends CoapResource {
				public lmdResource() {
						super("cr");//???
						getAttributes().setTitle("cr");//???
				}
				/**
				 *
				 * This function process the input commands, if the there is list, append to 
				 * the string, if not call sendTo methond find out the matching information
				 *
				 * @return information in the list
				 *
				 *
				 */
				@Override
				public void handlePUT(CoapExchange ce) {
						// Split on & and = then on ' '
						byte[] payload = ce.getRequestPayload();
						String inputLine = "";
						try {
								inputLine  = new String(payload, "UTF-8");
						}
						catch ( Exception uee) {
								System.out.println(uee.getMessage());
						}
						System.out.println("\n InputLine: "+inputLine);

						String outLine = "";
						String[] args = inputLine.split("-");//???
						
						// Format: dc=driver_cmd [driver_prm=driver_arg]*						
						if (args[0].equals("dc=help")) {
								ce.respond(getUsageInfo());
						}
						else if (args[0].equals("dc=gotoXYZ")) {
								X = Float.parseFloat(args[1].substring(2));
								Y = Float.parseFloat(args[2].substring(2));
								Z = Float.parseFloat(args[3].substring(2));
								ce.respond ("OK");
						}
						else if (args[0].equals("dc=getLocation")) {
								ce.respond ("X="+ String.format("%.2f",X) +
												"Y="+ String.format("%.2f",Y) +
												"Z="+ String.format("%.2f",Z) );
						}
						else {
								ce.respond("Error: LocationManagerDriver unknown command\n");
						}
						
						
				}
		}

}

