package org.reroutlab.code.auav.routines;


import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.CoapHandler;


abstract public class AuavRoutines implements Runnable {
		HashMap<String, String> d2p = null;
		final int TEXT_PLAIN=0;
		
		public String invokeDriver(String dn, String params, CoapHandler ch) {
				if (d2p == null) {
						try {
								URI uri = new URI("coap://127.0.0.1:5117/cr");
								CoapClient client = new CoapClient(uri);
								
								CoapResponse response = client.put("dn=list",TEXT_PLAIN);
								String ls = response.getResponseText();
								d2p = new HashMap<String,String>();
								String[] lines = ls.split("\n");
								for (int x=0;x<lines.length; x++) {
										String[] data = lines[x].split("-->");
										if (data.length == 2) {
												d2p.put(data[0].trim(),data[1].trim());
										}
								}
								
						}
						catch (Exception e) {
								d2p = null;
								System.out.println("AUAVRoutine invokeDriver error");
								e.printStackTrace();
								return "Invoke Error";
						}
				}
				if (d2p != null) {
						String portStr = d2p.get(dn);
						if (portStr != null) {
								try {
										URI uri = new URI("coap://127.0.0.1:"+portStr+"/cr");
										CoapClient client = new CoapClient(uri);
								
										client.put(ch,params,TEXT_PLAIN);
										return("Success");
										//return(response.getResponseText());
								}
								catch (Exception e) {
										return("Unable to reach driver " + dn + "  at port: " + portStr);
								}
						}
						else {
								return ("Unable to find driver: "+ dn);
						}
				}
				return("InvokeDriver: Unreachable code touched");
		}
		
		abstract public String startRoutine();
		abstract public String stopRoutine();		
}
