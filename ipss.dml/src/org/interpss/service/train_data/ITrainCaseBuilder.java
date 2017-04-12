 /*
  * @(#)ITrainCaseBuilder.java   
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

package org.interpss.service.train_data;

import com.interpss.core.datatype.Mismatch;

/**
 * A training case builder interface
 * 
 * @author Mike
 *
 */ 
public interface ITrainCaseBuilder {
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