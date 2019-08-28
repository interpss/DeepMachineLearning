package org.interpss.service.train_data.singleNet.aclf.load_change_random;

import org.apache.commons.math3.complex.Complex;

public class InterfacePowerRandomChangeTrainCaseBuilder extends BaseRandomLoadChangeTrainCaseBuilder{

	@Override
	public double[] getNetOutput() {
		int i = 3;
		double[] output = new double[2 * i];

		Complex power = getAclfNet().getBranch("Bus5->Bus6(1)").powerFrom2To();
		output[0] = power.getReal();
		output[1] = power.getImaginary();
		power = getAclfNet().getBranch("Bus4->Bus7(1)").powerFrom2To();
		output[2] = power.getReal();
		output[3] = power.getImaginary();
		power = getAclfNet().getBranch("Bus4->Bus9(1)").powerFrom2To();
		output[4] = power.getReal();
		output[5] = power.getImaginary();
		return output;
	}

}
