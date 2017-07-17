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
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.gimbal.GimbalMode;

import dji.common.util.CommonCallbacks;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
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
		
		private String LOG_TAG="DroneGimbalDriver ";

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
								System.out.println(LOG_TAG+ " Moving Gimbal");
								Aircraft aircraft = (Aircraft)DJISDKManager.getInstance().getProduct();
								if(aircraft.getGimbal()!=null){



								aircraft.getGimbal().startCalibration(new CommonCallbacks.CompletionCallback() {
																														    @Override
																													  	    public void onResult(DJIError djiError) {
																															 if (djiError == null) {
																																	 System.out.println(LOG_TAG+"Move success");
																															 }
																															 else {
																																	 System.out.println(LOG_TAG+djiError.getDescription());
																															 }
																													 			}
																														   }
																														 );



								long t = System.currentTimeMillis();
								long end = t + 1000;
								while (System.currentTimeMillis() < end) {
										if (mTimer == null) {
												mTimer = new Timer();
												mGimbalRotationTimerTask = new GimbalRotateTimerTask((float)7);
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

								
								ce.respond ("Move Gimbal Complete");}

							else{

									ce.respond ("No Gimbal");


								}
						}
						else {
								ce.respond("Error: DroneGimmbal unknown command\n");
						}
			}	
		}
	private static class GimbalRotateTimerTask extends TimerTask {
        float pitchValue;

        GimbalRotateTimerTask(float pitchValue) {
            super();
            this.pitchValue = pitchValue;
        }

        @Override
        public void run() {
                    DJISDKManager.getInstance().getProduct().getGimbal().
                    rotate(new Rotation.Builder().pitch(pitchValue)
                                                 .mode(RotationMode.SPEED)
                                                 .yaw(Rotation.NO_ROTATION)
                                                 .roll(Rotation.NO_ROTATION)
                                                 .time(0)
                                                 .build(), new CommonCallbacks.CompletionCallback() {

                       					 @Override
																													 public void onResult(DJIError djiError) {
																															 if (djiError == null) {
																																	 System.out.println("Move success");
																															 }
																															 else {
																																	 System.out.println(djiError.getDescription());
																															 }
																													 }
						 });
            }
    }	

    				
		
}
