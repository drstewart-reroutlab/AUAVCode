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

// LocationManagerDriver contains the code for behavior specified in the AUAVDrivers, plus this thread is accessible globally
public class CaptureImageDriver implements org.reroutlab.code.auav.drivers.AuavDrivers {

// Initializing the variables, used only in this module, no public access (outside of the method)
		private int IMAGE_PORT = 0;
		private int PIC_NUM = 0;
		private String AUAVHOME_STRING = System.getenv("AUAVHOME");
		private String PIC_ADDR = AUAVHOME_STRING +"/src/org/reroutlab/code/auav/drivers/CaptureImageDriver/CamEmu/";
		private static int LISTEN_PORT = 0;
		private int driverPort = 0;

		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}

// Main program: creating the new thread & new Location Manager Driver
		public static void main(String[] args) {
				try { 
						CaptureImageDriver cids = new CaptureImageDriver();
						cids.getCoapServer().start();
				}
				catch (Exception e) {
						cidsLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}
		
    		private static Logger cidsLogger =
				Logger.getLogger(CaptureImageDriver.class.getName());
		public void setLogLevel(Level l) {
				cidsLogger.setLevel(l);
		}		
		
		public int getLocalPort() {
			return driverPort;
		}
		
		private String usageInfo=";dc=PicLabel ##;dc=GetImage;";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		public CaptureImageDriver() throws Exception {
				cidsLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer(); //initilize the server
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);//get the address
				CoapEndpoint tmp = new CoapEndpoint(bindToAddress); //create endpoint
				cs.addEndpoint(tmp);//add endpoint to server				
				tmp.start();//Start this endpoint and all its components.
				driverPort = tmp.getAddress().getPort();
				
				cs.add(new cidsResource());
				
		}

		private class cidsResource extends CoapResource {
				public cidsResource() {
						super("cr");//???
						getAttributes().setTitle("cr");//???
				}
				/**
				 *
				 * This function process the input commands, if the there is list, append to 
				 * the string, if not call sendTo methond find out the matching information
				 *
				 */
		

		@Override
		public void handlePUT(CoapExchange ce) {
				byte[] payload = ce.getRequestPayload();
				String inputLine = "";
				try {
					inputLine  = new String(payload, "UTF-8");
				}
						catch ( Exception uee) {
								System.out.println(uee.getMessage());
						}
						System.out.println("\n InputLine: "+inputLine);				
				// Split on \n then on ' '
				String outLine = "";
				String[] args = inputLine.split("-");//???

						if (args[0].equals("dc=help")) {
								ce.respond(getUsageInfo());
						}
						else if (args[0].equals("dc=PicLabel")) {
								PIC_NUM = Integer.parseInt(args[1]);
								ce.respond("Set PIC_NUM to " + PIC_NUM);
						}
						else if (args[0].equals("dc=GetImage")) {
								ce.respond(PIC_ADDR + PIC_NUM );
						}
						else {
								ce.respond("Error: LocationManagerDriver unknown command\n");
						}
		}
	}
}

