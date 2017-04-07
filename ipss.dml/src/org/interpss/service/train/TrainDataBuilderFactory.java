package org.interpss.service.train;

import org.interpss.service.train.impl.TrainCaseBuilder;

import com.interpss.core.aclf.AclfNetwork;

public class TrainDataBuilderFactory {
	public static ITrainCaseBuilder createITrainCaseBuilder(AclfNetwork aclfNet) {
		return new TrainCaseBuilder(aclfNet);
	}
}
 