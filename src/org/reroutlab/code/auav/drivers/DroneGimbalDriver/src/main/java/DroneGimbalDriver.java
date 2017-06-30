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
import dji.common.gimbal.DJIGimbalSpeedRotation;
import dji.common.gimbal.DJIGimbalRotateDirection;
import dji.common.util.DJICommonCallbacks;
import dji.common.error.DJISDKError;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.products.DJIAircraft;
import dji.sdk.products.DJIHandHeld;
import dji.sdk.sdkmanager.DJIBluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;


/**
 * org.reroutlab.code.auav.drivers.DroneGimbalDriver is an AUAV service.
 * move is the only supported command
 * 
 * 
 *
 * @author  Christopher Stewart
 * @version 0.01
 * @since   2017-05-01 
 */

public class DroneGimbalDriver implements org.reroutlab.code.auav.drivers.AuavDrivers { //implements???
		
		private DJIGimbalSpeedRotation mPitchSpeedRotation;
		private DJIGimbalSpeedRotation mRollSpeedRotation;
		private DJIGimbalSpeedRotation mYawSpeedRotation;
		private Timer mTimer;
		private GimbalRotateTimerTask mGimbalRotationTimerTask;
	
		private CoapServer cs;
		public CoapServer getCoapServer() {
				return (cs);
		}
		//start a server
		public static void main(String[] args) {
				try { 
						DroneGimbalDriver dgd = new DroneGimbalDriver();
						dgd.getCoapServer().start();
				}
				catch (Exception e) {
						dgdLogger.log(Level.WARNING, "Unable to start server" + e.getMessage());
				}
				
		}




		//implement allows to use Thread
		//indicate port number
		private static int LISTEN_PORT = 0;
		private int driverPort = 0;
		//???
		
   		private static Logger dgdLogger =
				Logger.getLogger(DroneGimbalDriver.class.getName());
		public void setLogLevel(Level l) {
				dgdLogger.setLevel(l);
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
		public DroneGimbalDriver() throws Exception {
				dgdLogger.log(Level.FINEST, "In Constructor");
				cs = new CoapServer(); //initilize the server
				InetSocketAddress bindToAddress = new InetSocketAddress("localhost", LISTEN_PORT);//get the address
				CoapEndpoint tmp = new CoapEndpoint(bindToAddress); //create endpoint
				cs.addEndpoint(tmp);//add endpoint to server				
				tmp.start();//Start this endpoint and all its components.
				driverPort = tmp.getAddress().getPort();
				
				cs.add(new dgdResource());
				
		}



		
		//extends CoapResource class
		private class dgdResource extends CoapResource {
				public dgdResource() {
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
						else if (args[0].equals("dc=move")) {
								String[] kv1= args[1].split("=");
								String[] kv2= args[2].split("=");
								String[] kv3= args[3].split("=");								
								mPitchSpeedRotation = new DJIGimbalSpeedRotation(Float.parseFloat(kv1[1]),
																																 DJIGimbalRotateDirection.Clockwise);
								mRollSpeedRotation = new DJIGimbalSpeedRotation(Float.parseFloat(kv2[1]),
																																DJIGimbalRotateDirection.Clockwise);
								mYawSpeedRotation = new DJIGimbalSpeedRotation(Float.parseFloat(kv3[1]),
																															 DJIGimbalRotateDirection.Clockwise);
								
								long t = System.currentTimeMillis();
								long end = t + 3000;
								while (System.currentTimeMillis() < end) {
										if (mTimer == null) {
												mTimer = new Timer();
												mPitchSpeedRotation = new DJIGimbalSpeedRotation(10,
																																				 DJIGimbalRotateDirection.Clockwise);
												mGimbalRotationTimerTask = new GimbalRotateTimerTask(mPitchSpeedRotation,
																																						 mRollSpeedRotation,
																																						 mYawSpeedRotation);
												mTimer.schedule(mGimbalRotationTimerTask, 0, 100);
										}
								}

								if (mTimer != null) {
										mGimbalRotationTimerTask.cancel();
										mTimer.cancel();
										mTimer.purge();
										mGimbalRotationTimerTask = null;
										mTimer = null;
								}
								

								ce.respond ("Gimbal=Moved");
						}
						else {
								ce.respond("Error: DroneGimbalDriver unknown command\n");
						}
			}	
		}	

    		private static class GimbalRotateTimerTask extends TimerTask {
				DJIGimbalSpeedRotation mPitch;
				DJIGimbalSpeedRotation mRoll;
				DJIGimbalSpeedRotation mYaw;
				
				GimbalRotateTimerTask(DJIGimbalSpeedRotation pitch, DJIGimbalSpeedRotation roll, DJIGimbalSpeedRotation yaw) {

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

					else if(DJISDKManager.getInstance().getDJIProduct()==null){
						System.out.println("GetDJIProduct");

					//	dgdLogger.log(Level.FINEST, "getDJIProduct");

					}						
					if (DJISDKManager.getInstance().getDJIProduct().getGimbal() != null) {
						DJISDKManager.getInstance().getDJIProduct().
							getGimbal().rotateGimbalBySpeed(mPitch,	mRoll, mYaw,
									new DJICommonCallbacks.DJICompletionCallback() {
										@Override
										public void onResult(DJIError error) {
																														
										}
								});

								//using if-else statement to find which part lead to null
						}
				}catch(Exception e){
					
					dgdLogger.log(Level.WARNING, "Can't connect" + e.getStackTrace().toString());
					dgdLogger.log(Level.WARNING, e.toString());



				}
				}
		}
		
		
}
