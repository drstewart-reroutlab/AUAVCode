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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;

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
public class ChargingBatteryDriver implements org.reroutlab.code.auav.drivers.AuavDrivers {

// Initializing the variables, used only in this module, no public access (outside of the method)
		private int BATTERY_PORT = 0;
		private int BATTERY_STATUS = 0; // Percentage of battery remaining
		private int TIME_REMAINING = 0; // Assuming the DJI battery has duration of 25 minutes, which means 1500 seconds. This will estimate the remaining time of the battery, so the user can control when to charge.
		private int CHARGING_TIME = 0; // Assuming the DJI battery takes 1.5 hours to charge from 0% to 100%, which means 5400 seconds. This will estimate the time to fully charge the battery.
		private static int LISTEN_PORT = 0;
		private int driverPort = 0;
		
		
// Main program: creating the new thread & new Location Manager Driver

		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}

		public static void main(String[] args) {
			try {
				ChargingBatteryDriver cbd = new ChargingBatteryDriver();
				cbd.getCoapServer().start();
			}
				catch (Exception e) {
				cbdLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}

		
    private static Logger cbdLogger =
				Logger.getLogger(ChargingBatteryDriver.class.getName());
		public void setLogLevel(Level l) {
				cbdLogger.setLevel(l);
		}		
		
		public int getLocalPort() {
				return driverPort; 
		}
		
		private String usageInfo=";dc=batteryPercentage; dc=getRemainingDuration; dc=getChargingTime";
		public String getUsageInfo() {
				return usageInfo;
		}

		

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap<String, String> m) {
				if (m != null) {
						driver2port = new HashMap<String, String>(m);
				}
		}

		
		public ChargingBatteryDriver() throws Exception {
				cbdLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer(); //initilize the server
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);//get the address
				CoapEndpoint tmp = new CoapEndpoint(bindToAddress); //create endpoint
				cs.addEndpoint(tmp);//add endpoint to server				
				tmp.start();//Start this endpoint and all its components.
				driverPort = tmp.getAddress().getPort();
				
				cs.add(new cbdResource());
				
		}

		//extends CoapResource class
		private class cbdResource extends CoapResource {
				public cbdResource() {
						super("cr");//???
						getAttributes().setTitle("cr");//???
				}
		
			@Override
			public void handlePUT(CoapExchange ce) {
				// Split on \n then on ' '
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
				String[] args = inputLine.split("-");
						if (args[0].equals("dc=help")) {
							ce.respond(getUsageInfo());
						}
						else if (args[0].equals("dc=batteryPercentage")) {
								BroadcastReceiver battery = new BroadcastReceiver(){
        								@Override
        								public void onReceive(Context context, Intent intent) {
          							  	int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
									BATTERY_STATUS = level;
									String s=String.valueOf(level);
									cbdLogger.log(Level.FINEST,s+"%");
               								}
								};
							ce.respond("OK");
						}
						else if (args[0].equals("getRemainingDuration")) {
								TIME_REMAINING = BATTERY_STATUS*15;
								ce.respond("The remaining duration of the battery is: " + TIME_REMAINING/60 + " minutes  and " + 										TIME_REMAINING%60 + " seconds.");
						}
						else if (args[0].equals("getChargingTime")) {
								CHARGING_TIME = (100-BATTERY_STATUS)*54;
								ce.respond("The remaining charging time of the battery is: " + CHARGING_TIME/60 + " minutes  and " + 										CHARGING_TIME%60 + " seconds.");
						}
						else {
								ce.respond("Error: ChargingBatteryDriver invalid command\n");
						}
				}
		}

}

