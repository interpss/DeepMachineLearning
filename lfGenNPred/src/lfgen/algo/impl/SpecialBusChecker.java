package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;

import lfgen.algo.ISpecialBusChecker;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年1月24日 下午10:32:13
*
* Class description:
* 	用于将调相机节点的有功校正为0
*	用于将联络节点的有功和无功校正为0
*	校正方法：补偿电流
*		0   (已知) = [rY00 rY01 ~][dI0  未知
*		dV1 (未知) = [rY10 rY11 ~][-I1 = (YV)1 已知
*		dV2 (未知) = [rY20 rY21 ~][0  已知
*	故先求dI0再求dV1 dV2
*/

public class SpecialBusChecker extends AlgoObject implements ISpecialBusChecker {

	public static final String NAME = "Default_SpecialBusChecker";
	public static final String NAME_IN_SHORT = "dSBC";
	public static final String PARA_NEEDED = "NONE";
	
	/**
	 * 常数矩阵,这是跟校正有关的具体参数，不保存在RefNetInfo中
	 */
	protected Complex[][] vt = null;
	
	/*
	 * 从ref得到的指针，是避免多层引用和避免值重复的妥协
	 */
	protected int noBus = 0;
	protected int swingNo = 0;
	

	public SpecialBusChecker(RefInfo refInfo) {
		super(refInfo);
		long startTime = System.currentTimeMillis();
		this.methodName = NAME;
		System.out.print("[REPORT] new "+methodName+"...");
		
		this.noBus = ref.getNoBus();
		this.swingNo = ref.getSwingNo();
		
		Complex[][] yr = ref.getYr();
		int[] busType = ref.getBusType();
		//计算m01, m01 = -1/yr00 * yr01, dI0 == m01 * dI1
		Complex[] m01 = new Complex[noBus];
		for (int i=0; i<noBus; ++i) {
			m01[i] = new Complex(0, 0);
			if (busType[i] == RefInfo.CONNECT_BUS_TYPE) //这里说明m01仅在1处有值
				m01[i] = yr[swingNo][i].divide(yr[swingNo][swingNo]).multiply(-1);
		}
		//计算z矩阵, z = yr12,0 * m01 + yr12,1, dV = z * dI
		Complex[][] z = new Complex[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				z[i][j] = new Complex(0.0, 0.0);
				if (i != swingNo && busType[j] == RefInfo.CONNECT_BUS_TYPE) //对i说明z共12行，对j说明z共1列
					z[i][j] = yr[i][swingNo].multiply(m01[j]).add(yr[i][j]);
			}
		
		//生成vt, vt = z * -Y1, dV12 = vt * V012
		this.vt = new Complex[noBus][noBus];
		Complex[][] y = ref.getY();
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				vt[i][j] = new Complex(0.0, 0.0);
				if (i != swingNo)//这里说明vt共1+2行，对j没判断说明vt共0+1+2行
					for (int k=0; k<noBus; ++k)
						if (busType[k] == RefInfo.CONNECT_BUS_TYPE)
							vt[i][j] = vt[i][j].subtract(z[i][k].multiply(y[k][j]));
			}
		
		//report
		System.out.println(" ...ready. Need to input parameter: "+PARA_NEEDED);
		addInitTime(startTime);
	}
	
	/**
	 * 本类的主方法，用于将输入网络的联络节点功率校正为0
	 * 校正后的电压向量同时修改到网络
	 * @param net
	 * @return 校正后的电压向量
	 */
	@Override
	public void correct(AclfCase aclfCase) {
		long startTime = System.currentTimeMillis();
		addCallTimes();
		
		Complex[] voltage = aclfCase.getVoltage();
		
		//矩阵乘法
		Complex[] dV = new Complex[noBus]; 
		for (int i=0; i<noBus; ++i) {
			dV[i] = new Complex(0.0, 0.0);
			if (i != swingNo)
				for (int j=0; j<noBus; ++j)
					dV[i] = dV[i].add(vt[i][j].multiply(voltage[j]));
		}
		
		//correct v += dV, theta = theta
		for (int i=0; i<noBus; ++i) {
			voltage[i] = voltage[i].add(dV[i]);
		}
		
		//显式赋值，触发case.coincide＝false
		aclfCase.setVoltage(voltage);

		addTimeUse(startTime);
	}

	public int getNoBus() {
		return noBus;
	}

	public Complex[][] getVt() {
		return vt;
	}

	@Override
	public String keyMsg() {
		return NAME_IN_SHORT;
	}
}
