package lfgen.datatype;

import org.apache.commons.math3.complex.Complex;

/**
* @author JeremyChenlk
* @version 2019年2月10日 下午5:11:18
*
* Class description:
*	
*/

public class VoltageGenCondition {
	
	private static RefInfo ref = null;
	
	private double[] p = null;

	private double[] v = null;
	
	private Complex[] voltage = null;

	/**
	 * 指示类中的数组存的是数值还是因数
	 */
	private boolean pIsFactor = false;

	private boolean vIsFactor = false;

	/**
	 * 是否属于直接给定Vtheta的，否则属于PV方法。
	 */
	private boolean isThVMethod = false;
	
	private int bitState = 0;

	/**
	 * 是否是位运算方法（TraversalVoltageGenerator独有）
	 */
	private boolean isBit = false;
	
	public VoltageGenCondition(boolean isThVMethod) {
		BuildCondition(isThVMethod, null, false, null, false, false, 0, true);
	}
	
	public VoltageGenCondition(boolean isThVMethod, int bitState) {
		BuildCondition(isThVMethod, null, false, null, false, true, bitState, true);
	}

	public VoltageGenCondition(boolean isThVMethod, double[] p, boolean pIsFactor) {
		BuildCondition(isThVMethod, p, pIsFactor, null, false, false, 0, false);
	}
	
	public VoltageGenCondition(boolean isThVMethod, double[] p, boolean pIsFactor, double[] v, boolean vIsFactor, boolean isThV) {
		BuildCondition(isThVMethod, p, pIsFactor, v, vIsFactor, isThV, 0, false);
	}
	
	public VoltageGenCondition(boolean isThVMethod, double[] p, boolean pIsFactor, double[] v, boolean vIsFactor, boolean isThV, int bitState, boolean isBit) {
		BuildCondition(isThVMethod, p, pIsFactor, v, vIsFactor, isThV, bitState, isBit);
	}
	
	private void BuildCondition(boolean isThVMethod, double[] p, boolean pIsFactor, double[] v, boolean vIsFactor, boolean isThV, int bitState, boolean isBit) {
		if (p != null) 
			this.p = p;
		else
			this.p = new double[ref.getNoBus()];
		this.pIsFactor = pIsFactor;
		if (v != null)
			this.v = v;
		else
			this.v = new double[ref.getNoBus()];
		this.voltage = new Complex[ref.getNoBus()];
		this.vIsFactor = vIsFactor;
		this.isThVMethod = isThVMethod;
		this.bitState = bitState;
		this.isBit = isBit;
	}

	public double[] getP() {
		return p;
	}

	public void setP(double[] p) {
		this.p = p;
	}

	public void setPi(double p, int index) {
		this.p[index] = p;
	}
	public double[] getV() {
		return v;
	}

	public void setV(double[] v) {
		this.v = v;
	}
	
	public Complex[] getVoltage() {
		return voltage;
	}

	public void setVoltage(Complex[] voltage) {
		this.voltage = voltage;
	}

	public void setVi(double v, int index) {
		this.v[index] = v;
	}
	public boolean pIsFactor() {
		return pIsFactor;
	}

	public boolean vIsFactor() {
		return vIsFactor;
	}

	public boolean isThVMethod() {
		return isThVMethod;
	}

	public int getBitState() {
		return bitState;
	}
	
	public void setBitState(int bitState) {
		this.bitState = bitState;
	}
	
	public boolean isBit() {
		return isBit;
	}
	
	public static void init(RefInfo ref) {
		VoltageGenCondition.ref = ref;
	}
}
