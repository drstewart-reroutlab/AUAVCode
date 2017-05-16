package org.reroutlab.code.auav.drivers.ExternalCommandsDriver;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.System.*;
import org.reroutlab.code.auav.interfaces.*;

/**
 * org.reroutlab.code.ExternalCommands is an AUAV service.
 * The service accepts commands that could go to *any* module.
 * It also supports a "list" command that tells of all available modules.
 * It also supports a "help" command that explains syntax.
 *
 * @author  Christopher Stewart
 * @version 0.01
 * @since   2017-05-01 
 */

public class ExternalCommandsDriver implements Runnable,org.reroutlab.code.auav.interfaces.AuavDrivers {
		private ServerSocket serverSocket;
		public int getLocalPort() {
				if (serverSocket == null) {
						return -1;
				}
				return serverSocket.getLocalPort();
		}
		
		private String usageInfo="\nCMD param=value param=value param=value\nlist ";
		public String getUsageInfo() {
				return usageInfo;
		}

		private HashMap driver2port;  // key=drivername value={port,usageInfo}
		public void setDriverMap(HashMap m) {
				if (m != null) {
						driver2port = m;
				}
		}

		
		public ExternalCommandsDriver() {
				System.out.println("In Constructor");
				try {
						serverSocket = new ServerSocket(0);
				} catch (Exception e){
						System.out.println("Problem: " + e.toString() );
				}
				
		}

		public void run() {
				BufferedReader bufferedReader;
				while (true) {
						Socket clientSocket=null;
						try {
								clientSocket = serverSocket.accept();
								bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
								String inputLine = bufferedReader.readLine();
						}
						catch (Exception e) {
								System.out.println("Problem: " + e.toString() );
						}
						try {
								clientSocket.close();
						} catch (Exception e ){}
				}
		}
		
		public static void main(String[] args) {
				ExternalCommandsDriver ecd = new ExternalCommandsDriver();
				System.out.println("LocalPort: " + ecd.getLocalPort() );
				
		}
}

