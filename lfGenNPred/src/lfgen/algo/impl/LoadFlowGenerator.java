 package lfgen.algo.impl;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

import lfgen.algo.ILoadFlowGenerator;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;


public class LoadFlowGenerator extends AlgoObject implements ILoadFlowGenerator {

	public static final String NAME = "LoadFlowGenerator";
	public static final String NAME_IN_SHORT = "LFG";
	public static final String PARA_NEEDED = "NONE";
	
	private int noBus = 0;
	private Complex[][] yc = null;
	
	public LoadFlowGenerator(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.noBus = ref.getNoBus();
		this.yc = new Complex[noBus][noBus];
		
		Complex[][] y = ref.getY();
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) 
				this.yc[i][j] = new Complex(y[i][j].getReal(), 0 - y[i][j].getImaginary());//取共轭
		
		AclfCase.init(this);
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * 生成潮流的核心方法,复杂度为n^2
	 * @param onGoNet
	 * @return net（直接生成后修改到网络）
	 */
	public Complex[] genFlow(Complex[] voltage) {
		long startTime = System.currentTimeMillis();
		addCallTimes();
		
		Complex[] power = new Complex[noBus];
		
		for (int i=0; i<noBus; ++i) {
			power[i] = new Complex(0, 0);
			for (int j=0; j<noBus; ++j) {//Y*V*
				power[i] = power[i].add(this.yc[i][j].multiply(voltage[j].conjugate()));
			}
			power[i] = voltage[i].multiply(power[i]);//VY*V*
		}
		
		addTimeUse(startTime);
		return power;
	}

	public int getNoBus() {
		return noBus;
	}

	public Complex[][] getYc() {
		return yc;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT;
	}

}

