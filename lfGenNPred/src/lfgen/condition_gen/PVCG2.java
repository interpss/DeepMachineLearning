package lfgen.condition_gen;

import java.util.Random;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;

import com.interpss.core.aclf.AclfBus;

import lfgen.algo.IVgcBuilder;
import lfgen.algo.impl.AlgoObject;
import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;

/**
* @author JeremyChenlk
* @version 2019年2月16日 下午3:15:21
*
* Class description:
*	直流潮流+随机有功
*	随即电压+pvbuff
*/

public class PVCG2 extends AlgoObject implements IVgcBuilder {

	public static final String NAME = "PVCG2";
	public static final String NAME_IN_SHORT = "PVCG2";
	public static final String PARA_NEEDED = "vbase, vdis, pvbuff";

	public static double DEFAULT_P_POOL_SIZE = 1.0;

	public static int SWING = 0;
	public static int ONLY_PV = 1;
	public static int ONLY_PM = 2;
	public static int PV_AND_PQ = 3;
	public static int CONNECT = 4;
	public static int NORMAL_PQ = 5;
	
	private VoltageGenCondition c = null;
	private int noBus = 0;
	private int swingNo = 0;
	private int[] busType = null;
	private Random random = null;
	
	private double[] pf = null;
	private double pSize = DEFAULT_P_POOL_SIZE;
	
	private double vbase = 1;
	private double vdis = 0.05;
	private double pvbuff = 0.05;
	
	public PVCG2(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		c = new VoltageGenCondition(false);//isThVMethod
		noBus = ref.getNoBus();
		swingNo = ref.getSwingNo();
		busType = new int[noBus];
		pf = new double[noBus];
		
		random = new Random();
		
		EList<AclfBus> busList = ref.getNet().getBusList();
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = busList.get(i);
			String genCode = bus.getGenCode().getName();
			if (genCode.equals("Swing")) {//平衡节点
				this.busType[i] = PVCG2.SWING;
				this.swingNo = i;
			}else if (genCode.equals("GenPV")) {//纯PV节点
				if (Complex.equals(bus.getLoadPQ(), Complex.ZERO)) {
					if (bus.getGenP() == 0) {
						this.busType[i] = PVCG2.ONLY_PM;//纯调相机
					}else {
						this.busType[i] = PVCG2.ONLY_PV;//非调相机纯PV
					}
				}else {
					this.busType[i] = PVCG2.PV_AND_PQ;//带PQ的PV
				}
			}else if (genCode.equals("GenPQ")) {
				if (bus.getGenP() == 0 && bus.getGenQ() == 0 && bus.getLoadP() == 0 && bus.getLoadQ() == 0) {
					this.busType[i] = PVCG2.CONNECT;//联络节点
				}else {
					this.busType[i] = PVCG2.NORMAL_PQ;//其他PQ节点
				}
			}
		}
		
		calpf();

		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	private void calpf() {
		EList<AclfBus> busList = ref.getNet().getBusList();
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = busList.get(i);
			double busp = (bus.getGenP() - bus.getLoadP());
			if (busType[i] != SWING) {
				if (busp > 0)
					pf[i] = 1;
				else if (busp < 0)
					pf[i] = -1;
				else
					pf[i] = 0;
			}else {
				pf[i] = busp * pSize;
			}
		}
	}
	
	public void init(double vbase, double vdis, double pvbuff) {
		this.vbase = vbase;
		this.vdis = vdis;
		this.pvbuff = pvbuff;
	}

	@Override
	public VoltageGenCondition nowCondition() {
		return c;
	}

	@Override
	public VoltageGenCondition nextCondition() {
		double[] p = new double[noBus];
		double[] v = new double[noBus];
		for (int i=0; i<noBus; ++i) {
			if (i != swingNo) {
				p[i] = pf[i] * Math.abs(nextRandom());
				v[i] = vbase + vdis * nextRandom() + pvbuff * Math.random();
			}else {
				v[i] = vbase + pvbuff * Math.random();
			}
		}
		p = regulized(p);
		
		c.setP(p);
		c.setV(v);
		
		return c;
	}
	
	private double[] regulized(double[] p) {
		double sum = 0;
		for (int i=0; i<noBus; ++i) {
			if (i != noBus)
				sum += p[i];
		}
		
		if (sum > p[swingNo]) {
			double f = p[swingNo] / sum;
			for (int i=0; i<noBus; ++i)
				if (i != swingNo)
					p[i] *= f;
		}		
		
		return p;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT+"-vbase-"+vbase+"-vdis-"+vdis+"-pvbf-"+pvbuff;
	}
	
	private double nextRandom() {
		double dis = random.nextGaussian();
		while (dis < -1 || dis > 1)
			dis = random.nextGaussian();
		return dis;
	}

}
