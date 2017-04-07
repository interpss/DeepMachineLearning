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

public class TrainCaseBuilder extends BaseTrainCaseBuilder {
	private double[] baseCaseData;
	
	public TrainCaseBuilder(AclfNetwork net) {
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
	 * @see org.interpss.service.ITrainCaseBuilder#createNextTrainCase()
	 */
	@Override
	public void createTrainCase() {
		double factor = new Random().nextFloat();
		if (factor < 0.5)
			factor += 0.5;
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
