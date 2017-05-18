package org.reroutlab.code.auav.drivers;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.HashMap;
public interface AuavDrivers {

		public int getLocalPort();
		public String getUsageInfo();
		public void setDriverMap(HashMap<String,String> m);
		public void setLogLevel(Level l);
}
