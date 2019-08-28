 /*
  * @(#)AclfPyGateway.java   
  *
  * Copyright (C) 2005-17 www.interpss.org
  *
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0
    
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
  *
  * @Author Mike Zhou
  * @Version 1.0
  * @Date 04/7/2017
  * 
  *   Revision History
  *   ================
  *
  */

package org.interpss.service;

import py4j.GatewayServer;

/**
 * InterPSS AC Loadflow training data service gateway
 * 
 * @author Mike
 *
 */ 
public class Py4JGateway {
	/**
	 * main method to start the service
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		AclfTrainDataGenerator app = new AclfTrainDataGenerator();
		// app is now the gateway.entry_point
		GatewayServer server = new GatewayServer(app);
		System.out.println("Starting Py4J " + app.getClass().getTypeName() + " ...");
		server.start();
	}	
}
