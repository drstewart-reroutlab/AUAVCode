package org.reroutlab.code.auav.kernels;

import org.reroutlab.code.auav.drivers.ExternalCommandsDriver;
import org.reroutlab.code.auav.drivers.LocationManagerDriver;
import org.reroutlab.code.auav.drivers.CatchImageDriver;
import org.reroutlab.code.auav.drivers.CaptureImageDriver;
import org.reroutlab.code.auav.drivers.ChargingBatteryDriver;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 *This class works like handler, connects all drivers
 *
 *
 */
public class LinuxKernel {
		private Level AUAVLEVEL = Level.FINE; // set AUAVLEVEL
		private static Logger theLogger =
				Logger.getLogger(LinuxKernel.class.getName());//get Logger object by calling getLogger receive the name of the LinuxKernal.class'name


		HashMap n2p = new HashMap<String, String>(); 

		public LinuxKernel ()  {//setup constructor 
				//				:/home/cstewart/reroutlab.code/reroutlab.cstewart.code.auav/libs/CaptureImageDriver.jar:/home/cstewart/reroutlab.code/reroutlab.cstewart.code.auav/libs/ChargingBatteryDriver.jar:

				
				String jarList = System.getProperty("java.class.path");
				String[] fullPath = jarList.split(".jar:");
				String[] jarNames = new String[fullPath.length];
				int countDrivers = 0;
				for (int x =0; x < fullPath.length;x++){
						String[] seps = fullPath[x].split("/");
						if (seps[seps.length - 1].endsWith("Driver") == true) {
								jarNames[countDrivers] = seps[seps.length - 1];
								countDrivers++;						
						}
				}

				for (int x = 0; x < countDrivers; x++) {
						System.out.println("Jar: " + jarNames[x]);
				}
				
				theLogger.setLevel(AUAVLEVEL); //set logger's level

				// Create new driver objects
				theLogger.log(Level.FINE,"Creating driver objects");
				ExternalCommandsDriver ecd = new ExternalCommandsDriver();
				LocationManagerDriver lm = new LocationManagerDriver();
				CatchImageDriver cid = new CatchImageDriver();
				CaptureImageDriver cids = new CaptureImageDriver();
				ChargingBatteryDriver cb = new ChargingBatteryDriver();
				
				// Gather name to ports/usage mapping
				theLogger.log(Level.FINE,"Creating driver-to-port mapping");				
				n2p.put(ecd.getClass().getCanonicalName(),
								new String("Port:"+ecd.getLocalPort()+"\n" ) );
				n2p.put(lm.getClass().getCanonicalName(),
								new String("Port:"+lm.getLocalPort()+"\n" ) );
				n2p.put(cid.getClass().getCanonicalName(),
								new String("Port:"+cid.getLocalPort()+"\n" ) );
				n2p.put(cids.getClass().getCanonicalName(),
								new String("Port:"+cids.getLocalPort()+"\n" ) );
				n2p.put(cb.getClass().getCanonicalName(),
								new String("Port:"+cb.getLocalPort()+"\n" ) );
				

				// Send the map back to each object
				ecd.setDriverMap(n2p);
				lm.setDriverMap(n2p);
				cid.setDriverMap(n2p);
				cids.setDriverMap(n2p);
				cb.setDriverMap(n2p);				
				ecd.setLogLevel(AUAVLEVEL);
				lm.setLogLevel(AUAVLEVEL);
				cid.setLogLevel(AUAVLEVEL);
				cids.setLogLevel(AUAVLEVEL);
				cb.setLogLevel(AUAVLEVEL);

				// Printing the map object locally for logging
				String mapAsString = "Active Drivers\n";
				Set keys = n2p.keySet();
				for (Iterator i = keys.iterator(); i.hasNext(); ) {
						String name = (String) i.next();
						String value = (String) n2p.get(name);
						mapAsString = mapAsString + name + " --> " + value + "\n";
				}
				theLogger.log(Level.INFO,mapAsString);								
				
				// Start each thread
				theLogger.log(Level.FINE,"Activating threads");				
				Thread ecdT = new Thread(ecd);
				ecdT.start();//This will call run method automatically
				Thread lmT = new Thread(lm);
				lmT.start();
				Thread cidT = new Thread(cid);
				cidT.start();
				Thread cidsT = new Thread(cids);
				cidsT.start();
				Thread cbT = new Thread(cb);
				cbT.start();
				
				
		}

		public static void main(String args[]) throws Exception {
				LinuxKernel k = new LinuxKernel();
		}
}

