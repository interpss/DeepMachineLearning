 /*
  * @(#)BaseAclfTrainCaseBuilder.java   
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
package org.interpss.service.train_data.aclf;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFunction;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.pssl.simu.IpssAclf;
import org.interpss.service.train_data.ITrainCaseBuilder;

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
 * for predicting bus voltage:
 *     
 *     type      input          output
 *     swing   Vang, Vmag     Q,    P
 *     pv      P,    Vmag     Q,    Vang
 *     pq      P,    Q        Vmag, Vang
 * 
 */ 
 
public abstract class BaseAclfTrainCaseBuilder implements ITrainCaseBuilder {
	protected AclfNetwork aclfNet;
	protected int noBus;
	protected int noBranch;
	
	protected double[] getNetInputPQ() {
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

	protected double[] getNetOutputVoltage() {
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

	protected double[] getNetBranchP() {
		double[] output = new double[this.noBranch];
		
		int i = 0;
		for (AclfBranch branch : aclfNet.getBranchList()) {
			if (branch.isActive()) {
				output[i] = branch.powerFrom2To().getReal();
				i++;
			}
		}
		return output;
	}
	
	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#calMismatch()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.interpss.service.ITrainCaseBuilder#getNoBranch()
	 */
	@Override
	public int getNoBranch() {
		return noBranch;
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
