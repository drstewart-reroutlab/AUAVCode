package org.reroutlab.code.auav.routines;

import java.util.HashMap;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;


public class BasicRoutine extends org.reroutlab.code.auav.routines.AuavRoutines{
		Thread t = null;
		String resp="";
		String csLock = "free";

		public BasicRoutine() {
				t = new Thread (this, "Main Thread");
				
		}

		public void main(String args[]) {
		}
		public String startRoutine() {

				if (t != null) {
						t.start();
						return "BasicRoutine Started";
				}
				return "BasicRoutine not Initialized";

		}
		
		public String stopRoutine() {
				return "BasicRoutine Stopped";
		}

		synchronized void setLock(String value) {
				csLock = value;
		}
		
		public void run() {
				CoapHandler ch = new CoapHandler() {
						@Override public void onLoad(CoapResponse response) {
								resp = response.getResponseText();
								setLock("barrier-1");
						}
						
						@Override public void onError() {
								System.err.println("FAILED");
								setLock("barrier-1");
						}};
				
				String succ = invokeDriver("org.reroutlab.code.auav.drivers.FlyDroneDriver",
																	 "dc=lft", ch );

				while (csLock.equals("barrier-1") == false) {
						try { Thread.sleep(1000); }
						catch (Exception e) {}
				}

				setLock("Free");
				succ = invokeDriver("org.reroutlab.code.auav.drivers.DroneGimbalDriver",
																	 "dc=cal", ch );				
				while (csLock.equals("barrier-1") == false) {
						try { Thread.sleep(1000); }
						catch (Exception e) {}
				}
				System.out.println("BasicRoutine: " + resp);
				
				setLock("Free");
				succ = invokeDriver("org.reroutlab.code.auav.drivers.FlyDroneDriver",
																	 "dc=lnd", ch );
				while (csLock.equals("barrier-1") == false) {
						try { Thread.sleep(1000); }
						catch (Exception e) {}
				}				
				System.out.println("BasicRoutine: " + resp);				
				


		}
		
}
