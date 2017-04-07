package org.interpss.service.train.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFunction;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.pssl.simu.IpssAclf;
import org.interpss.service.train.ITrainCaseBuilder;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.aclf.adpter.AclfPVGenBus;
import com.interpss.core.aclf.adpter.AclfSwingBus;
import com.interpss.core.algo.AclfMethod;
import com.interpss.core.datatype.Mismatch;

/**
 * Base class for implementing training case creation builder.
 *     
 *     type      input          output
 *     swing   Vang, Vmag     Q,    P
 *     pv      P,    Vmag     Q,    Vang
 *     pq      P,    Q        Vmag, Vang
 * 
 */ 
 
public abstract class BaseTrainCaseBuilder implements ITrainCaseBuilder {
	protected AclfNetwork aclfNet;
	protected int noBus;
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getNetInputPQ()
	 */
	@Override
	public double[] getNetInputPQ() {
		double[] input = new double[2*this.noBus];
		
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (bus.isSwing()) {  // Swing Bus
					AclfSwingBus swing = bus.toSwingBus();
					input[i] = swing.getDesiredVoltAng(UnitType.Rad);
					input[this.noBus+i] = swing.getDesiredVoltMag(UnitType.PU);
				}
				else if (bus.isGenPV()) {  // PV bus
					AclfPVGenBus pv = bus.toPVBus();
					input[i] = bus.getGenP() - bus.getLoadP();
					input[this.noBus+i] = pv.getDesiredVoltMag();
				}
				else {
					input[i] = bus.getGenP() - bus.getLoadP();
					input[this.noBus+i] = bus.getGenQ() - bus.getLoadQ();
				}
				i++;
			}
		}
		return input;
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getNetOutputVolt()
	 */
	@Override
	public double[] getNetOutputVolt() {
		double[] output = new double[2*this.noBus];
		
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (bus.isSwing()) {  // Swing Bus
					AclfSwingBus swing = bus.toSwingBus();
					Complex gen = swing.getGenResults(UnitType.PU);
					output[i] = gen.getImaginary();
					output[this.noBus+i] = gen.getReal();
				}
				else if (bus.isGenPV()) {  // PV bus
					AclfPVGenBus pv = bus.toPVBus();
					Complex gen = pv.getGenResults(UnitType.PU);
					output[i] = gen.getImaginary() - bus.getLoadQ();;
					output[this.noBus+i] = bus.getVoltageAng();
				}
				else {
					output[i] = bus.getVoltageMag();
					output[this.noBus+i] = bus.getVoltageAng();
				}				
				i++;
			}
		}
		return output;
	}
	
	@Override
	public String getMismatchInfo(double[] netVolt) {
		return calMismatch(netVolt).toString();
	}
	
	public Mismatch calMismatch(double[] netVolt) {
		
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (bus.isSwing()) {  // Swing Bus
					//AclfSwingBus swing = bus.toSwingBus();
					//Complex gen = swing.getGenResults(UnitType.PU);
					//output[i] = gen.getImaginary();
					//output[this.noBus+i] = gen.getReal();
				}
				else if (bus.isGenPV()) {  // PV bus
					//AclfPVGenBus pv = bus.toPVBus();
					//Complex gen = pv.getGenResults(UnitType.PU);
					//output[i] = gen.getImaginary() - bus.getLoadQ();;
					bus.setVoltageAng(netVolt[this.noBus+i]);
				}
				else {
					bus.setVoltageMag(netVolt[i]);
					bus.setVoltageAng(netVolt[this.noBus+i]);
				}		
				i++;
			}
		}
		
		return this.aclfNet.maxMismatch(AclfMethod.NR);
	};
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getAclfNet()
	 */
	@Override
	public AclfNetwork getAclfNet() {
		return aclfNet;
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getNoBus()
	 */
	@Override
	public int getNoBus() {
		return noBus;
	}

	/**
	 * Run Loadlow
	 * 
	 * @return
	 */
	protected String runLF() {
		String rntStr = "";
		try {
		  	IpssAclf.createAclfAlgo(this.aclfNet)
  					.lfMethod(AclfMethod.NR)
  					.nonDivergent(true)
  					.runLoadflow();	
		  	
		  	System.out.println("Run Aclf " + (this.aclfNet.isLfConverged()? " converged, " : " diverged, ") 
		  			+ this.aclfNet.maxMismatch(AclfMethod.NR).toString());
		  	
		  	rntStr = CorePluginFunction.aclfResultSummary.apply(this.aclfNet).toString();
		} catch ( InterpssException e) {
			e.printStackTrace();
			rntStr = "Error in LF calculation";
		}
		
		return rntStr;
	}	
}
