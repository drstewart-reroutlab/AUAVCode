package org.reroutlab.code.auav.kernels;

import org.reroutlab.code.auav.drivers.ExternalCommandsDriver;
import org.reroutlab.code.auav.drivers.LocationManagerDriver;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


public class LinuxKernel {
		private Level AUAVLEVEL = Level.FINE;
		private static Logger theLogger =
				Logger.getLogger(LinuxKernel.class.getName());


		HashMap n2p = new HashMap<String, String>();

		public LinuxKernel ()  {
				theLogger.setLevel(AUAVLEVEL);

				// Create new driver objects
				theLogger.log(Level.FINE,"Creating driver objects");
				ExternalCommandsDriver ecd = new ExternalCommandsDriver();
				LocationManagerDriver lm = new LocationManagerDriver();
				
				// Gather name to ports/usage mapping
				theLogger.log(Level.FINE,"Creating driver-to-port mapping");				
				n2p.put(ecd.getClass().getCanonicalName(),
								new String("Port:"+ecd.getLocalPort()+"\n" ) );
				n2p.put(lm.getClass().getCanonicalName(),
								new String("Port:"+lm.getLocalPort()+"\n" ) );
				

				// Send the map back to each object
				ecd.setDriverMap(n2p);
				lm.setDriverMap(n2p);				
				ecd.setLogLevel(AUAVLEVEL);
				lm.setLogLevel(AUAVLEVEL);

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
				ecdT.start();
				Thread lmT = new Thread(lm);
				lmT.start();				
				
		}

		public static void main(String args[]) throws Exception {
				LinuxKernel k = new LinuxKernel();
		}
}
