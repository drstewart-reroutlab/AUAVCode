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

public class DroneGimbalDriver implements Runnable {//,org.reroutlab.code.auav.drivers.AuavDrivers {
		private int DGD_PORT = 0;
		private DJIGimbalSpeedRotation mPitchSpeedRotation;
		private DJIGimbalSpeedRotation mRollSpeedRotation;
		private DJIGimbalSpeedRotation mYawSpeedRotation;
		private Timer mTimer;
		private GimbalRotateTimerTask mGimbalRotationTimerTask;

		
		public static void main(String[] args) {
				DroneGimbalDriver dgd = new DroneGimbalDriver();
				//System.out.println("LocalPort: " + dgd.getLocalPort() );
				Thread dgdT = new Thread(dgd);
				dgdT.start();
				
		}

		
    private static Logger dgdLogger =
				Logger.getLogger(DroneGimbalDriver.class.getName());
		public void setLogLevel(Level l) {
				dgdLogger.setLevel(l);
		}		
		
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
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

		
		public DroneGimbalDriver() {
				dgdLogger.log(Level.FINEST, "In Constructor");
				try {
						serverSocket = new ServerSocket(DGD_PORT);
						System.out.println(getLocalPort());
				} catch (Exception e){
						dgdLogger.log(Level.WARNING, "Unable to create server socket");
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
								dgdLogger.log(Level.WARNING, "Problem: " + e.toString() );
						}
				}
		}
		

		private String processCommands(String inputLine) {
				String outLine = "";
				String[] args = inputLine.split(" ");
						if (args[0].equals("help")) {
								return(getUsageInfo());
						}
						else if (args[0].equals("move")) {
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
								

								return ("Gimbal=Moved");
						}
						else {
								return("Error: DroneGimbalDriver unknown command\n");
						}
		}


    private static class GimbalRotateTimerTask extends TimerTask {
				DJIGimbalSpeedRotation mPitch;
				DJIGimbalSpeedRotation mRoll;
				DJIGimbalSpeedRotation mYaw;
				
				GimbalRotateTimerTask(DJIGimbalSpeedRotation pitch,
															DJIGimbalSpeedRotation roll,
															DJIGimbalSpeedRotation yaw) {
						super();
						this.mPitch = pitch;
						this.mRoll = roll;
						this.mYaw = yaw;
				}
				
				@Override
				public void run() {
						
						
						if (DJISDKManager.getInstance().getDJIProduct().getGimbal() != null) {
								DJISDKManager.getInstance().getDJIProduct().
										getGimbal().rotateGimbalBySpeed(mPitch,	mRoll, mYaw,
																										new DJICommonCallbacks.DJICompletionCallback() {
																												@Override
																												public void onResult(DJIError error) {
																														
																												}
																										});
						}
				}
		}
		
		
}

