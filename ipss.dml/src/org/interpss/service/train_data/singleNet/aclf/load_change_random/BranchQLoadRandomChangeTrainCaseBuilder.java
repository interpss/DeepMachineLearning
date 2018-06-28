package org.interpss.service.train_data.singleNet.aclf.load_change_random;

import com.interpss.core.aclf.AclfBranch;

public class BranchQLoadRandomChangeTrainCaseBuilder extends BaseLoadRandomChangeTrainCaseBuilder {

	@Override
	public double[] getNetOutput() {
		double[] output = new double[this.noBranch];

		int i = 0;
		for (AclfBranch branch : aclfNet.getBranchList()) {
			if (branch.isActive()) {
				if (this.branchId2NoMapping != null)
					i = this.branchId2NoMapping.get(branch.getId());
				output[i] = branch.powerFrom2To().getImaginary();
				i++;
			}
		}
		return output;
	}

	
}
