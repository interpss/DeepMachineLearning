package org.interpss.service.train_data.singleNet.aclf.load_change;

import java.util.Random;

import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.service.train_data.ITrainCaseBuilder.BusData;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.adpter.AclfPVGenBus;
import com.interpss.core.aclf.adpter.AclfSwingBus;

public class BusVoltLoadChangeRandomTrainCaseBuilder extends BusVoltLoadChangeTrainCaseBuilder {
	/**
	 * The bus load is scaled by the scaling factor
	 * 
	 * @param factor the scaling factor
	 */
	@Override
	public void createTestCase() {
		int i = 0;
		double dp = 0;
		for (AclfBus bus : getAclfNet().getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null )
					i = this.busId2NoMapping.get(bus.getId());				
				if (!bus.isSwing() && !bus.isGenPV()) { 
					double factor= 2*new Random().nextFloat();
					bus.setLoadP(this.baseCaseData[i].loadP * factor);
					bus.setLoadQ(this.baseCaseData[i].loadQ * factor);
					dp +=bus.getLoadP()-this.baseCaseData[i].loadP;
					System.out.println("Bus id :"+bus.getId()+ 
							   ", load factor: " + factor);
				}
				i++;
			}
		}
		AclfBus bus = getAclfNet().getBus("Bus2");
		bus.setGenP(dp + bus.getGenP()*new Random().nextFloat());
//		System.out.println("Total system load: " + ComplexFunc.toStr(getAclfNet().totalLoad(UnitType.PU)) +
//						   ", factor: " + factor);
		
		//System.out.println(aclfNet.net2String());
		
		String result = this.runLF(this.getAclfNet());
	}
	
	@Override
	public void createTrainCase(int nth, int nTotal) {
		createTestCase();
	}
	
	@Override
	public double[] getNetInput() {
		double[] input = new double[2*this.noBus];
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (this.busId2NoMapping != null) 
					i = this.busId2NoMapping.get(bus.getId());
				BusData busdata = this.baseCaseData[i];
				if (busdata.isSwing() /*bus.isSwing()*/) {  // Swing Bus
//					AclfSwingBus swing = bus.toSwingBus();
//					input[2*i] = swing.getDesiredVoltAng(UnitType.Rad);
//					input[2*i+1] = swing.getDesiredVoltMag(UnitType.PU);
				}
				else if (busdata.isPV() /*bus.isGenPV()*/) {  // PV bus
//					AclfPVGenBus pv = bus.toPVBus();
					input[2*i] = bus.getGenP();
					input[2*i+1] = bus.getGenP()*bus.getGenP();
				}
				else {
					input[2*i] = bus.getLoadP();
					input[2*i+1] = bus.getLoadP()*bus.getLoadP();
				}
				i++;
			}
		}
		return input;
	}
	
}
