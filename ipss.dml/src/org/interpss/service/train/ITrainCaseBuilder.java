package org.interpss.service.train;

import com.interpss.core.aclf.AclfNetwork;

/**
 * A training case builder interface
 * 
 * @author Mike
 *
 */ 
public interface ITrainCaseBuilder {
	/**
	 * @return the aclfNet
	 */
	AclfNetwork getAclfNet();
 
	/**
	 * @return the noBus
	 */
	int getNoBus();

	/**
	 * create a new training case
	 */
	void createTrainCase();

	/**
	 * get loadflow calculation input data of the current training case
	 * 
	 * @return Loadflow calculation input: [P/Vang, Q/Vmag]
	 */
	double[] getNetInputPQ();

	/**
	 * get loadflow calculation output data of the current training case
	 *  
	 * @return Loadflow calculation output: [Vmag/Q, Vang/P]
	 */
	double[] getNetOutputVolt();
	
	/**
	 * compute AclfNetwork mismatch information
	 *  
	 * @param netVolt
	 * @return
	 */
	String getMismatchInfo(double[] netVolt);
}