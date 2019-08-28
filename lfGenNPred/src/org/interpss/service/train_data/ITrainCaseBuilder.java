 /*
  * @(#)ITrainCaseBuilder.java   
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

package org.interpss.service.train_data;

import org.apache.commons.math3.complex.Complex;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.datatype.Mismatch;

/**
 * A training case builder interface
 * 
 * @author Mike
 *
 */ 
public interface ITrainCaseBuilder {
	/**
	 * Bus data structure to hold cached base case bus data
	 *
	 */
	public static class BusData {
		public final static int Swing = 0;
		public final static int PV = 1;
		public final static int PQ = 2;
		
		public String id;
		public double loadP = 0.0, loadQ = 0.0;
		public double genP = 0.0;
		public int type = PQ;

		//Jeremy add for VThCaseBuilder.class
		public Complex voltage;
		
		public boolean isSwing() { return this.type == Swing; }
		public boolean isPV() { return this.type == PV; }
		public boolean isPQ() { return this.type == PQ; }
		
		@Override public String toString() { return "BusData: " + id + ", " + type + ", " + loadP + ", " + loadQ;}
	}
	
	/**
	 * get the AclfNetwork object 
	 * 
	 * @return
	 */
	AclfNetwork getAclfNet();
	
	/**
	 * load an AclfNetwork object from a file and configure the builder
	 *  
	 * @param filename the filename
	 */
	void loadConfigureAclfNet(String filename) throws InterpssException;
	
	/**
	 * get the cached based case bus data
	 * 
	 * @return the baseCaseData
	 */
	BusData[] getBaseCaseData();
	
	/**
	 * create BusId to model array number mapping relationship 
	 * by loading the mapping info stored in the mapping file
	 * 
	 * @param filename BusId to model array number mapping filename
	 */
	void createBusId2NoMapping(String filename);
	
	/**
	 * create BranchId to model array number mapping relationship 
	 * by loading the mapping info stored in the mapping file
	 * 
	 * @param filename BranchId to model array number mapping filename
	 */
	void createBranchId2NoMapping(String filename);
	
	/**
	 * get number of buses in the power network model
	 * 
	 * @return the noBus
	 */
	int getNoBus();
	
	/**
	 * get number of branches in the power network model
	 * 
	 * @return the noBranch
	 */
	int getNoBranch();

	/**
	 * create a new training case, nth case out of nTotal cases
	 * 
	 * @param nth case number
	 * @param nTotal total number of cases to be created
	 */
	void createTrainCase(int nth, int nTotal);

	/**
	 * create a new test case for evaluating the model accuracy
	 */
	void createTestCase();

	/**
	 * create a new test case based on a factor value for evaluating the model accuracy
	 * 
	 * @param factor a number to be used in the case creation
	 */
	void createTestCase(double factor);
	
	/**
	 * get input data of the current training case
	 * 
	 * @return training input, for example, [P/Vang, Q/Vmag]
	 */
	double[] getNetInput();

	/**
	 * get output data of the current training case
	 *  
	 * @return training output, for example, [Vmag/Q, Vang/P]
	 */
	double[] getNetOutput();
	
	/**
	 * compute the network mismatch information
	 *  
	 * @param netVolt network bus voltage solution
	 * @return the mismatch object
	 */
	Mismatch calMismatch(double[] netVolt);
}