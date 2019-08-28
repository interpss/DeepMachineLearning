package lfgen.platform;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.fadapter.IpssFileAdapter;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;

import lfgen.algo.ILoadFlowGenerator;
import lfgen.algo.IQChecker;
import lfgen.algo.ISpecialBusChecker;
import lfgen.algo.IStatistics;
import lfgen.algo.IVgcBuilder;
import lfgen.algo.IVoltageGenerator;

import lfgen.algo.impl.AlgoObject;
import lfgen.algo.impl.LoadFlowGenerator;
import lfgen.algo.impl.QChecker;
import lfgen.algo.impl.SpecialBusChecker;
import lfgen.algo.impl.VoltageGenerator;

import lfgen.condition_gen.VThScanConditionBuilder;
import lfgen.datatype.AclfCase;
import lfgen.datatype.VoltageGenCondition;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年1月24日 下午2:48:16
*
* Class description:
*	提供一个生成方法的平台，要有各种模块的指针
*
* Step1. initialization
* Description:
* 	provide V0 and theta0
* 
* Step2. generate case
* Description:
* 	generate P and Q with given V and theta
* 
* Step3. Qchecker
* Description:
* 	return power correction info
*
* Step4. connect bus power checker
* Description:
*  correct voltage to fit the power limitation of connect bus 
*
* Step6. go to step3, if fits, output this case
*/

public abstract class LoadFlowCaseGenerator implements ILoadFlowCaseGenerator, IStatistics {

	public static final String NAME = "LoadFlowCaseGenerator";
	public static final String NAME_IN_SHORT = "dLFG";
	public static final String PARA_NEEDED = "NONE";
	public static final double DEFAULT_PU_ERROR = 1e-10;
	
	/**
	 * 为多次调用设计，用来统计总的调用次数和总的迭代次数
	 */
	protected int callTimes = 0;
	protected int totalIterTimes = 0;
	/**
	 * 用于统计迭代数量,在init中
	 */
	protected int[] iter = null;
	
	/**
	 * 调用时间统计
	 */
	protected long timeUse = 0;
	protected long initTimeUse = 0;
	protected long[] iterTimeUse = null;
	
	
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

	/**
	 * 网络信息类，只要网络对象一确定，内中所有量就确定的一个类
	 */
	protected RefInfo refInfo = null;
	protected String platformName = null;
	protected String nameInShort = null;
	
	/**
	 * 构造函数，根据基态潮流生成refInfo，用
	 * @param baseCasePath
	 * @throws InterpssException
	 */
	public LoadFlowCaseGenerator(String baseCasePath) throws InterpssException {
		long startTime = System.currentTimeMillis();
		platformName = NAME;
		nameInShort = NAME_IN_SHORT;
		System.out.print("[REPORT] new "+platformName+"...");
		
		timeUse = 0;
		initTimeUse = 0;
		callTimes = 0;
		totalIterTimes = 0;
		
		IpssCorePlugin.init();
		AclfNetwork refNet = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();
		
		gotoLimit(refNet);
		
		refInfo = new RefInfo(refNet);//主要的时间花费
		
		VoltageGenCondition.init(refInfo);
		
		//report
		System.out.println(" ...ready.");
		addInitTime(startTime);
	}
	
	protected void gotoLimit(AclfNetwork net) throws InterpssException {
		
		int busi = 9;
		System.out.println("============ Go to Limit ============");
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		
		AclfBus bus = net.getBusList().get(9);
		Complex base = bus.getLoadPQ();
		Complex delta = base.multiply(10);
		Complex result = base;

		System.out.println("base = "+base+", delta = "+delta);
		if (bus.getContributeLoadList().size() == 0)
			return;//仅用于cpf39的妥协
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
		
	}

	/**
	 * 初始化方法，是整个平台的插座，通过实现类重写此方法来插入不同的算法
	 * 需要继承后自己重写
	 * @param maxIter
	 * @param boundaryFactor
	 * @param qLimitSparseFactor
	 * @throws InterpssException
	 */
	@Override
	public void init(){
		long startTime = System.currentTimeMillis();
		
		//new all the methods' implements
		voltageGenerator = new VoltageGenerator(refInfo);
		
		loadFlowGenerator = new LoadFlowGenerator(refInfo);
		
		qChecker = new QChecker(refInfo);
		
		specialBusChecker = new SpecialBusChecker(refInfo);
		
		voltageGenConditionBuilder = new VThScanConditionBuilder(refInfo);
		
		addInitTime(startTime);
	}
	
	/**
	 * 生成条件，根据条件生成case
	 * 不需要写计时的！！
	 * 1. 决定各个方法的参数
	 * 2. 调用多少次go(c)
	 * 3. 怎样输出报告（感觉干的是main干的）
	 * @throws InterpssException 
	 */
	@Override
	public void boom() throws InterpssException {
		int maxIter = 10;
		iter = new int[maxIter+1];
		iterTimeUse = new long[maxIter+1];
		for (int i=0; i<(1<<26); ++i) 
			go(voltageGenConditionBuilder.nextCondition());
	}
	
	protected void setMaxIter(int maxIter) {
		iter = new int[maxIter+1];
		iterTimeUse = new long[maxIter+1];
		System.out.println("[REPORT] "+platformName+" paramter set. maxIter = "+maxIter);
	} 
	
	/**
	 * 此方法不对外开放，是算法的核心步骤来的
	 * @param c
	 * @throws InterpssException
	 */
	protected void go(VoltageGenCondition c) throws InterpssException{
		long startTime = System.currentTimeMillis();
		addCallTimes();
		
		Complex[] voltage = voltageGenerator.genVoltage(c);
		AclfCase aclfCase = new AclfCase(voltage);
		
		specialBusChecker.correct(aclfCase);

		int iter = 0;
		boolean successGen = true;
		while (!qChecker.correct(aclfCase)) {
			iter += 1;
			if (iter > this.iter.length - 2) {
				successGen = false;
				break;
			}
			specialBusChecker.correct(aclfCase);
		}
		this.iter[iter] += 1;
		this.totalIterTimes += iter;
		if (successGen) {
			successGenReaction(aclfCase);
		}
		addTimeUse(startTime, iter);
		addTimeUse(startTime);
	}
	
	/**
	 * 此方法用于执行对于成功生成案例的反应
	 * 不同实现中可以重写以添加执行方法。
	 * 默认不添加内容以加速
	 * @param onGoNet
	 * @param voltage
	 * @param power
	 */
	protected abstract void successGenReaction(AclfCase aclfCase);
	
	public void report() {
		System.out.println("[REPORT] "+platformName+".report():"
				+"\n ** Time use = "+this.timeUse+"\tSub Cases = "+callTimes+"\t initTime = "+this.initTimeUse+", iterTimes/callTimes = "+(totalIterTimes+0.0)/callTimes);

		((AlgoObject) voltageGenerator).report();
		((AlgoObject) loadFlowGenerator).report();
		((AlgoObject) qChecker).report();
		((AlgoObject) specialBusChecker).report();
		String str = new String("");
		for (int i=0; i<this.iter.length; ++i) {
			str += "\tIter "+i+" case =  "+this.iter[i]+"\t\ttime cost = "+this.iterTimeUse[i]+"\n";
		}
		System.out.println(str);
	}

	public String getReportTitle() {
		String report = new String("");
		
		report += "CallTimes\ttTotal\ttInit\ttVG\ttLFG\ttQC\ttSBC";
		for (int i=0; i<this.iter.length; ++i)
			report += "\t"+i;
		report += "\t"+(this.iter.length-2)+"Rate";
		for (int i=0; i<this.iter.length; ++i)
			report += "\t"+i+"time";
		
		return report;
	}

	/**
	 * 此方法限制了LFCG的所有方法指针必须指向(IDecorator)
	 */
	public String getReportStr() {
		String report = new String("");
		
		report += this.callTimes;
		report += "\t"+this.timeUse;
		report += "\t"+this.initTimeUse;
		report += "\t"+((AlgoObject) voltageGenerator).getTimeUse();
		report += "\t"+((AlgoObject) loadFlowGenerator).getTimeUse();
		report += "\t"+((AlgoObject) qChecker).getTimeUse();
		report += "\t"+((AlgoObject) specialBusChecker).getTimeUse();
		report += "\t"+((AlgoObject) voltageGenConditionBuilder).getTimeUse();
		
		double sum = 0;
		for (int i=0; i<this.iter.length-1; ++i) {
			report += "\t"+this.iter[i];
			sum += this.iter[i];
		}
		report += "\t"+Double.toString(sum/this.callTimes);
		for (int i=0; i<this.iter.length; ++i)
			report += "\t"+this.iterTimeUse[i];
		return report;
	}

	@Override
	public int getCallTimes() {
		return this.callTimes;
	}

	@Override
	public long getInitTimeUse() {
		return this.initTimeUse;
	}

	@Override
	public long getTimeUse() {
		return this.timeUse;
	}

	@Override
	public double getAverageTimeUse() {
		return this.timeUse/this.callTimes;
	}
	
	@Override
	public void resetStatistics() {
		callTimes = 0;
		totalIterTimes = 0;
		iter = new int[iter.length];
		timeUse = 0;
		initTimeUse = 0;
		iterTimeUse = new long[iter.length];
		
		((AlgoObject) voltageGenerator).resetStatistics();
		((AlgoObject) loadFlowGenerator).resetStatistics();
		((AlgoObject) qChecker).resetStatistics();
		((AlgoObject) specialBusChecker).resetStatistics();
		((AlgoObject) voltageGenConditionBuilder).resetStatistics();
		System.out.printf("[REPORT] platform %25s is reset, now called times =  %10d, with time use(s) = %10.0f, in which init time use = %10.3f\n",
				platformName, callTimes, timeUse/1000.0, initTimeUse/1000.0);
	}
	
	protected void addCallTimes() {
		this.callTimes += 1;
	}
	
	protected void addInitTime(long startTime) {
		long timeuse = System.currentTimeMillis() - startTime;
		this.initTimeUse += timeuse;
		this.timeUse += timeuse;
	}

	protected void addTimeUse(long startTime) {
		this.timeUse += System.currentTimeMillis() - startTime;
	}
	protected void addTimeUse(long startTime, int index) {
		this.iterTimeUse[index] += System.currentTimeMillis() - startTime;
	}

	public int getIterTimes() {
		return totalIterTimes;
	}

	public int[] getIter() {
		return iter;
	}

	public IVoltageGenerator getVoltageGenerator() {
		return voltageGenerator;
	}

	public ILoadFlowGenerator getLoadFlowGenerator() {
		return loadFlowGenerator;
	}

	public IQChecker getqChecker() {
		return qChecker;
	}

	public long[] getIterTime() {
		return iterTimeUse;
	}

	public ISpecialBusChecker getSpecialBusChecker() {
		return specialBusChecker;
	}

	public RefInfo getRefInfo() {
		return refInfo;
	}

	public String getPlatformName() {
		return platformName;
	}
}
