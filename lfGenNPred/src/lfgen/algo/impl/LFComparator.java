package lfgen.algo.impl;

import org.apache.commons.math3.complex.Complex;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;

/**
* @author JeremyChenlk
* @version 2019年1月26日 下午10:58:40
*
* Class description:
*	
*/

public class LFComparator {
	
	public static final double V_MIN = 0.5;
	public static final double V_MAX = 5;
	
	public static final double TH_ABS_MAX = Math.PI;

	public static final double P_ABS_MAX = 5;
	public static final double Q_ABS_MAX = 4;
	
	public static final double CONNECT_BUS_PQ_ERROR = 1e-10;
	
	public LFComparator() {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean checkCase(RefInfo ref, AclfCase aclfCase) {
		boolean ok = true;
		//System.out.println("[DEBUG] checkCase started, case ID = "+aclfCase.toString());
		ok &= checkPQ(ref, aclfCase.getPower());
		ok &= checkVTh(ref, aclfCase.getVoltage());
		//System.out.println("\tcheckCase end, case ID = "+aclfCase.toString());
		return ok;
	}
	
	private static boolean checkVTh(RefInfo ref, Complex[] voltage) {
		boolean vok = true;
		boolean thok = true;
		double[] v = new double[ref.getNoBus()];
		double[] th = new double[ref.getNoBus()];
		for (int i=0; i<ref.getNoBus(); ++i) {
			v[i] = voltage[i].abs();
			th[i] = voltage[i].getArgument();
		}
		
		//System.out.println("[DEBUG] checkVTH started, V_MAX/ V_MIN = "+V_MAX+", "+V_MIN+"; TH_ABS_MAX = "+TH_ABS_MAX);
		for (int i=0; i<ref.getNoBus(); ++i) {
			if (v[i] > V_MAX || v[i] < V_MIN) {
//				System.out.println("[DEBUG] checkVTH at bus "+i+", V overflow = "+v[i]);
				vok = false;
			}
			if (Math.abs(th[i]) > TH_ABS_MAX) {
				System.out.println("[DEBUG] checkVTH at bus "+i+", th overflow = "+th[i]);
				thok = false;
			}
		}
//		if (!Complex.equals(voltage[ref.getSwingNo()], Complex.ONE)) {
//			vok = false;
//			System.out.println("[DEBUG] checkVTH at bus Swing, Vth !=1,0, = "+voltage[ref.getSwingNo()]);
//		}
		
//		if (vok && thok) {
//			System.out.println("\tcheckVTH passed.");
//		}
		
		return vok && thok;
	}

	private static boolean checkPQ(RefInfo ref, Complex[] power) {
		boolean pok = true;
		boolean qok = true;
		double[] p = new double[ref.getNoBus()];
		double[] q = new double[ref.getNoBus()];
		for (int i=0; i<ref.getNoBus(); ++i) {
			p[i] = power[i].getReal();
			q[i] = power[i].getImaginary();
		}

		//System.out.println("[DEBUG] checkPQ started, P_ABS_MAX = "+P_ABS_MAX+"; Q_ABS_MAX = "+Q_ABS_MAX);
		for (int i=0; i<ref.getNoBus(); ++i) {
			if (Math.abs(p[i]) > P_ABS_MAX) {
//				System.out.println("[DEBUG] checkPQ at bus "+i+", P overflow = "+p[i]);
				pok = false;
			}
			if (Math.abs(q[i]) > Q_ABS_MAX) {
//				System.out.println("[DEBUG] checkPQ at bus "+i+", Q overflow = "+q[i]);
				qok = false;
			}
			if (ref.getBusType()[i] == RefInfo.ONLY_PV_BUS_TYPE) {
				if (q[i] > ref.getMaxQGenLimit()[i] || q[i] < ref.getMinQGenLimit()[i]) {
					System.out.println("[DEBUG] checkPQ at ONLY_PV bus "+i+", Q limit overflow = "+q[i]
							+", limit max/min = "+ref.getMaxQGenLimit()[i]+", "+ref.getMinQGenLimit()[i]);
					qok = false;
				}
			}else if (ref.getBusType()[i] == RefInfo.CONNECT_BUS_TYPE) {
				if (power[i].abs() > CONNECT_BUS_PQ_ERROR) {
					pok = qok = false;
					System.out.println("[DEBUG] checkPQ at CONNECT bus "+i+", PQ !=0, = "+power[i]);
				}
			}
			
		}

//		if (pok && qok) {
//			System.out.println("\tcheckPQ passed.");
//		}
		return pok && qok;
	}

	public static void watch(AclfNetwork net, String statement) throws InterpssException {
		int noBus = net.getNoBus();
		double[] p = new double[noBus];
		double[] q = new double[noBus];
		double[] v = new double[noBus];
		double[] th = new double[noBus];

		for (int i=0; i<noBus; ++i) {
			AclfBus bus = net.getBusList().get(i);
			p[i] = bus.getGenP() - bus.getLoadP();
			q[i] = bus.getGenQ() - bus.getLoadQ();
			v[i] = bus.getVoltageMag();
			th[i] = bus.getVoltageAng();
		}
		
		//report
		String report = new String(" P");
		for (int i=0; i<noBus; ++i) {
			report += "\t" + p[i];
		}
		report += "\n Q";
		for (int i=0; i<noBus; ++i) {
			report += "\t" + q[i];
		}
		report += "\n V";
		for (int i=0; i<noBus; ++i) {
			report += "\t" + v[i];
		}
		report += "\n th";
		for (int i=0; i<noBus; ++i) {
			report += "\t" + th[i];
		}
		report += "\n";
		
		System.out.println("[REPORT] watch... "+statement);
		System.out.println(report);
		
	}
	
	/**
	 * compare the status of an AclfNetwork between before and after load flow
	 * @param net
	 * @throws InterpssException 
	 */
	public static boolean compareWatch(AclfNetwork net, String statement) throws InterpssException {
		
		int noActiveBus = net.getNoActiveBus();
		double[] preP = new double[noActiveBus];
		double[] preQ = new double[noActiveBus];
		double[] preV = new double[noActiveBus];
		double[] preTh = new double[noActiveBus];
		double[] afterP = new double[noActiveBus];
		double[] afterQ = new double[noActiveBus];
		double[] afterV = new double[noActiveBus];
		double[] afterTh = new double[noActiveBus];
		
		//get variables
		for (int i=0; i<noActiveBus; ++i) {
			AclfBus bus = net.getBusList().get(i);
			preP[i] = bus.getGenP() - bus.getLoadP();
			preQ[i] = bus.getGenQ() - bus.getLoadQ();
			preV[i] = bus.getVoltageMag();
			preTh[i] = bus.getVoltageAng();
		}
		
		//load flow
		CoreObjectFactory.createLoadflowAlgorithm(net).loadflow();
		
		//get variables
		for (int i=0; i<noActiveBus; ++i) {
			AclfBus bus = net.getBusList().get(i);
			afterP[i] = bus.getGenP() - bus.getLoadP();
			afterQ[i] = bus.getGenQ() - bus.getLoadQ();
			afterV[i] = bus.getVoltageMag();
			afterTh[i] = bus.getVoltageAng();
		}
		
		//compare and report
		System.out.println("[REPORT] compareWatch... "+statement);
		boolean f1 = compare(preP, afterP, "Bus P");
		boolean f2 = compare(preQ, afterQ, "Bus Q");
		boolean f3 = compare(preV, afterV, "Bus V");
		boolean f4 = compare(preTh, afterTh, "Bus Theta");
		return f1 && f2 && f3 && f4;
	}
	
	private static boolean compare(double[] a, double[] b, String arrayName) {
		boolean theSame = true;
		for (int i=0; i<a.length; ++i) {
			if (a[i] != b[i])
				theSame = false;
		}
		
		if (theSame) {
			System.out.println("  ======"+arrayName+"... are the same.");
			String report = new String("");
			for (int i=0; i<a.length; ++i) {
				report += "\t" + a[i];
			}
			System.out.println(report);
		}else {
			System.out.println("  ======"+arrayName+"... are not the same =======");
			String report = new String("  before");
			for (int i=0; i<a.length; ++i) {
				report += "\t" + a[i];
			}
			report += "\n  after ";
			for (int i=0; i<b.length; ++i) {
				report += "\t" + b[i];
			}
			//report += "\n";
			System.out.println(report);
		}
		
		return theSame;
	}

}
