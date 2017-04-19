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
	 * Load a loadflow case and create the TrainCaseBuilder object
	 * 
	 * @param filename
	 * @param buildername training set builder Java class name
	 * @return [no of active buses, no of active branches] in the network
	 */
	public int[] loadCase(String filename, String buildername) {
		IpssCorePlugin.init();
		
		try {
			AclfNetwork aclfNet = IpssAdapter.importAclfNet(filename)
					.setFormat(IEEECommonFormat)
					.load()
					.getImportedObj();
			
			this.trainCaseBuilder = TrainDataBuilderFactory.createITrainCaseBuilder(aclfNet, buildername);
			System.out.println(filename + " aclfNet case loaded");
		} catch ( InterpssException e) {
			e.printStackTrace();
			return new int[] {0, 0};
		}

		return new int[] {this.trainCaseBuilder.getNoBus(), this.trainCaseBuilder.getNoBranch()};
	}

	/**
	 * create and return a set of training cases, 
	 *   Data format: [2][points][]
	 *       [
	 *         [[input], [output]], ... [[input],[output] ]
	 *       ]         
	 * @param points number of training cases
	 * @return the training set
	 */
	public double[][][] getTrainSet(int points) {
		double[][][] set = new double[2][points][];
		
		for ( int i = 0; i < points; i++) {
			this.trainCaseBuilder.createTrainCase(i, points);
			
			set[0][i] = this.trainCaseBuilder.getNetInput();
			set[1][i] = this.trainCaseBuilder.getNetOutput();
		}
		
		return set;
	}

	/**
	 * create and return a random test case, 
	 *   Data format: [2][]
	 *              [
	 *                 [input], [output]
	 *              ]         
	 * @return the training set
	 */
	public double[][] getTestCase() {
		double[][] data = new double[2][];
		
		this.trainCaseBuilder.createTestCase();
			
		data[0] = this.trainCaseBuilder.getNetInput();
		data[1] = this.trainCaseBuilder.getNetOutput();
		
		return data;
	}	
	
	/**
	 * create and return a test case, 
	 *   Data format: [2][]
	 *              [
	 *                [input], [output]
	 *              ]         
	 * @param factor some value for creating the test case
	 * @return the training set
	 */
	public double[][] getTestCase(double factor) {
		double[][] data = new double[2][];
		
		this.trainCaseBuilder.createTestCase(factor);
			
		data[0] = this.trainCaseBuilder.getNetInput();
		data[1] = this.trainCaseBuilder.getNetOutput();
		
		return data;
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
