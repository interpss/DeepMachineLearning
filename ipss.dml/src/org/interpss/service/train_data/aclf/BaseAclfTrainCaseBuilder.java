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
package org.interpss.service.train_data.aclf;

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
	
	protected HashMap<String,Integer> busId2NoMapping;
	protected HashMap<String,Integer> branchId2NoMapping;
	
	protected double[] getNetInputPQ() {
		double[] input = new double[2*this.noBus];
		
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (this.busId2NoMapping != null) 
					i = this.busId2NoMapping.get(bus.getId());
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
				if ( this.busId2NoMapping != null ) 
					i = this.busId2NoMapping.get(bus.getId());
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
				if ( this.branchId2NoMapping != null ) 
					i = this.branchId2NoMapping.get(branch.getId());
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
				if ( this.busId2NoMapping != null ) 
					i = this.busId2NoMapping.get(bus.getId());
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
		  	//System.out.println(rntStr);
		  	/*
		  	int cnt = 0;
			for (AclfBranch branch : aclfNet.getBranchList()) {
				if (branch.isActive()) {
					System.out.println(branch.getId() + " " + cnt++);
				}
			}
			*/
		} catch ( InterpssException e) {
			e.printStackTrace();
			rntStr = "Error in LF calculation";
		}
		
		return rntStr;
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.train_data.ITrainCaseBuilder#createBusId2NoMapping(java.lang.String)
	 */
	@Override
	public void createBusId2NoMapping(String filename) {
		this.busId2NoMapping = new HashMap<>();
		loadFile(filename, line -> {
			// Bus1 0
			String[] strAry = line.split(" ");
			this.busId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
		// in the case of is mapping file, NN model size is determined by the info in the file
		this.noBus = this.busId2NoMapping.size();
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.train_data.ITrainCaseBuilder#createBranchId2NoMapping(java.lang.String)
	 */
	@Override
	public void createBranchId2NoMapping(String filename) {
		this.branchId2NoMapping = new HashMap<>();
		loadFile(filename, line -> {
			// Bus1->Bus2(1) 0
			String[] strAry = line.split(" ");
			this.branchId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
		// in the case of is mapping file, NN model size is determined by the info in the file
		this.noBranch = this.branchId2NoMapping.size();
	}
	
	private void loadFile(String filename, Consumer<String> processor) {
		try (Stream<String> stream = Files.lines(Paths.get(filename))) {
			stream.forEach(processor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
