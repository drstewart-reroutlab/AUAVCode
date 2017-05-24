package org.reroutlab.code.auav.kernels;

import org.reroutlab.code.auav.drivers.ExternalCommandsDriver;
import org.reroutlab.code.auav.drivers.LocationManagerDriver;
import org.reroutlab.code.auav.drivers.CatchImageDriver;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

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
				theLogger.setLevel(AUAVLEVEL); //set logger's level

				// Create new driver objects
				theLogger.log(Level.FINE,"Creating driver objects");
				ExternalCommandsDriver ecd = new ExternalCommandsDriver();
				LocationManagerDriver lm = new LocationManagerDriver();
				CatchImageDriver cid = new CatchImageDriver();
				
				// Gather name to ports/usage mapping
				theLogger.log(Level.FINE,"Creating driver-to-port mapping");				
				n2p.put(ecd.getClass().getCanonicalName(),
								new String("Port:"+ecd.getLocalPort()+"\n" ) );
				n2p.put(lm.getClass().getCanonicalName(),
								new String("Port:"+lm.getLocalPort()+"\n" ) );
				n2p.put(cid.getClass().getCanonicalName(),
								new String("Port:"+cid.getLocalPort()+"\n" ) );

				

				// Send the map back to each object
				ecd.setDriverMap(n2p);
				lm.setDriverMap(n2p);
				cid.setDriverMap(n2p);				
				ecd.setLogLevel(AUAVLEVEL);
				lm.setLogLevel(AUAVLEVEL);
				cid.setLogLevel(AUAVLEVEL);


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
				
				
		}

		public static void main(String args[]) throws Exception {
				LinuxKernel k = new LinuxKernel();
		}
}

