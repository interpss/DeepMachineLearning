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
	 * set AclfNetwork object and configure the builder
	 *  
	 * @param net the AclfNetwork object to be set
	 */
	void setAclfNetConfig(AclfNetwork net);
	
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
	 * create a new test case
	 */
	void createTestCase();

	/**
	 * create a new test case based on a factor value
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