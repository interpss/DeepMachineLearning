package lfgen.condition_gen;

import java.util.Random;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;

import com.interpss.core.aclf.AclfBus;

import lfgen.algo.IVgcBuilder;
import lfgen.algo.impl.AlgoObject;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019年2月13日 下午6:23:34
*
* Class description:
*	p+dp
*	v+dv
*/

public class PVConditionGenerator extends AlgoObject implements IVgcBuilder {

	public static final String NAME = "PVConditionGenerator";
	public static final String NAME_IN_SHORT = "PV";
	public static final String PARA_NEEDED = "sigma";
	
	private VoltageGenCondition c = null;
	
	public static int SWING = 0;
	public static int ONLY_PV = 1;
	public static int ONLY_PM = 2;
	public static int PV_AND_PQ = 3;
	public static int CONNECT = 4;
	public static int NORMAL_PQ = 5;
	public static double DEFAULT_P_POOL_SIZE = 1.0;
	public static double DEFAULT_SIGMA = 0.062;
	public static double DEFAULT_DV = 0.1;
	private int noBus = 0;
	private int swingNo = 0;
	private int[] busType = null;
	private double[] pbase = null;
	private double[] vbase = null;
	private double[] dp = null;
	private double[] dv = null;
	private double pSize = DEFAULT_P_POOL_SIZE;
	private double sigma = DEFAULT_SIGMA;
	
	private Random random = null;
	
	public PVConditionGenerator(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		c = new VoltageGenCondition(false);//isThVMethod
		noBus = ref.getNoBus();
		swingNo = ref.getSwingNo();
		busType = new int[noBus];
		pbase = new double[noBus];
		vbase = new double[noBus];
		dp = new double[noBus];
		dv = new double[noBus];
		
		random = new Random();
		
		EList<AclfBus> busList = ref.getNet().getBusList();
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = busList.get(i);
			String genCode = bus.getGenCode().getName();
			if (genCode.equals("Swing")) {//平衡节点
				this.busType[i] = PVConditionGenerator.SWING;
				this.swingNo = i;
			}else if (genCode.equals("GenPV")) {//纯PV节点
				if (Complex.equals(bus.getLoadPQ(), Complex.ZERO)) {
					if (bus.getGenP() == 0) {
						this.busType[i] = PVConditionGenerator.ONLY_PM;//纯调相机
					}else {
						this.busType[i] = PVConditionGenerator.ONLY_PV;//非调相机纯PV
					}
				}else {
					this.busType[i] = PVConditionGenerator.PV_AND_PQ;//带PQ的PV
				}
			}else if (genCode.equals("GenPQ")) {
				if (bus.getGenP() == 0 && bus.getGenQ() == 0 && bus.getLoadP() == 0 && bus.getLoadQ() == 0)
					this.busType[i] = PVConditionGenerator.CONNECT;//联络节点
				}else
					this.busType[i] = PVConditionGenerator.NORMAL_PQ;//其他PQ节点
			pbase[i] = bus.getGenP() - bus.getLoadP();
			vbase[i] = bus.getVoltage().abs();
		}
		
		calDp();
		calDv();

		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	public void setSigma(double sigma) {
		this.sigma = sigma;
		System.out.println("[REPORT] "+methodName+" paramter set. sigma = "+sigma);
	}
	
	private void calDp() {
		EList<AclfBus> busList = ref.getNet().getBusList();
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = busList.get(i);
			if (busType[i] == PVConditionGenerator.SWING) {
				pbase[i] = (bus.getGenP() - bus.getLoadP()) * this.pSize;
				dp[i] = 0;
			}else if (busType[i] == PVConditionGenerator.ONLY_PV) {
				pbase[i] = (bus.getGenP() - bus.getLoadP());
				dp[i] = (bus.getGenP() - bus.getLoadP()) * 0.5;
			}else if (busType[i] == PVConditionGenerator.ONLY_PM) {
				pbase[i] = 0;
				dp[i] = 0;
			}else if (busType[i] == PVConditionGenerator.PV_AND_PQ) {
				pbase[i] = (bus.getGenP() - bus.getLoadP());
				dp[i] = (bus.getGenP() - bus.getLoadP()) * 0.5;
			}else if (busType[i] == PVConditionGenerator.CONNECT) {
				pbase[i] = 0;
				dp[i] = 0;
			}else if (busType[i] == PVConditionGenerator.NORMAL_PQ) {
				pbase[i] = (bus.getGenP() - bus.getLoadP());
				dp[i] = (bus.getGenP() - bus.getLoadP()) * 0.5;
			}
		}
	}
	
	private void calDv() {
		EList<AclfBus> busList = ref.getNet().getBusList();
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = busList.get(i);
			if (busType[i] == PVConditionGenerator.SWING) {
				vbase[i] = bus.getVoltage().abs();
				dv[i] = 0;
			}else if (busType[i] == PVConditionGenerator.ONLY_PV) {
				vbase[i] = bus.getVoltage().abs();
				dv[i] = DEFAULT_DV;
			}else if (busType[i] == PVConditionGenerator.ONLY_PM) {
				vbase[i] = bus.getVoltage().abs();
				dv[i] = DEFAULT_DV;
			}else if (busType[i] == PVConditionGenerator.PV_AND_PQ) {
				vbase[i] = bus.getVoltage().abs();
				dv[i] = DEFAULT_DV;
			}else if (busType[i] == PVConditionGenerator.CONNECT) {
				vbase[i] = bus.getVoltage().abs();
				dv[i] = DEFAULT_DV;
			}else if (busType[i] == PVConditionGenerator.NORMAL_PQ) {
				vbase[i] = bus.getVoltage().abs();
				dv[i] = DEFAULT_DV;
			}
		}
	}

	@Override
	public VoltageGenCondition nowCondition() {
		return this.c;
	}

	@Override
	public VoltageGenCondition nextCondition() {
		long startTime = System.currentTimeMillis();
		
		for (int i=0; i<noBus; ++i) {
			c.setPi(pbase[i] + dp[i]*nextRandom(), i);
			c.setVi(vbase[i] + dv[i]*nextRandom(), i);
		}
		
		addTimeUse(startTime);
		return this.c;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT+"-sig-"+sigma;
	}
	
	private double nextRandom() {
		double dis = random.nextGaussian();
		while (dis < -1 || dis > 1)
			dis = random.nextGaussian();
		return dis * this.sigma;
	}
}
