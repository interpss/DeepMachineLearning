package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;

import lfgen.datatype.VoltageGenCondition;
import lfgen.algo.IVoltageGenerator;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年1月24日 下午8:50:43
*
* Class description:
*	1. 初值生成器，提供的方法：
*	给定电压幅值和有功水平生成case
*	给定某个位运算量生成电压和有功水平，再生成case
*	给定有功水平和某个位运算量生成case
*/

public class VoltageGenerator extends AlgoObject implements IVoltageGenerator {

	public static final String NAME = "Default_VoltageGenerator";
	public static final String NAME_IN_SHORT = "dVG";
	public static final String PARA_NEEDED = "NONE";
	
	protected double[][] b1r = null;
	protected int noBus = 0;
	protected int swingNo = 0;
	
	public VoltageGenerator(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.b1r = ref.getB1r();
		this.noBus = ref.getNoBus();
		this.swingNo = ref.getSwingNo();
		
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * 有目的性的生成
	 * 生成电压的主方法，由幅值和有功生成。（因为给定有功一个是符合工程上的需要，一个是符合实际生成中削减空间，提高有效性的必要）
	 * note: 有功水平更为重要，放参数表前面
	 * @param v
	 * @param p
	 * @return
	 */
	@Override
	public Complex[] genVoltage(VoltageGenCondition c) {
		long startTime = System.currentTimeMillis();
		addCallTimes();
		
		Complex[] voltage;
		if (c.isThVMethod()) {
			voltage = c.getVoltage();
		}else {
			voltage = toComplexV(c.getV(), genTheta(c.getP()));
		}
		
		addTimeUse(startTime);
		return voltage;
	}
	
	/**
	 * 根据给定的p，利用直流潮流得到相角，写死的。
	 * P=Bθ => θ = B^-1 * P
	 * @param p
	 * @return
	 */
	private double[] genTheta(double[] p) {
		double[] theta = new double[noBus];
		
		for (int i=0; i<noBus; ++i) {
			theta[i] = 0;
			if (i != swingNo)
				for (int j=0; j<noBus; ++j) 
					if (j != swingNo)
					theta[i] -= this.b1r[i][j] * p[j];
		}
		
		return theta;
	}

	/**
	 * 根据电压幅值和相角生成电压相量的唯一（划重点）方法，只需要在此显式地将swing节点电压改回来就好了
	 * @param v
	 * @param th
	 * @return
	 */
	 private Complex[] toComplexV(double[] v, double[] th) {
		Complex[] voltage = new Complex[noBus];
		for (int i=0; i<noBus; ++i) {
			voltage[i] = new Complex(v[i]*Math.cos(th[i]), v[i]*Math.sin(th[i]));
		}
//		//显式地将swing幅值校正
//		if (!Complex.equals(voltage[this.swingNo], new Complex(1, 0))) {
//			//Debug
//			System.out.println("[DEBUG] VoltageGenerator.toComplexV: voltage[swingNo] != (1, 0), == "+voltage[this.swingNo]+"\t, but it is already fixed to (1, 0)!");
//			String errStr = new String("");
//			for (int i=0; i<noBus; ++i) 
//				errStr += "Bus "+i+" V = "+voltage[i]+"\t";
//			System.out.println(errStr);
//			
//			voltage[this.swingNo] = new Complex(1, 0);
//		}
		return voltage;
	}
	
	public double[][] getB1r() {
		return b1r;
	}

	public int getNoBus() {
		return noBus;
	}

	public int getSwingNo() {
		return swingNo;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT;
	}
}
