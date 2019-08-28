package org.interpss.service.train_data.singleNet.aclf.load_change;

import java.util.Random;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.ComplexFunc;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.service.train_data.impl.BaseAclfTrainCaseBuilder;

import com.interpss.core.aclf.AclfBus;

/**
* @author JeremyChenlk
* @version 2018年12月27日 下午12:59:49
*
* Class description:
*	
*/

public class VThCaseBuilder extends BaseAclfTrainCaseBuilder {

	private static double BASE_VOLTAGE = 0.8;
	
	public VThCaseBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTrainCase()
	 */
	@Override
	public void createTrainCase(int nth, int nTotal) {
		/*
		 * We scale the bus load (P,Q) by a factor in the 
		 * range BASE_VOLTAGE + [0, 0.4]
		 */
		double factor = BASE_VOLTAGE + 0.4 * nth/(float)nTotal;

		createCase(factor);
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTestCase()
	 */
	@Override
	public void createTestCase() {
		createCase(BASE_VOLTAGE + new Random().nextFloat());
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#createTestCase()
	 */
	@Override
	public void createTestCase(double factor) {
		createCase(factor);
	}
	
	@Override
	public double[] getNetInput() {
		return this.getNetOutputVoltage(this.getAclfNet());
	}

	@Override
	public double[] getNetOutput() {
		return this.getNetInputPQ(this.getAclfNet());
	}

	/**
	 * The bus voltage is scaled by the scaling factor
	 * 
	 * @param factor the scaling factor
	 */
	private void createCase(double factor) {
		
		// gen new voltage and set into aclfNet
		int i = 0;
		for (AclfBus bus : getAclfNet().getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null )
					i = this.busId2NoMapping.get(bus.getId());				
				if (!bus.isSwing() && !bus.isGenPV()) {  
					bus.setVoltage(this.baseCaseData[i].voltage.multiply(factor));
				}
				i++;
			}
		}
		
		// get PowerIntoNet
		i = 0;
		Complex[] power = new Complex[noBus];
		for (AclfBus bus : getAclfNet().getBusList()) {
			if (bus.isActive()) {			 
				power[i] = bus.powerIntoNet();
				i++;
			}
		}
		
		// set power
		i = 0;
		for (AclfBus bus : getAclfNet().getBusList()) {
			if (bus.isActive()) {
				if (bus.isSwing()){
					bus.setGenP(power[i].getReal() + bus.getLoadP());
					bus.setGenQ(power[i].getImaginary() + bus.getLoadQ());
				}else if (bus.isGenPV()) {
					bus.setGenP(power[i].getReal() + bus.getLoadP());
					bus.setGenQ(power[i].getImaginary() + bus.getLoadQ());
				}else {
					bus.setGenP(power[i].getReal() + bus.getLoadP());
					bus.setGenQ(power[i].getImaginary() + bus.getLoadQ());
//					bus.setLoadP(bus.getGenP() - power[i].getReal());
//					bus.setLoadQ(bus.getGenQ() - power[i].getImaginary());
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
