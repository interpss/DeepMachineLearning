 /*
  * @(#)IEEECDFTrainCaseBuilder.java   
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
package org.interpss.service.train.impl;

import java.util.Random;

import org.interpss.numeric.datatype.ComplexFunc;
import org.interpss.numeric.datatype.Unit.UnitType;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

/**
 * Load bus P,Q are modified to create training cases
 *     
 *     type      input          output
 *     swing   Vang, Vmag     Q,    P
 *     pv      P,    Vmag     Q,    Vang
 *     pq      P,    Q        Vmag, Vang
 * 
 */  

public class IEEECDFTrainCaseBuilder extends BaseTrainCaseBuilder {
	private double[] baseCaseData;
	
	public IEEECDFTrainCaseBuilder(AclfNetwork net) {
		this.aclfNet = net;
		this.noBus = aclfNet.getNoActiveBus();
		
		this.baseCaseData = new double[2*this.noBus];	
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (bus.isGen()) {
					bus.getGenPQ();
					bus.getContributeGenList().clear();
				}
				
				if (!bus.isSwing() && !bus.isGenPV()) { 
					this.baseCaseData[i] = bus.getLoadP();
					this.baseCaseData[this.noBus+i] = bus.getLoadQ();
					bus.getContributeLoadList().clear();
				}
				i++;
			}
		}
		
		//System.out.println(this.runLF());
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTrainCase()
	 */
	@Override
	public void createTrainCase(int nth, int nTotal) {
		//double factor = 0.5 + new Random().nextFloat();
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
	
	private void createCase(double factor) {
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (!bus.isSwing() && !bus.isGenPV()) {  
					bus.setLoadP(this.baseCaseData[i] * factor);
					bus.setLoadQ(this.baseCaseData[this.noBus+i] * factor);
				}
				i++;
			}
		}
		System.out.println("Total system load: " + ComplexFunc.toStr(aclfNet.totalLoad(UnitType.PU)) +
						   ", factor: " + factor);
		
		//System.out.println(aclfNet.net2String());
		
		String result = this.runLF();
		//System.out.println(result);
	}
	
}
