 /*
  * @(#)TrainDataBuilderFactory.java   
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

import org.interpss.service.train_data.aclf.load_change.BranchContingencyMaxPLoadChangeTrainCaseBuilder;
import org.interpss.service.train_data.aclf.load_change.BranchPLoadChangeTrainCaseBuilder;
import org.interpss.service.train_data.aclf.load_change.BusVoltLoadChangeTrainCaseBuilder;

import com.interpss.core.aclf.AclfNetwork;

/**
 * Training data builder factory
 * 
 * @author Mike
 *
 */
public class TrainDataBuilderFactory {
	/**
	 * create a training data builder object
	 * 
	 * @param aclfNet AclfNetwork object
	 * @param buidlername training case builder class name
	 * @return the builder object
	 */
	public static ITrainCaseBuilder createITrainCaseBuilder(String builderName) {
		if (builderName.equals("BranchPLoadChangeTrainCaseBuilder"))
			return new BranchPLoadChangeTrainCaseBuilder();
		if (builderName.equals("BranchContingencyMaxPLoadChangeTrainCaseBuilder"))
			return new BranchContingencyMaxPLoadChangeTrainCaseBuilder();
		else
			return new BusVoltLoadChangeTrainCaseBuilder();
	}
}
 