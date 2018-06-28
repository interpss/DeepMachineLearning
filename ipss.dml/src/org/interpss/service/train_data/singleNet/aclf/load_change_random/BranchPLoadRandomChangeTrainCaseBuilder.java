package org.interpss.service.train_data.singleNet.aclf.load_change_random;

public class BranchPLoadRandomChangeTrainCaseBuilder extends BaseLoadRandomChangeTrainCaseBuilder {

	@Override
	public double[] getNetOutput() {
		return this.getNetBranchP(this.getAclfNet());
	}

	
}
