package lfgen.platform;

import com.interpss.common.exp.InterpssException;

import lfgen.algo.impl.LoadFlowGenerator;
import lfgen.algo.impl.QChecker;
import lfgen.algo.impl.SpecialBusChecker;
import lfgen.algo.impl.VoltageGenerator;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午1:45:09
*
* Class description:
*	
*/

public interface ILoadFlowCaseGenerator {
	void init();
	void boom() throws InterpssException;
}
