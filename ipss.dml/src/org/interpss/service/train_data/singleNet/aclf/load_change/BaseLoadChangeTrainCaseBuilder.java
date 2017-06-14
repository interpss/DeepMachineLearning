 /*
  * @(#)BaseLoadChangeTrainCaseBuilder.java   
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

import java.util.Random;

import org.interpss.numeric.datatype.ComplexFunc;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.service.train_data.impl.BaseAclfTrainCaseBuilder;

import com.interpss.core.aclf.AclfBus;
/**
 * Base class for creating training case for : Load bus P,Q are modified to create training cases
 * 
 */  

public abstract class BaseLoadChangeTrainCaseBuilder extends BaseAclfTrainCaseBuilder {
	public BaseLoadChangeTrainCaseBuilder() {
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTrainCase()
	 */
	@Override
	public void createTrainCase(int nth, int nTotal) {
		//double factor = 0.5 + new Random().nextFloat();
		/*
		 * We scale the bus load (P,Q) by a factor in the 
		 * range [0.5, 1.5]
		 */
		double factor = 0.5 + nth/(float)nTotal;

		createCase(factor);
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTestCase()
	 */
	@Override
	public void createTestCase() {
		createCase(0.5 + new Random().nextFloat());
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTestCase()
	 */
	@Override
	public void createTestCase(double factor) {
		createCase(factor);
	}	
	
	/**
	 * The bus load is scaled by the scaling factor
	 * 
	 * @param factor the scaling factor
	 */
	private void createCase(double factor) {
		
		int i = 0;
		for (AclfBus bus : getAclfNet().getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null )
					i = this.busId2NoMapping.get(bus.getId());				
				if (!bus.isSwing() && !bus.isGenPV()) {  
					bus.setLoadP(this.baseCaseData[i].loadP * factor);
					bus.setLoadQ(this.baseCaseData[i].loadQ * factor);
				}
				i++;
			}
		}
		System.out.println("Total system load: " + ComplexFunc.toStr(getAclfNet().totalLoad(UnitType.PU)) +
						   ", factor: " + factor);
		
		//System.out.println(aclfNet.net2String());
		
		String result = this.runLF(this.getAclfNet());
		//System.out.println(result);
	}
}
