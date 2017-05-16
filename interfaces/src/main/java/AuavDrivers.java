package org.reroutlab.code.auav.interfaces;

import java.util.HashMap;
public interface AuavDrivers {

		public int getLocalPort();
		public String getUsageInfo();
		public void setDriverMap(HashMap m);
}
