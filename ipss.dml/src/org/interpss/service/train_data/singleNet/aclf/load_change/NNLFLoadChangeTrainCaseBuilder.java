 /*
  * @(#)NNLFLoadChangeTrainCaseBuilder.java   
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
package org.interpss.service.train_data.singleNet.aclf.load_change;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.algo.AclfMethodType;
import com.interpss.core.datatype.Mismatch;

/**
 * This training case builder usses the NN-model Loadflow approach. 
 * Load bus P,Q are modified to create training cases for predicting bus voltage
 * 
 */  

public class NNLFLoadChangeTrainCaseBuilder extends BaseLoadChangeTrainCaseBuilder {
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getNetOutputVolt()
	 */
	@Override
	public double[] getNetOutput() {
		double[] output = new double[2*this.noBus];
	
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null ) 
					i = this.busId2NoMapping.get(bus.getId());
				output[i] = bus.getVoltageMag();
				output[this.noBus+i] = bus.getVoltageAng();
				i++;
			}
		}
		return output;
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getNetInputPQ()
	 */
	@Override
	public double[] getNetInput() {
		return this.getNetInputPQ(this.getAclfNet());
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#calMismatch()
	 */
	@Override
	public Mismatch calMismatch(double[] netVolt) {
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null ) 
					i = this.busId2NoMapping.get(bus.getId());
				bus.setVoltageMag(netVolt[i]);
				bus.setVoltageAng(netVolt[this.noBus+i]);
				i++;
			}
		}
		
		return aclfNet.maxMismatch(AclfMethodType.NN_MODEL);
	};	
}
