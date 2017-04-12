 /*
  * @(#)AclfPyGateway.java   
  *
  * Copyright (C) 2005-17 www.interpss.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
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
import org.interpss.service.train.ITrainCaseBuilder;
import org.interpss.service.train.TrainDataBuilderFactory;

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
