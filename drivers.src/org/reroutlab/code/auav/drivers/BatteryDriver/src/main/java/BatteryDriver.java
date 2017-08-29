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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;

public class BatteryDriver implements org.reroutlab.code.auav.drivers.AuavDrivers {

		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}
		//start a server
		public static void main(String[] args) {
				try { 
						BatteryDriver bd = new BatteryDriver();
						bd.getCoapServer().start();
				}
				catch (Exception e) {
						bdLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}




		//implement allows to use Thread
		//indicate port number
		private static int LISTEN_PORT = 0;
		private int driverPort = 0;
		//???
		

		
   		private static Logger bdLogger =
				Logger.getLogger(BatteryDriver.class.getName());
		public void setLogLevel(Level l) {
				bdLogger.setLevel(l);
		}		
		
		public int getLocalPort() {
				return driverPort; 
		}
		
		private String usageInfo="getBattery";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		
		//constructor???
		public BatteryDriver() throws Exception {
				bdLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer(); //initilize the server
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);//get the address
				CoapEndpoint tmp = new CoapEndpoint(bindToAddress); //create endpoint
				cs.addEndpoint(tmp);//add endpoint to server				
				tmp.start();//Start this endpoint and all its components.
				driverPort = tmp.getAddress().getPort();
				
				cs.add(new bdResource());
				
		}



		
		//extends CoapResource class
		private class bdResource extends CoapResource {
				public bdResource() {
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
						else if (args[0].equals("dc=getbattery")) {
								BroadcastReceiver battery = new BroadcastReceiver(){
        								@Override
        								public void onReceive(Context context, Intent intent) {
          							  	int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
									String s=String.valueOf(level);
									bdLogger.log(Level.FINEST,s+"%");
               								}

								};
									ce.respond("OK");	



    	    					}else {
								ce.respond("Error: BatteryDriver unknown command\n");
						}
	
    			    
		
				}
		}
}
