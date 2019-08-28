package lfgen.output;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.CorePluginFactory;
import org.interpss.fadapter.IpssFileAdapter;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;

import lfgen.algo.impl.AlgoObject;
import lfgen.algo.impl.LFComparator;
import lfgen.datatype.AclfCase;

/**
* @author JeremyChenlk
* @version 2019年2月21日 下午12:25:24
*
* Class description:
*	
*/

public class TimeUseBetweenLFAndPV extends LfcgByPV {

	public static final String NAME = "TimeUseBetweenLFAndPV";
	public static final String NAME_IN_SHORT = "PVtime";
	public static final String PARA_NEEDED = "NONE";
	
	private AclfNetwork onGoNet = null;
	private long lFTimeUse = 0;

	public TimeUseBetweenLFAndPV(String baseCasePath) throws InterpssException {
		super(baseCasePath);
		long startTime = System.currentTimeMillis();
		platformName = NAME;
		System.out.print("[REPORT] new "+platformName+"... ...ready.");
		
		onGoNet = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();
		
		this.lFTimeUse = 0;
		addInitTime(startTime);
	}
	
	@Override
	protected void successGenReaction(AclfCase aclfCase) {
		nCase += 1;
		if (LFComparator.checkCase(refInfo, aclfCase)) {
			okCase += 1;
			//P = G - L
			EList<AclfBus> busList = onGoNet.getBusList();
			Complex[] power = aclfCase.getPower();
			Complex[] voltage = aclfCase.getVoltage();
			for (int i=0; i<busList.size(); ++i) 
				if (i != refInfo.getSwingNo()){
					AclfBus bus = busList.get(i);
					//平启动跑一次好了
					if (refInfo.getBusType()[i] == 1/*纯PV节点*/) {
						bus.setGenP(bus.getLoadP() + power[i].getReal());
						bus.getPVBusLimit().setVSpecified(voltage[i].abs());
					}else {
						bus.setLoadP(bus.getGenP() - power[i].getReal());
						bus.setLoadQ(bus.getGenQ() - power[i].getImaginary());
					}
					bus.setVoltage(new Complex(1.0, 0));
				}
			
			long startTime = System.currentTimeMillis();
			
			//lf
			LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(onGoNet);
			
			try {
				algo.loadflow();
			} catch (InterpssException e) {
				e.printStackTrace();
			}
			
			this.lFTimeUse += System.currentTimeMillis() - startTime;
		}
	}

	@Override
	public String getReportTitle() {
		return super.getReportTitle()+"\tSuccessCaseLFTime";
	}
	
	@Override
	public String getReportStr() {
		return super.getReportStr()+"\t"+this.lFTimeUse;
	}
	
	@Override
	public void report() {
		System.out.println("\n\t==============================================\n"
				+"\t[REPORT] nSuccessCase = "+"\t"+nCase+"\tokCase = "+okCase+"\n"
				+"\t==============================================");
		
		System.out.println("[REPORT] "+platformName+".report():"
				+"\n ** Sub Time use = "+this.timeUse	+"\tSub Cases = "+callTimes+"\t initTime = "+this.initTimeUse+", iterTimes/callTimes = "+(totalIterTimes+0.0)/callTimes
				+"\n **  LF Time use = "+this.lFTimeUse+"\t LF Cases = "+(callTimes-iter[iter.length-1]));
		
		((AlgoObject) voltageGenerator).report();
		((AlgoObject) loadFlowGenerator).report();
		((AlgoObject) qChecker).report();
		((AlgoObject) specialBusChecker).report();
		String str = new String("");
		for (int i=0; i<this.iter.length; ++i) {
			str += "\tIter "+i+" case =  "+this.iter[i]+"\t\ttime cost = "+this.iterTimeUse[i]+"\n";
		}
		System.out.println(str);
		System.out.println("[REPORT] TimeUseBetweenLFAndSub.report(): "+"V boundary sparse = "
				+"\n ** Sub Time use = "+this.timeUse	+"\tSub Cases = "+callTimes+"\t initTime = "+this.initTimeUse+", iterTimes/callTimes = "+(totalIterTimes+0.0)/callTimes
				+"\n **  LF Time use = "+this.lFTimeUse+"\t LF Cases = "+(callTimes-iter[iter.length-1]));
	}
	
	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee118.ieee";
		TimeUseBetweenLFAndPV g = new TimeUseBetweenLFAndPV(baseCasePath);
		g.init();
		g.boom();
	}

}
