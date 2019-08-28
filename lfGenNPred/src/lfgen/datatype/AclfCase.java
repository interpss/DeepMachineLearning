package lfgen.datatype;

import org.apache.commons.math3.complex.Complex;

import lfgen.algo.impl.LoadFlowGenerator;

/**
* @author JeremyChenlk
* @version 2019年2月10日 上午10:48:07
*
* Class description:
*	潮流类
*/

public class AclfCase {
	
	/**
	 * 用于电压发生变化之后，需要调用power时，自动更新power
	 */
	private static LoadFlowGenerator lfg = null;
	
	/**
	 * 样本状态
	 * 0 - 新的V，PV对不上
	 * 1 - V不变，PV对得上
	 */
	private boolean coincide = false;
	
	private Complex[] voltage = null;
	private Complex[] power = null;

	public AclfCase(Complex[] voltage) {
		this.voltage = voltage;
		this.power = new Complex[voltage.length];//应当注意此时是空的
		this.coincide = false;
	}

	public boolean getCoincide() {
		return coincide;
	}

	public Complex[] getVoltage() {
		return voltage;
	}

	public void setVoltage(Complex[] voltage) {
		this.voltage = voltage;
		this.coincide = false;
	}

	public Complex[] getPower() {
		if (!this.coincide) {
			power = lfg.genFlow(voltage);
			coincide = true;
		}
		return power;
	}
	
	@Override
	public String toString() {
		String str = new String("");
		String[] busCode = lfg.getRefInfo().getBusCode();
		for (int i=0; i<voltage.length; ++i) {
			str += "Bus "+i+"\t"+busCode[i]+"\t";
			if (busCode[i].equals("Swing")) {
				str += getVoltage()[i];
			}else if (busCode[i].equals("GenPV")) {
				str += getPower()[i].getReal()+"\t"+getVoltage()[i].abs();
			}else if (busCode[i].equals("GenPQ")) {
				str += getPower()[i];
			}
			str += "\n";
		}
		return str;
	}
	
	/**
	 * 在程序初始化阶段给这个赋值，之后不用显式调用lfg
	 * @param lfg
	 */
	public static void init(LoadFlowGenerator lfg) {
		AclfCase.lfg = lfg;
	}
	
}
