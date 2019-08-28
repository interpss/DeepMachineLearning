package lfgen.validate;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.fadapter.IpssFileAdapter;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGen;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;

import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019年3月13日 下午2:41:56
*
* Class description:
*	
*/

public class LfLimit {
	
	private AclfNetwork net = null;

	public LfLimit(String baseCasePath) throws InterpssException {
		IpssCorePlugin.init();
		net = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();
	}
	
	public void go() throws InterpssException {
		
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		algo.loadflow();
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		LFOut.showlf(net);
//		printNetStatus();
		
		AclfBus bus = net.getBusList().get(9);
		Complex base = bus.getLoadPQ();
		Complex delta = base.multiply(10);
		Complex result = base;

		System.out.println("base = "+base+", delta = "+delta);
		bus.getContributeLoadList().get(0).setLoadCP(base.add(delta));
		System.out.println("\tbus load pq = "+bus.getLoadPQ());
		
		int iter = 0;
		while(delta.abs() > 1e-10) {
			iter += 1;
			//当前单根区间
			System.out.println("base = "+base+", delta = "+delta);
			
			//试探中点
			delta = delta.multiply(0.5);
			bus.getContributeLoadList().get(0).setLoadCP(base.add(delta));
			//中点坐标
			System.out.println("bus load pq = "+bus.getLoadPQ());
			
			//若试探成功则...
			if (algo.loadflow()) {
				base = base.add(delta);
				result = base;
				System.out.println("a success bus 9 load PQ = "+bus.getLoadPQ());
			}
//			System.out.println(AclfOutFunc.loadFlowSummary(net));
		}
		
		System.out.println("iter = "+iter);
		bus.getContributeLoadList().get(0).setLoadCP(result);
		System.out.println("after searching, bus 9 load PQ = "+result+", lf result = "+algo.loadflow());
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		LFOut.showlf(net);
//		printNetStatus();
	}
	
	public void printNetStatus() {
		/*
		 * 1. print contribute gen and load
		 * 2. print gencode
		 */
		System.out.println("====================Print bus status====================="); 
		for (AclfBus bus:net.getBusList()) {
			System.out.println("Bus "+bus.getId()+" GenCode = "+ bus.getGenCode());
			for (AclfGen gen:bus.getContributeGenList()) {
				System.out.println("Gen "+gen.getId()+" toString ="+gen.toString());
			}
			for (AclfLoad load:bus.getContributeLoadList()) {
				System.out.println("Load "+load.getId()+" toString ="+load.toString());
			}
		}
	}
	
	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		LfLimit g = new LfLimit(baseCasePath);
		g.go();
	}

}
