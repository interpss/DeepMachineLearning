package org.interpss.service.train_data.singleNet.aclf.load_change_random;

public class BusVoltLoadChangeRandomTrainCaseBuilder extends BaseRandomLoadChangeTrainCaseBuilder {

	@Override
	public double[] getNetOutput() {
		return this.getNetOutputVoltage(this.getAclfNet());
	}

	
}
