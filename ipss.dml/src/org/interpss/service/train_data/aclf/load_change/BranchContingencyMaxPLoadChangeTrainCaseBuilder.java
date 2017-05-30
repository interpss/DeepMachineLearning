package org.interpss.service.train_data.aclf.load_change;

import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;
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

	private static int i;

	public BranchContingencyMaxPLoadChangeTrainCaseBuilder() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.interpss.service.ITrainCaseBuilder#getNetInputPQ()
	 */
	@Override
	public double[] getNetInput() {
		return this.getNetInputPQ();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.interpss.service.ITrainCaseBuilder#getNetOutputVolt()
	 */
	@Override
	public double[] getNetOutput() {
		double[] output = new double[this.noBranch];
		//IpssCorePlugin.init();  this statement should put in the main function
		try {
			DclfAlgorithmDSL algoDsl = IpssDclf.createDclfAlgorithm(aclfNet).runDclfAnalysis();
			aclfNet.getBranchList().stream()
					.filter(branch -> !branch.getFromAclfBus().isRefBus() && !branch.getToAclfBus().isRefBus())
					.forEach(branch -> CoreObjectFactory.createContingency(branch.getId(), branch.getId(),
							BranchOutageType.OPEN, aclfNet));

			aclfNet.getContingencyList().forEach(cont -> {
				i = 0;
				algoDsl.contingencyAanlysis((Contingency) cont, (contBranch, postContFlow) -> {
					if (output[i] < Math.abs(postContFlow/aclfNet.getBaseMva()))
						output[i] = Math.abs(postContFlow/aclfNet.getBaseMva());
					i++;
				});
			});
		} catch (InterpssException | ReferenceBusException | IpssNumericException e) {
			e.printStackTrace();
		}
		
		return output;
	}

}
