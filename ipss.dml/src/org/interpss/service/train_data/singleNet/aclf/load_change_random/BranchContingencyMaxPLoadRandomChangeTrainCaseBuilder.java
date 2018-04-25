package org.interpss.service.train_data.singleNet.aclf.load_change_random;

public class BranchContingencyMaxPLoadRandomChangeTrainCaseBuilder extends BaseRandomLoadChangeTrainCaseBuilder{

	@Override
	public double[] getNetOutput() {
		return this.getNetBranchContingencyMaxP(this.aclfNet);
	}

}
