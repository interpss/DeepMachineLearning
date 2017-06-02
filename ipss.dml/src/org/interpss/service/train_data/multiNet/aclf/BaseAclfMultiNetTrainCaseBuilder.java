 /*
  * @(#)BaseAclfTrainCaseBuilder.java   
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
package org.interpss.service.train_data.multiNet.aclf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFunction;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.pssl.simu.IpssAclf;
import org.interpss.service.train_data.BaseAclfTrainCaseBuilder;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.aclf.adpter.AclfPVGenBus;
import com.interpss.core.aclf.adpter.AclfSwingBus;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.datatype.Mismatch;

/**
 * Base class for implementing Aclf training case creation builder.
 *     
 * 
 */ 
 
public abstract class BaseAclfMultiNetTrainCaseBuilder extends BaseAclfTrainCaseBuilder {
	protected AclfNetwork aclfNet;
	
	protected String[] filenames;

	public BaseAclfMultiNetTrainCaseBuilder(String[] names) {
		this.filenames = names;
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#calMismatch()
	 */
	@Override
	public Mismatch calMismatch(double[] netVolt) {
		return calMismatch(netVolt, this.getAclfNet());
	};
	
	public AclfNetwork getAclfNet() {
		return this.aclfNet;
	}

	public void setAclfNet(AclfNetwork aclfNet) {
		this.aclfNet = aclfNet;
	}
}
