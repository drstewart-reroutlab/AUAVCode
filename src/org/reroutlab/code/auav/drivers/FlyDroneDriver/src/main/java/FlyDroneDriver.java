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



import java.util.Timer;
import java.util.TimerTask;


import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.eventbus.EventBus;
import dji.common.util.CommonCallbacks;


/**
 * org.reroutlab.code.auav.drivers.FlyDroneDriver is an AUAV service.
 * move is the only supported command
 * 
 * 
 *
 * @author  Christopher Stewart
 * @version 0.01
 * @since   2017-05-01 
 */

public class FlyDroneDriver implements org.reroutlab.code.auav.drivers.AuavDrivers { //implements???

    private float FmPitch;
		private float FmRoll;
		private float FmYaw;
		private float FmThrottle;
		private float RmPitch;
		private float RmRoll;
		private float RmYaw;
		private float RmThrottle;
		private Timer mTimer;
		private String LOG_TAG="FlyDroneDriver ";
		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}
		//start a server
		public static void main(String[] args) {
				try { 
						FlyDroneDriver fdd = new FlyDroneDriver();
						fdd.getCoapServer().start();
				}
				catch (Exception e) {
						fddLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}




		//implement allows to use Thread
		//indicate port number
		private static int LISTEN_PORT = 0;
		private int driverPort = 0;
		//???
		
   		private static Logger fddLogger =
				Logger.getLogger(FlyDroneDriver.class.getName());
		public void setLogLevel(Level l) {
				fddLogger.setLevel(l);
		}		
		
		public int getLocalPort() {
				return driverPort; //???
		}
		
		private String usageInfo=";move Pitch=## Yaw=## Roll=##;";
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
		public FlyDroneDriver() throws Exception {
				fddLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer(); //initilize the server
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);//get the address
				CoapEndpoint tmp = new CoapEndpoint(bindToAddress); //create endpoint
				cs.addEndpoint(tmp);//add endpoint to server				
				tmp.start();//Start this endpoint and all its components.
				driverPort = tmp.getAddress().getPort();
				
				cs.add(new fddResource());
				
		}



		
		//extends CoapResource class
		private class fddResource extends CoapResource {
				public fddResource() {
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
						else if (args[0].equals("dc=lft")) {
								System.out.println(LOG_TAG+ " Taking Off");
								Aircraft aircraft = (Aircraft)DJISDKManager.getInstance().getProduct();
								aircraft.getFlightController().startTakeoff(
																											 new CommonCallbacks.CompletionCallback() {
																													 @Override
																													 public void onResult(DJIError djiError) {
																															 if (djiError == null) {
																																	 System.out.println(LOG_TAG+"Takeoff success");
																															 }
																															 else {
																																	 System.out.println(LOG_TAG+djiError.getDescription());
																															 }
																													 }
																											 }
																											 );

								ce.respond ("startTakeoff Complete");
						}
						else if (args[0].equals("dc=lnd")) {
								System.out.println(LOG_TAG+ " Landing");
								Aircraft aircraft = (Aircraft)DJISDKManager.getInstance().getProduct();
								aircraft.getFlightController().startLanding(
																											 new CommonCallbacks.CompletionCallback() {
																													 @Override
																													 public void onResult(DJIError djiError) {
																															 if (djiError == null) {
																																	 System.out.println(LOG_TAG+"Landing success");
																															 }
																															 else {
																																	 System.out.println(LOG_TAG+djiError.getDescription());
																															 }
																													 }
																											 }
																											 );

								ce.respond ("startLanding Complete");
						}

						else {
								ce.respond("Error: FlyDroneDriver unknown command\n");
						}
			}	
		}	

    		private static class GimbalRotateTimerTask extends TimerTask {
				float mPitch; float mRoll;
				float mYaw;
				
				GimbalRotateTimerTask(float pitch, float roll, float yaw) {

						super();
						this.mPitch = pitch;
						this.mRoll = roll;
						this.mYaw = yaw;
				}
				
				@Override
				public void run() {


					

				try{
					//DJISDKManager.getInstance().registerApp();
					//System.out.println("Register successful");

					if(DJISDKManager.getInstance()==null){
						System.out.println("GetInstance");}

					else if(DJISDKManager.getInstance().getProduct()==null){
						System.out.println("GetProduct");

					//	fddLogger.log(Level.FINEST, "getDJIProduct");

					}						
					if (DJISDKManager.getInstance().getProduct().getGimbal() != null) {

								//using if-else statement to find which part lead to null
						}
				}catch(Exception e){
					
					fddLogger.log(Level.WARNING, "Can't connect" + e.getStackTrace().toString());
					fddLogger.log(Level.WARNING, e.toString());



				}
				}
		}
		
		
}
