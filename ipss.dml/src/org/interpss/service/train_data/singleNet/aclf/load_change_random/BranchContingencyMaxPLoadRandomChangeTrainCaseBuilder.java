package org.interpss.service.train_data.singleNet.aclf.load_change_random;

import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.contingency.BranchOutageType;
import com.interpss.core.aclf.contingency.Contingency;
import com.interpss.core.dclf.common.ReferenceBusException;
import com.interpss.core.dclf.solver.HashMapCacheDclfSolver;

public class BranchContingencyMaxPLoadRandomChangeTrainCaseBuilder extends BaseRandomLoadChangeTrainCaseBuilder{
	private static int i;
	@Override
	public double[] getNetOutput() {
		double[] output = new double[this.noBranch];
		//IpssCorePlugin.init();  this statement should put in the main function
		try {
			DclfAlgorithmDSL algoDsl = IpssDclf.createDclfAlgorithm(getAclfNet());
			algoDsl.getAlgorithm().setDclfSolver(new HashMapCacheDclfSolver(getAclfNet()));
			algoDsl.runDclfAnalysis();
			getAclfNet().getBranchList().stream()
					.filter(branch -> !branch.getFromAclfBus().isRefBus() && !branch.getToAclfBus().isRefBus())
					.forEach(branch -> CoreObjectFactory.createContingency(branch.getId(), branch.getId(),
							BranchOutageType.OPEN, getAclfNet()));

			getAclfNet().getContingencyList().forEach(cont -> {
				i = 0;
				algoDsl.contingencyAanlysis((Contingency) cont, (contBranch, postContFlow) -> {
					if (output[i] < Math.abs(postContFlow/getAclfNet().getBaseMva()))
						output[i] = Math.abs(postContFlow/getAclfNet().getBaseMva());
					i++;
				});
			});
		} catch (InterpssException | ReferenceBusException | IpssNumericException e) {
			e.printStackTrace();
		}
		
		return output;
	}


	
}
