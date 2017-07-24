package org.reroutlab.code.auav.kernels;

import org.reroutlab.code.auav.drivers.AuavDrivers;
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
		private Level AUAVLEVEL = Level.FINE; // set AuavLEVEL
		private static Logger theLogger =
				Logger.getLogger(LinuxKernel.class.getName());//get Logger object by calling getLogger receive the name of the LinuxKernal.class'name


		HashMap n2p = new HashMap<String, String>(); 
		AuavDrivers[] ad = new AuavDrivers[128];
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
				theLogger.setLevel(AUAVLEVEL); //set logger's level
				
				for (int x = 0; x < countDrivers; x++) {
						System.out.println("Jar: "+jarNames[x]);
						ad[x] = instantiate(jarNames[x],org.reroutlab.code.auav.drivers.AuavDrivers.class);
						n2p.put(ad[x].getClass().getCanonicalName(),
										new String(""+ad[x].getLocalPort()+"\n" ) );						
				}

				// Printing the map object locally for logging
				String mapAsString = "Active Drivers\n";
				Set keys = n2p.keySet();
				for (Iterator i = keys.iterator(); i.hasNext(); ) {
						String name = (String) i.next();
						String value = (String) n2p.get(name);
						mapAsString = mapAsString + name + " --> " + value + "\n";
				}
				theLogger.log(Level.INFO,mapAsString);
				
				for (int x = 0; x < countDrivers; x++) {
						// Send the map back to each object
						ad[x].setDriverMap(n2p);
						ad[x].setLogLevel(AUAVLEVEL);
						ad[x].getCoapServer().start();
				}

				
				
				
		}

		public static void main(String args[]) throws Exception {
				LinuxKernel k = new LinuxKernel();
		}

		// Code taken from stackoverflow in May 2017
		// Thanks Sean Patrick Floyd
		// Documentation by Christopher Stewart 
		public <T> T instantiate(final String className, final Class<T> type){
				try{
						return type.cast(Class.forName(className).newInstance());
				} catch(InstantiationException
								          | IllegalAccessException
								| ClassNotFoundException e){
						throw new IllegalStateException(e);
				}
		}
}

