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

import static org.interpss.pssl.plugin.IpssAdapter.FileFormat.IEEECommonFormat;

import org.interpss.IpssCorePlugin;
import org.interpss.pssl.plugin.IpssAdapter;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.TrainDataBuilderFactory;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;

import py4j.GatewayServer;

/**
 * InterPSS AC Loadflow training data service gateway
 * 
 * @author Mike
 *
 */ 
public class AclfPyGateway {
	private ITrainCaseBuilder trainCaseBuilder;
	
	/**
	 * Load a loadflow case in IEEE CMD format and create the TrainCaseBuilder object
	 * 
	 * @param filename
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java
	 * @return an int[2] array, [no of active buses, no of active branches] in the network
	 */
	public int[] loadCase(String filename, String buildername) {
		return loadCase(filename, buildername, null, null);
	}

	/**
	 * Load a loadflow case in IEEE CMD format and create the TrainCaseBuilder object
	 * 
	 * @param filename
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java
	 * @param busIdMappingFile
	 * @param branchIdMappingFile
	 * @return an int[2] array, [no of active buses, no of active branches] in the network
	 */	
	public int[] loadCase(String filename, String buildername, String busIdMappingFile, String branchIdMappingFile) {
		IpssCorePlugin.init();
		
		try {
			AclfNetwork aclfNet = IpssAdapter.importAclfNet(filename)
					.setFormat(IEEECommonFormat)
					.load()
					.getImportedObj();
			
			this.trainCaseBuilder = TrainDataBuilderFactory.createITrainCaseBuilder(buildername);
			
			if (busIdMappingFile != null)
				this.trainCaseBuilder.createBusId2NoMapping(busIdMappingFile);
			if (branchIdMappingFile != null)
				this.trainCaseBuilder.createBranchId2NoMapping(branchIdMappingFile);
			
			this.trainCaseBuilder.setAclfNet(aclfNet);
			System.out.println(filename + " aclfNet case loaded");
		} catch ( InterpssException e) {
			e.printStackTrace();
			return new int[] {0, 0};
		}		
	
		return new int[] {this.trainCaseBuilder.getNoBus(), this.trainCaseBuilder.getNoBranch()};
	}
	
	/**
	 * create and return a set of training cases, 
	 * 
	 *   Data format: [2][points][]
	 *       [
	 *         [input, output], ... [input,output ]
	 *       ]
	 * 
	 * input/output is a string of "x1 x2 ...", representing
	 * a double[] for the large-scale array performance reason. 
	 *                
	 * @param points number of training cases
	 * @return the training set
	 */
	public String[][] getTrainSet(int points) {
		String[][] trainSet = new String[2][points];
		for (int i = 0; i < points; i++) {
			this.trainCaseBuilder.createTrainCase(i, points);
			double[] input = this.trainCaseBuilder.getNetInput();
			double[] output = this.trainCaseBuilder.getNetOutput();
			trainSet[0][i] = this.array2String(input);
			trainSet[1][i] = this.array2String(output);
		}
		return trainSet;
	}

	/**
	 * create and return a random test case, 
	 * 
	 *   Data format: [2][]
	 *              [
	 *                 input, output
	 *              ]
	 *                
	 *	input/output is a string of "x1 x2 ...", representing
	 *  a double[] for the large-scale array performance reason.                        

	 * @return the training set
	 */
	public String[][] getTestCase() {
		String [][] data = new String[2][1];
		
		this.trainCaseBuilder.createTestCase();
		double[] input = this.trainCaseBuilder.getNetInput();
		double[] output = this.trainCaseBuilder.getNetOutput();	
		data[0][0] = this.array2String(input);
		data[1][0] = this.array2String(output);
		
		return data;
	}	
	
	/**
	 * create and return a test case using the factor to generate the case,
	 *   
	 *   Data format: [2][]
	 *              [
	 *                input, output
	 *              ]  

	 *	input/output is a string of "x1 x2 ...", representing
	 *  a double[] for the large-scale array performance reason.                        
	 *                     
	 * @param factor some value for creating the test case
	 * @return the training set
	 */
	public String[][] getTestCase(double factor) {
		String [][] data = new String[2][1];
		
		this.trainCaseBuilder.createTestCase(factor);
			
		double[] input = this.trainCaseBuilder.getNetInput();
		double[] output = this.trainCaseBuilder.getNetOutput();	
		data[0][0] = this.array2String(input);
		data[1][0] = this.array2String(output);
		
		return data;
	}	
	
	private String array2String(double[] array){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(String.valueOf(array[i]) + " ");
		}
		return sb.toString();
		
	}
	/**
	 * compute and return the mismatch info based on the network solution 
	 * for bus voltage
	 * 
	 * @param netVolt network bus voltage solution
	 * @return mismatch info string
	 */
	public String getMismatchInfo(double[] netVolt) {
		return this.trainCaseBuilder.calMismatch(netVolt).toString();
	}	
	
	/**
	 * main method to start the service
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		AclfPyGateway app = new AclfPyGateway();
		// app is now the gateway.entry_point
		GatewayServer server = new GatewayServer(app);
		System.out.println("Starting Py4J " + app.getClass().getTypeName() + " ...");
		server.start();
	}	
}
