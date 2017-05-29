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

public class ExternalCommandsDriver implements org.reroutlab.code.auav.drivers.AuavDrivers {
		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}
		public static void main(String[] args) {
				try { 
						ExternalCommandsDriver ecd = new ExternalCommandsDriver();
						ecd.getCoapServer().start();
				}
				catch (Exception e) {
						ecdLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}




		//implement allows to use Thread
		//indicate port number
		private static int LISTEN_PORT = 5117;
		private int driverPort = 0;
		
    private static Logger ecdLogger =
				Logger.getLogger(ExternalCommandsDriver.class.getName());//return the name of the entity represented by this class object
    				//get Logger object by calling getLogger receive the name of the ExternalCommandDriver.class'name
		/**
		 *
		 *
		 * This specifying which message levels will be logged by this logger
		 *
		 */
		public void setLogLevel(Level l) {
				ecdLogger.setLevel(l);
						}		
		
		/**
		 *
		 * This function just to check if serverSocket is null, set the local port number in to -1
		 * 
		 * @return local port number
		 *
		 */
		public int getLocalPort() {
						return driverPort;
		}
		
		private String usageInfo="CMD param=value param=value param=value; list ";

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

		
		public ExternalCommandsDriver() throws SocketException {
				ecdLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer();
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);
				cs.addEndpoint(new CoapEndpoint(bindToAddress));
				driverPort = bindToAddress.getPort();
				
				cs.add(new ecdResource());
				
		}

		private class ecdResource extends CoapResource {
				public ecdResource() {
						super("cr");
						getAttributes().setTitle("cr");
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
						//System.out.println("\n InputLine: "+inputLine);

						String outLine = "";
						String[] args = inputLine.split("-");
						ecdLogger.log(Level.WARNING, "Send the cmd: " + inputLine );									 
						String driverName = args[0].substring(args[0].indexOf("=")+1,args[0].length());
						driverName = driverName.trim();
						// Format: dn=driver_name dc=driver_cmd [driver_prm=driver_arg]*
						if (driverName.endsWith("list")) {
								String output = "";
								if (driver2port != null) {
										Set keys = driver2port.keySet(); //HashMap<port, usageInfo>
										for (Iterator i = keys.iterator(); i.hasNext(); ) {
												String name = (String) i.next();
												String value = (String) driver2port.get(name);
												output = output + name + "-->" + value + "\n";
										}
								}
								outLine = outLine + "Active Drivers\n"+output;
								ce.respond(outLine);
						}
						else if (driver2port.containsKey(driverName) )  {
								String[] tmp_port = ((String)driver2port.get(driverName)).split(":");
								CoapClient client = new CoapClient("coap://127.0.0.1:"+tmp_port[1].trim()+"/cr");
								String c = "";										
								for (int i=1; i < args.length;i++) {
										c = c + args[i] + "-";
								}
								c = c.trim();
								ecdLogger.log(Level.WARNING, "Send the cmd: " + c );									 
								CoapResponse response = client.put(c,0);
								ce.respond(response.getResponseText());
						}
						else {
								ce.respond("Error: Unable to find driver " + inputLine);
						}
				}
		}

}

