package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.numeric.sparse.ISparseEqnComplex;
import org.interpss.numeric.sparse.ISparseEqnDouble;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

import lfgen.algo.IQChecker;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年1月24日 下午10:09:06
*
* Class description:
*	检查节点无功是否越限的方法，如果越限应能提供修正
*	改用B11试一试
*/

public class QChecker extends AlgoObject implements IQChecker {

	public static final String NAME = "Default_QChecker";
	public static final String NAME_IN_SHORT = "dQC";
	public static final String PARA_NEEDED = "qLimitSparseFactor";
	
	protected static final double DEFAULT_Q_SPARSE = 0.95;

	/**
	 * 由于实际中如果校正到极限的话往往再微调又有超限情况发生，所以需要设松弛因子
	 */
	protected double qLimitSparseFactor = 0;
	
	/*
	 * 从ref得到的指针，是避免多层引用和避免值重复的妥协
	 */
	protected int noBus = 0;
	protected double[][] b11r = null;
	protected int swingNo = 0;
	protected String[] busCode = null;
	protected int[] busType = null;
	protected double[] maxQGenLimit = null;
	protected double[] minQGenLimit = null;
	
	public QChecker(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = "Default_QChecker";
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.qLimitSparseFactor = QChecker.DEFAULT_Q_SPARSE;
		
		noBus = ref.getNoBus();
		maxQGenLimit = ref.getMaxQGenLimit();
		minQGenLimit = ref.getMinQGenLimit();
		b11r = ref.getB11r();
		busCode = ref.getBusCode();
		busType = ref.getBusType();
		swingNo = ref.getSwingNo();
		
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * 当遇到越限的情况时调用的方法，应用PQ分解法，用B矩阵的逆求电压修正量
	 * 只修改busCode==GenPV的节点
	 * 已在LFTest()中确认，min/maxQGenLimit 均为标幺值
	 * -dV = br * dQ/V
	 * V = V - dV
	 * TODO：注意！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！这里限制了PQ+PV节点，但我认为只能限制纯PV节点
	 * @param voltage 电压值
	 * @param power 当前功率分布，用来提取无功修正量
	 * @return
	 * @throws InterpssException
	 */
	@Override
	public boolean correct(AclfCase aclfCase) {
		long startTime = System.currentTimeMillis();
		
		Complex[] power = aclfCase.getPower();
		//get dQ[]
		boolean qOverflow = false;
		double[] dQ = new double[noBus];
		for (int i=0; i<noBus; ++i) {
			double q = power[i].getImaginary();
			if (busType[i] == RefInfo.ONLY_PV_BUS_TYPE /*busCode[i].equals("GenPV")*/) {
				if (q > maxQGenLimit[i]) {
					dQ[i] = maxQGenLimit[i] * this.qLimitSparseFactor - q;
					qOverflow = true;
				}else if (q < minQGenLimit[i]) {
					dQ[i] = minQGenLimit[i] * this.qLimitSparseFactor - q;
					qOverflow = true;
				}else
					dQ[i] = 0;
			}else {
				dQ[i] = 0;
			}
		}
		//如果没越限则跳过下面，直接返回没越限就行了
		if (!qOverflow) {
			addTimeUse(startTime);
			return true;
		}
		addCallTimes();
		
		//如果越限了，修正电压
		Complex[] voltage = aclfCase.getVoltage();
		//get dQ/V
		double[] dqv = new double[noBus]; 
		for (int i=0; i<noBus; ++i) {
			dqv[i] = dQ[i] / voltage[i].abs();
		}
		//矩阵乘法  -dV = br * dQ/V
		double[] dV = new double[noBus]; 
		for (int i=0; i<noBus; ++i) {
			dV[i] = 0;
			if (i != swingNo && !busCode[i].equals("GenPV"))//注意到理论上的b11r矩阵是这里的b11r去掉swing和PV行列
				for (int j=0; j<noBus; ++j)
					if (j != swingNo && !busCode[i].equals("GenPV"))
						dV[i] -= b11r[i][j] * dqv[j];
		}
				
		//correct v += dV, theta = theta
		for (int i=0; i<noBus; ++i) 
			if (i != swingNo){
				double v = voltage[i].abs() + dV[i];
				voltage[i] = voltage[i].multiply(v/voltage[i].abs());
			}
		
		aclfCase.setVoltage(voltage);
		
		addTimeUse(startTime);
		return false;
	}

	/**
	 * 支持修改默认值
	 */
	@Override
	public void setQLimitSparseFactor(double qLimitSparseFactor) {
		this.qLimitSparseFactor = qLimitSparseFactor;
		System.out.println("[REPORT] "+methodName+" paramter set. qLimitSparseFactor = "+qLimitSparseFactor);
	}

	public int getNoBus() {
		return noBus;
	}

	public double[][] getB11r() {
		return b11r;
	}

	public String[] getBusCode() {
		return busCode;
	}

	public double[] getMaxQGenLimit() {
		return maxQGenLimit;
	}

	public double[] getMinQGenLimit() {
		return minQGenLimit;
	}

	public double getQLimitSparseFactor() {
		return qLimitSparseFactor;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT+"-qf-"+this.qLimitSparseFactor;
	}
	
}
