package lfgen.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.fadapter.IpssFileAdapter;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.aclf.impl.AclfLoadImpl;
import com.interpss.core.algo.LoadflowAlgorithm;

import lfgen.algo.ILoadFlowGenerator;
import lfgen.algo.IQChecker;
import lfgen.algo.ISpecialBusChecker;
import lfgen.algo.IVgcBuilder;
import lfgen.algo.IVoltageGenerator;
import lfgen.algo.impl.LFComparator;
import lfgen.algo.impl.LoadFlowGenerator;
import lfgen.algo.impl.QChecker;
import lfgen.algo.impl.SpecialBusChecker;
import lfgen.algo.impl.VoltageGenerator;
import lfgen.condition_gen.VThScanConditionBuilder;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;
import lfgen.output.VThSpaceAstringency;
import lfgen.platform.LoadFlowCaseGenerator;

/**
* @author JeremyChenlk
* @version 2019年3月18日 下午2:48:52
*
* Class description:
*	
*/

public class LfAdjust {
	
	private AclfNetwork net = null;
	
	/*
	 * 各种模块的指针
	 * 1. 初值生成器	VoltageGenerator
	 * 2. 核心生成器	LoadFlowGenerator
	 * 3. 功率越限检查器	QChecker
	 * 4. 联络节点功率检查器	ConnectBusChecker
	 */
	protected IVoltageGenerator voltageGenerator = null;
	protected ILoadFlowGenerator loadFlowGenerator = null;
	protected IQChecker qChecker = null;
	protected ISpecialBusChecker specialBusChecker = null;
	protected IVgcBuilder voltageGenConditionBuilder = null;
	protected RefInfo refInfo = null;
	
	public LfAdjust(String baseCasePath) throws InterpssException {
		IpssCorePlugin.init();
		AclfNetwork refNet = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();
		gotoLimit(refNet);
		
		net = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();
		gotoLimit(net);
		LFOut.showlf(net);
		
		refInfo = new RefInfo(refNet);//主要的时间花费
		
		VoltageGenCondition.init(refInfo);
	}

	public void init(){
		long startTime = System.currentTimeMillis();
		
		//new all the methods' implements
		voltageGenerator = new VoltageGenerator(refInfo);
		
		loadFlowGenerator = new LoadFlowGenerator(refInfo);
		
		qChecker = new QChecker(refInfo);
		
		specialBusChecker = new SpecialBusChecker(refInfo);
		
		voltageGenConditionBuilder = new VThScanConditionBuilder(refInfo);
	}
	
	private Complex[] genVoltage(double a) {
		Complex[] voltage = new Complex[net.getNoBus()];
		for (int i=0; i<net.getNoBus(); ++i) {
			voltage[i] = refInfo.getNet().getBusList().get(i).getVoltage();
		}
		voltage[9] = new Complex(voltage[9].getReal() * (1-a),
				voltage[9].getImaginary() * (1+a));
		return voltage;
	}

	protected void go(double a) throws InterpssException{
		long startTime = System.currentTimeMillis();
		
		Complex[] voltage = genVoltage(a);
		AclfCase aclfCase = new AclfCase(voltage);
		
		specialBusChecker.correct(aclfCase);
		reportCase("After SBC", aclfCase);
		
		boolean successGen = true;
		while (!qChecker.correct(aclfCase)) {
			reportCase("After QC", aclfCase);
			specialBusChecker.correct(aclfCase);
			reportCase("After QC and SBC", aclfCase);
		}
		if (successGen) {
			successGenReaction(aclfCase);
		}
	}
	
	private void reportCase(String str, AclfCase aclfCase) throws InterpssException {
		System.out.println("\n==========="+str+"============");
		
		setNet(aclfCase);

		System.out.println("\n=========== End "+str+"============");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setNet(AclfCase aclfCase) throws InterpssException {
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		Complex[] voltage = aclfCase.getVoltage();
		Complex[] power = aclfCase.getPower();
//		//given all param: PQ + Vtheta
//		for (int i=0; i<net.getNoBus(); ++i) {
//			AclfBus bus = net.getBusList().get(i);
//			Complex injectPower = power[i];
//			bus.setVoltage(voltage[i]);
//			if (bus.getGenCode().getName().equals("Swing")) {
//				System.out.println("SET Swing Bus "+i+" voltage to: "+voltage[i]+", bus voltage = "+bus.getVoltage());
//			}else if (bus.getGenCode().getName().equals("GenPV")) {
//				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
//				bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
//				System.out.println("SET GenPV Bus "+i+" V to: "+voltage[i].abs()+", bus V = "+bus.getVoltageMag());
//				System.out.println("SET GenPV Bus "+i+" P to: "+injectPower.getReal()+", bus P = "+(bus.getGenP() - bus.getLoadP()));
//			}else if (bus.getGenCode().getName().equals("GenPQ")) {
//				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
//				if (bus.getContributeLoadList().size() == 0) {
//					System.out.println(injectPower);
//					if (refInfo.getBusType()[i] == RefInfo.ONLY_PV_BUS_TYPE) {
//						bus.getContributeGenList().get(0).setGen(injectPower);
//					}
//				}else {
//					bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
//				}
//				System.out.println("SET GenPQ Bus "+i+" power to: "+injectPower+", bus P = "+(bus.getGenPQ().subtract(bus.getLoadPQ())));
//			}
//		}
//		//lf given param 1 0
//		for (int i=0; i<net.getNoBus(); ++i) {
//			AclfBus bus = net.getBusList().get(i);
//			Complex injectPower = power[i];
//			if (bus.getGenCode().getName().equals("Swing")) {
//				bus.setVoltage(voltage[i]);
//				System.out.println("SET Swing Bus "+i+" voltage to: "+voltage[i]+", bus voltage = "+bus.getVoltage());
//			}else if (bus.getGenCode().getName().equals("GenPV")) {
//				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
//				bus.setVoltageMag(voltage[i].abs());
//				bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
//				System.out.println("SET GenPV Bus "+i+" V to: "+voltage[i].abs()+", bus V = "+bus.getVoltageMag());
//				System.out.println("SET GenPV Bus "+i+" P to: "+injectPower.getReal()+", bus P = "+(bus.getGenP() - bus.getLoadP()));
//			}else if (bus.getGenCode().getName().equals("GenPQ")) {
//				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
//				if (bus.getContributeLoadList().size() == 0) {
//					System.out.println(injectPower);
//					if (refInfo.getBusType()[i] == RefInfo.ONLY_PV_BUS_TYPE) {
//						bus.getContributeGenList().get(0).setGen(injectPower);
//					}
//				}else {
//					bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
//				}
//				System.out.println("SET GenPQ Bus "+i+" power to: "+injectPower+", bus P = "+(bus.getGenPQ().subtract(bus.getLoadPQ())));
//			}
//		}
		//lf given param former
		for (int i=0; i<net.getNoBus(); ++i) {
			AclfBus bus = net.getBusList().get(i);
			Complex injectPower = power[i];
			if (bus.getGenCode().getName().equals("Swing")) {
				bus.setVoltage(voltage[i]);
				System.out.println("SET Swing Bus "+i+" voltage to: "+voltage[i]+", bus voltage = "+bus.getVoltage());
			}else if (bus.getGenCode().getName().equals("GenPV")) {
				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
				bus.setVoltageMag(voltage[i].abs());
				bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
				System.out.println("SET GenPV Bus "+i+" V to: "+voltage[i].abs()+", bus V = "+bus.getVoltageMag());
				System.out.println("SET GenPV Bus "+i+" P to: "+injectPower.getReal()+", bus P = "+(bus.getGenP() - bus.getLoadP()));
			}else if (bus.getGenCode().getName().equals("GenPQ")) {
				Complex oriGen = refInfo.getNet().getBusList().get(i).getGenPQ();
				if (bus.getContributeLoadList().size() == 0) {
					System.out.println(injectPower);
					if (refInfo.getBusType()[i] == RefInfo.ONLY_PV_BUS_TYPE) {
						bus.getContributeGenList().get(0).setGen(injectPower);
					}
				}else {
					bus.getContributeLoadList().get(0).setLoadCP(oriGen.subtract(injectPower));
				}
				System.out.println("SET GenPQ Bus "+i+" power to: "+injectPower+", bus P = "+(bus.getGenPQ().subtract(bus.getLoadPQ())));
			}
		}
		System.out.println("======= before lf =======");
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		System.out.println("======= after lf = "+algo.loadflow()+"  =======");
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		System.out.println(AclfOutFunc.loadLossAllocation(net));
		LFOut.showlf(net);
	}
	
	protected void successGenReaction(AclfCase aclfCase) {
		System.out.println("=========================================================SUCCESSGEN!================================================");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void gotoLimit(AclfNetwork net) throws InterpssException {
		
		int busi = 9;
		System.out.println("============ Go to Limit ============");
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		
		AclfBus bus = net.getBusList().get(9);
		Complex base = bus.getLoadPQ();
		Complex load0 = bus.getLoadPQ();
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
		System.out.println("after searching, bus 9 load PQ = "+result+", lf result = "+algo.loadflow()+", result/load0 = "+result.divide(load0));
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		System.out.println(AclfOutFunc.loadLossAllocation(net));
		LFOut.showlf(net);
	}

	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		LfAdjust g = new LfAdjust(baseCasePath);
		g.init();
		g.go(0.01);
	}

}
