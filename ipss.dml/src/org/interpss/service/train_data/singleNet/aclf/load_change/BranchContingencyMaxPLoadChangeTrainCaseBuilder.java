package org.interpss.service.train_data.singleNet.aclf.load_change;

import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.contingency.BranchOutageType;
import com.interpss.core.aclf.contingency.Contingency;
import com.interpss.core.dclf.common.ReferenceBusException;

/**
 * Load bus P,Q are modified to create training cases for predicting branch
 * active power flow
 * 
 * 
 */

public class BranchContingencyMaxPLoadChangeTrainCaseBuilder extends BaseLoadChangeTrainCaseBuilder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.interpss.service.ITrainCaseBuilder#getNetInputPQ()
	 */
	@Override
	public double[] getNetInput() {
		return this.getNetInputPQ(this.getAclfNet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.interpss.service.ITrainCaseBuilder#getNetOutputVolt()
	 */
	@Override
	public double[] getNetOutput() {
		return this.getNetBranchContingencyMaxP(this.aclfNet);
	}

}
