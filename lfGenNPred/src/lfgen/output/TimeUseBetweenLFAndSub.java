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
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年2月2日 下午10:21:52
*
* Class description:
*	此类提供的数据用来：
*		比较迭代方法和潮流方法的用时差别
*		比较迭代方法和潮流方法的用时
*/

public class TimeUseBetweenLFAndSub extends VThSpaceAstringency {

	public static final String NAME = "TimeUseBetweenLFAndSub";
	public static final String NAME_IN_SHORT = "VTH";
	public static final String PARA_NEEDED = "NONE";
	
	private AclfNetwork onGoNet = null;
	private long lFTimeUse = 0;

	public TimeUseBetweenLFAndSub(String baseCasePath) throws InterpssException {
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
	
	/**
	 * setPowerIntoNet 然后平启动计时
	 * @param onGoNet
	 * @param voltage
	 * @param power
	 */
	@Override
	protected void successGenReaction(AclfCase aclfCase) {
		//P = G - L
		EList<AclfBus> busList = onGoNet.getBusList();
		Complex[] power = aclfCase.getPower();
		Complex[] voltage = aclfCase.getVoltage();
		String[] busCode = refInfo.getBusCode();
		for (int i=0; i<refInfo.getNoBus(); ++i) {
			AclfBus bus = busList.get(i);
			Complex injectPower = power[i];
			if (bus.getGenCode().getName().equals("Swing")) {
				bus.setVoltage(voltage[i]);
//				System.out.println("SET Swing Bus "+i+" voltage to: "+voltage[i]+", bus voltage = "+bus.getVoltage());
			}else if (bus.getGenCode().getName().equals("GenPV")) {
				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
				bus.setVoltageMag(voltage[i].abs());
				bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
//				System.out.println("SET GenPV Bus "+i+" V to: "+voltage[i].abs()+", bus V = "+bus.getVoltageMag());
//				System.out.println("SET GenPV Bus "+i+" P to: "+injectPower.getReal()+", bus P = "+(bus.getGenP() - bus.getLoadP()));
			}else if (bus.getGenCode().getName().equals("GenPQ")) {
				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
				if (bus.getContributeLoadList().size() == 0) {
//					System.out.println(injectPower);
					if (refInfo.getBusType()[i] == RefInfo.ONLY_PV_BUS_TYPE) {
						bus.getContributeGenList().get(0).setGen(injectPower);
					}
				}else {
					bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
				}
//				System.out.println("SET GenPQ Bus "+i+" power to: "+injectPower+", bus P = "+(bus.getGenPQ().subtract(bus.getLoadPQ())));
			}
		}
		
		long startTime = System.currentTimeMillis();
		
		//lf
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(onGoNet);
		
		try {
			boolean f = algo.loadflow();
		} catch (InterpssException e) {
			e.printStackTrace();
		}
		
		this.lFTimeUse += System.currentTimeMillis() - startTime;
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
		TimeUseBetweenLFAndSub g = new TimeUseBetweenLFAndSub(baseCasePath);
		g.init();
		g.boom();
	}

}
