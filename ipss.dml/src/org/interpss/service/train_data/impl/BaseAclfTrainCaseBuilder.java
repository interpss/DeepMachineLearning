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
package org.interpss.service.train_data.impl;

import static org.interpss.pssl.plugin.IpssAdapter.FileFormat.IEEECommonFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFunction;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.pssl.plugin.IpssAdapter;
import org.interpss.pssl.simu.IpssAclf;
import org.interpss.service.UtilFunction;
import org.interpss.service.pattern.NetOptPattern;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.ITrainCaseBuilder.BusData;

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
 */ 
 
public abstract class BaseAclfTrainCaseBuilder implements ITrainCaseBuilder {
	/** AclfNetwork object*/
	protected AclfNetwork aclfNet;
	
	/** NN model bus array dimension */
	protected int noBus;
	/** NN model branch array dimension */
	protected int noBranch;
	
	/** Bus id to NN model bus array index mapping */
	protected HashMap<String,Integer> busId2NoMapping;
	/** Branch id to NN model branch array index mapping */
	protected HashMap<String,Integer> branchId2NoMapping;
	
	/** cached base case data for creating training cases*/
	protected BusData[] baseCaseData;
	
	/**
	 * get the AclfNetwork object 
	 * 
	 * @return
	 */
	@Override
	public AclfNetwork getAclfNet() {
		return aclfNet;
	}
	
	/**
	 * get the cached based case bus data
	 * 
	 * @return the baseCaseData
	 */
	public BusData[] getBaseCaseData() {
		return baseCaseData;
	}

	/* (non-Javadoc)
	 * @see org.interpss.service.train_data.ITrainCaseBuilder#createTrainCase(int, int)
	 */
	@Override
	public void loadConfigureAclfNet(String filename) throws InterpssException {
		this.aclfNet = UtilFunction.loadAclfNetIEEECDF(filename);
		
		// set noBus/Branch in case the mapping relationships
		// are not defined
		if (this.busId2NoMapping == null)
			this.noBus = getAclfNet().getNoActiveBus();
		if (this.branchId2NoMapping == null)
			this.noBranch = getAclfNet().getNoActiveBranch();
		
		System.out.println(filename + " aclfNet case loaded, no buses/branches: " + this.noBus + ", " + this.noBranch);
		
		// cache the base case bus data 
		this.baseCaseData = new BusData[this.noBus];	
		for (int i = 0; i < this.noBus; i++ ) 
			this.baseCaseData[i] = new BusData();
		
		int i = 0;
		for (AclfBus bus : getAclfNet().getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null )
					i = this.busId2NoMapping.get(bus.getId());
				BusData busdata = this.baseCaseData[i];
				busdata.id = bus.getId();
				if (bus.isGen()) {
					bus.getGenPQ();
					bus.getContributeGenList().clear();
				}
				
				if (!bus.isSwing() && !bus.isGenPV()) { 
					busdata.loadP = bus.getLoadP();
					busdata.loadQ = bus.getLoadQ();
					bus.getContributeLoadList().clear();
				}
				else 
					busdata.type = bus.isSwing()? BusData.Swing : BusData.PV; 
				i++;
			}
		}
		
		//System.out.println(this.runLF());
	}
	
	protected double[] getNetInputPQ(AclfNetwork aclfNet) {
		double[] input = new double[2*this.noBus];
		
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if (this.busId2NoMapping != null) 
					i = this.busId2NoMapping.get(bus.getId());
				BusData busdata = this.baseCaseData[i];
				if (busdata.isSwing() /*bus.isSwing()*/) {  // Swing Bus
					AclfSwingBus swing = bus.toSwingBus();
					input[i] = swing.getDesiredVoltAng(UnitType.Rad);
					input[this.noBus+i] = swing.getDesiredVoltMag(UnitType.PU);
				}
				else if (busdata.isPV() /*bus.isGenPV()*/) {  // PV bus
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

	protected double[] getNetOutputVoltage(AclfNetwork aclfNet) {
		double[] output = new double[2*this.noBus];
		for (int i = 0; i < this.noBus; i++) {
			output[i] = 1.0;
			output[i+this.noBus] = 0.0;
		}
			
		
		int i = 0;
		for (AclfBus bus : aclfNet.getBusList()) {
			if (bus.isActive()) {
				if ( this.busId2NoMapping != null ) 
					i = this.busId2NoMapping.get(bus.getId());
				BusData busdata = this.baseCaseData[i];
				if (busdata.isSwing() /*bus.isSwing()*/) {  // Swing Bus
					AclfSwingBus swing = bus.toSwingBus();
					Complex gen = swing.getGenResults(UnitType.PU);
					output[i] = gen.getImaginary();
					output[this.noBus+i] = gen.getReal();
				}
				else if (busdata.isPV() /*bus.isGenPV()*/) {  // PV bus
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

	protected double[] getNetBranchP(AclfNetwork aclfNet) {
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
				BusData busdata = this.baseCaseData[i];
				if (busdata.isSwing() /*bus.isSwing()*/) {  // Swing Bus
					//AclfSwingBus swing = bus.toSwingBus();
					//Complex gen = swing.getGenResults(UnitType.PU);
					//output[i] = gen.getImaginary();
					//output[this.noBus+i] = gen.getReal();
				}
				else if (busdata.isPV() /*bus.isGenPV()*/) {  // PV bus
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
		
		return aclfNet.maxMismatch(AclfMethod.NR);
	};
	
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
	protected String runLF(AclfNetwork aclfNet) {
		String rntStr = "";
		try {
		  	IpssAclf.createAclfAlgo(aclfNet)
  					.lfMethod(AclfMethod.NR)
  					.nonDivergent(true)
  					.runLoadflow();	
		  	
		  	System.out.println("Run Aclf " + (aclfNet.isLfConverged()? " converged, " : " diverged, ") 
		  			+ aclfNet.maxMismatch(AclfMethod.NR).toString());
		  	
		  	rntStr = CorePluginFunction.aclfResultSummary.apply(aclfNet).toString();
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
		loadTextFile(filename, line -> {
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
		loadTextFile(filename, line -> {
			// Bus1->Bus2(1) 0
			String[] strAry = line.split(" ");
			this.branchId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
		// in the case of is mapping file, NN model size is determined by the info in the file
		this.noBranch = this.branchId2NoMapping.size();
	}
	
	protected void loadTextFile(String filename, Consumer<String> processor) {
		try (Stream<String> stream = Files.lines(Paths.get(filename))) {
			stream.filter(line -> {return !line.startsWith("#") && 
					                      !line.trim().equals("");})
				  .forEach(processor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
