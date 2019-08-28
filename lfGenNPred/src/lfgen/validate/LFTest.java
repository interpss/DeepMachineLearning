package lfgen.validate;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.fadapter.IpssFileAdapter;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.numeric.sparse.ISparseEqnComplex;
import org.interpss.pssl.simu.IpssDclf;
import org.interpss.pssl.simu.IpssDclf.DclfAlgorithmDSL;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGen;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.core.dclf.DclfAlgorithm;
import com.interpss.core.dclf.common.ReferenceBusException;

import lfgen.datatype.RefInfo;
import lfgen.output.TimeUseBetweenLFAndSub;
import lfgen.platform.LoadFlowCaseGenerator;

/**
* @author JeremyChenlk
* @version 2019年1月27日 下午5:00:17
*
* Class description:
*	
*/

public class LFTest {

	public LFTest() {
		// TODO Auto-generated constructor stub
	}
	
	public void seeBusCodeAndQLimit() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		
		for (int i=0; i<net.getNoBus(); ++i) {
			String report = new String("");
			
			report += " Bus "+i+" code = "+net.getBusList().get(i).getGenCode().getName();
			report += "\t==GenPV?: "+net.getBusList().get(i).getGenCode().getName().equals("GenPV")+Complex.equals(net.getBusList().get(i).getLoadPQ(), Complex.ZERO);
			report += "\tGenQ = " + net.getBusList().get(i).getGenQ();
			report += "\tLoadQ = " + net.getBusList().get(i).getLoadQ();
			report += "\tminQ, maxQ = " + net.getBusList().get(i).getQGenLimit().getMin()+", "
						+net.getBusList().get(i).getQGenLimit().getMax();
			report += "\tVLimit"+net.getBusList().get(i).getVLimit().getMin()+", "+net.getBusList().get(i).getVLimit().getMax();
			
			System.out.println(report);
		}
	}
	
	public void seeOverrideTitle() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		int maxIterTimes = 10;
		double qLimitSparseFactor = 0.9;
		double boundaryFactor=0.5;
		TimeUseBetweenLFAndSub g = new TimeUseBetweenLFAndSub(baseCasePath);
		g.init();
		
		System.out.println(g.getReportTitle());
		
	}
	
	public void seeBranch() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		
		for (int i=0; i<net.getBranchList().size(); ++i) {
			AclfBranch branch = net.getBranchList().get(i);
			String report = new String("");
			report += " Branch "+i+"\n"+branch.toString();
			
			System.out.println(report);
		}
	}
	
	public void seePowerIntoNet() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		
		for (int i=0; i<net.getNoBus(); ++i) {
			System.out.println(" Bus "+i+"\tcurrent = "+net.getBusList().get(i).currentIntoNet());
		}
	}
	
	public void seeBase() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();

		System.out.println(" Base kVA = "+net.getBaseKva());
		System.out.println(" Base MVA = "+net.getBaseMva());
		
		for (int i=0; i<net.getNoBus(); ++i) {
			System.out.println(" Base V in bus "+i+" = "+net.getBusList().get(i).getBaseVoltage());
		}
		
		ISparseEqnComplex yeqn = net.formYMatrix();
		//get y matrix
		Complex[][] y = new Complex[net.getNoBus()][net.getNoBus()];
		for (int i=0; i<net.getNoBus(); ++i)
			for (int j=0; j<net.getNoBus(); ++j) {
				y[i][j] = new Complex(0, 0);
				y[i][j] = yeqn.getA(i, j);
			}

		//debug
		System.out.println(" Y: ");
		for (int i=0; i<net.getNoBus(); ++i) {
			for (int j=0; j<net.getNoBus(); ++j) {
				System.out.print(y[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	public void seeBitCal() {
		System.out.println(1<<26 & 1);
	}
	
	public void seeDclf() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		
		DclfAlgorithmDSL algoDsl = IpssDclf.createDclfAlgorithm(net);
		try {
			algoDsl.runDclfAnalysis();
		} catch (ReferenceBusException e1) {
			e1.printStackTrace();
		} catch (IpssNumericException e1) {
			e1.printStackTrace();
		}
		DclfAlgorithm algo = algoDsl.getAlgorithm();
		try {
			algo.calculateDclf();
		} catch (ReferenceBusException e) {
			e.printStackTrace();
		} catch (IpssNumericException e) {
			e.printStackTrace();
		}
		System.out.println("algo==null "+algo);
		//EList<AclfBus> busList = algo.getNetwork().getBusList();
		
		double[] ang = new double[net.getNoBus()];
		for (int i=0; i<net.getNoBus(); ++i) {
			ang[i] = algo.getBusAngle(i);//busList.get(i).getVoltageAng();
		}
		for (int i=0; i<net.getNoBus(); ++i) {
			System.out.println("Bus "+i+"\tangle = "+ang[i]);
		}
	}
	
	public void seeComplex() {
		//所以，要用类自己的equals
		Complex a = new Complex(0.0, 0.0);
		Complex b = new Complex(0, 0);
		System.out.println("a(0.0, 0.0) equals b(0, 0) ? "+Complex.equals(a, b)+"\t ==? "+(a==b));
		
		Complex c = a;
		c = new Complex(1.0, 0);
		System.out.println("Complex.equals(a, c)?"+Complex.equals(a, c)+"\t a = "+a+"\t c = "+c);
		
		//以下说明Complex因为没有自己改变自己的方法，所以赋值跟double赋值一样
		Complex[] aa = new Complex[1];
		aa[0] = a;
		Complex[] bb = new Complex[1];
		bb = aa;
		System.out.println("aa[0] = a, aa[0] = "+aa[0]+"\t a = "+a);
		a = new Complex(2, 3);
		System.out.println("           aa[0] = "+aa[0]+"\t a = "+a);
		
		//以下说明Complex[] 指针的机制跟double[]一样
		aa[0] = a;
		System.out.println("           aa[0] = "+aa[0]+"\t bb[0] = "+bb[0]);

		System.out.println("从下面可以看出，Swing的V用Complex.ONE, 初始化复数用Complex.ZERO");
		System.out.println("Complex.I = "+Complex.I);
		System.out.println("Complex.ONE = "+Complex.ONE);
		System.out.println("Complex.INF = "+Complex.INF);
		System.out.println("Complex.ZERO = "+Complex.ZERO);
		System.out.println("Complex.NaN = "+Complex.NaN);
		Complex cpx = Complex.ZERO;
		System.out.println("Complex cpx = Complex.ZERO, cpx = "+cpx);
		System.out.println("	hash: Complex.ZERO = "+((Object)Complex.ZERO).hashCode()+", cpx = "+((Object)cpx).hashCode());
		cpx = cpx.add(new Complex(1, 2));
		System.out.println("cpx = cpx.add(new Complex(1, 2)), cpx = "+cpx);
		System.out.println("	hash: new Complex(1, 2) = "+((Object)new Complex(1, 2)).hashCode()+", cpx = "+((Object)cpx).hashCode());
		System.out.println("	hash: new Complex(2, 3) = "+((Object)new Complex(2, 3)).hashCode()+", cpx = "+((Object)cpx).hashCode());
		cpx = new Complex(2, 3);
		System.out.println("cpx = new Complex(2, 3), cpx = "+cpx);
		System.out.println("	hash: new Complex(2, 3) = "+((Object)new Complex(1, 2)).hashCode()+", cpx = "+((Object)cpx).hashCode());
		
		
	}
	
	public void seePointer() {
		//说明指针可以不用new而直接赋值
		double[] p = new double[] {9};
		double[] p1 = new double[1];
		p = p1;
		p1[0] = 1.1;
		System.out.println(p[p.length-1]);
	}
	
	public void seeSetVoltage() throws InterpssException {

		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();

		System.out.println(AclfOutFunc.loadFlowSummary(net));
		
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);

		for (int i=0; i<net.getNoBus(); ++i) {
			System.out.println(" Bus "+i+"\tvoltage = "+net.getBusList().get(i).getVoltage().abs()
					+"\t"+net.getBusList().get(i).getVoltage()
					+"\t"+net.getBusList().get(i).getGenQ());
		}
		for (int i=0; i<net.getNoBus(); ++i) 
			if (net.getBusList().get(i).getGenCode().getName().equals("GenPV"))
				System.out.println(" Bus "+i+"\tvoltage = "+net.getBusList().get(i).getPVBusLimit().getVSpecified());
		
		//对PV节点设置电压幅值的方法
		System.out.println("================= Bus "+1+"\tvoltage = "+net.getBusList().get(1).getVoltage()
				+"\t"+net.getBusList().get(1).getPVBusLimit().getVSpecified()
				+"\t"+net.getBusList().get(1).getGenPQ());
		
		net.getBusList().get(1).getPVBusLimit().setVSpecified(1.1);
		net.getBusList().get(1).setVoltage(new Complex(1.1, 0));
		net.getBusList().get(1).setGenP(1);

		System.out.println("================= Bus "+1+"\tvoltage = "+net.getBusList().get(1).getVoltage()
				+"\t"+net.getBusList().get(1).getPVBusLimit().getVSpecified()
				+"\t"+net.getBusList().get(1).getGenPQ());
		
		algo.loadflow();
		
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		

		for (int i=0; i<net.getNoBus(); ++i) {
			System.out.println("after.., Bus "+i+"\tvoltage = "+net.getBusList().get(i).getVoltage().abs()
					+"\t"+net.getBusList().get(i).getVoltage()
					+"\t"+net.getBusList().get(i).getGenQ());
		}
		for (int i=0; i<net.getNoBus(); ++i) 
			if (net.getBusList().get(i).getGenCode().getName().equals("GenPV"))
				System.out.println(" Bus "+i+"\tvoltage = "+net.getBusList().get(i).getPVBusLimit().getVSpecified());
	}
	
	public void callDaddy() {
		System.out.println("叫爸爸！\n:爸爸！\n叫妈妈\n：娘！！！");
	}
	
	public void seeMatrix() throws InterpssException {
		//验证了B^-1矩阵是对的
		String baseCasePath = "testdata/cases/ieee14.ieee";
		
		IpssCorePlugin.init();
		
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		
		RefInfo ref = new RefInfo(net);
		int noBus = net.getNoBus();

		double[][] b = ref.getB();
		System.out.println("====================== B Matrix ======================");
		printMatrix(b, noBus);
		
		double[][] br = ref.getBr();
		System.out.println("====================== B^-1 Matrix ======================");
		printMatrix(br, noBus);

		double[][] bbr = new double[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				for (int k=0; k<noBus; ++k)
					bbr[i][j] += b[i][k] * br[k][j];
				if (Math.abs(bbr[i][j] - 1) < 1e-10)
					bbr[i][j] = 1.0;
				else if (Math.abs(bbr[i][j]) < 1e-10)
					bbr[i][j] = 0.0;
			}
		System.out.println("====================== B*B^-1 Matrix ======================");
		printMatrix(bbr, noBus);

		double[][] b1 = ref.getB1();
		System.out.println("====================== B1 Matrix ======================");
		printMatrix(b1, noBus);
		
		double[][] b1r = ref.getB1r();
		System.out.println("====================== B1^-1 Matrix ======================");
		printMatrix(b1r, noBus);

		double[][] b1b1r = new double[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				for (int k=0; k<noBus; ++k)
					b1b1r[i][j] += b1[i][k] * b1r[k][j];
				if (Math.abs(b1b1r[i][j] - 1) < 1e-10)
					b1b1r[i][j] = 1.0;
				else if (Math.abs(b1b1r[i][j]) < 1e-10)
					b1b1r[i][j] = 0.0;
			}
		System.out.println("====================== B1*B1^-1 Matrix ======================");
		printMatrix(b1b1r, noBus);

		double[][] b11 = ref.getB11();
		System.out.println("====================== B11 Matrix ======================");
		printMatrix(b11, noBus);
		
		double[][] b11r = ref.getB11r();
		System.out.println("====================== B11^-1 Matrix ======================");
		printMatrix(b11r, noBus);

		double[][] b11b11r = new double[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				for (int k=0; k<noBus; ++k)
					b11b11r[i][j] += b11[i][k] * b11r[k][j];
				if (Math.abs(b11b11r[i][j] - 1) < 1e-10)
					b11b11r[i][j] = 1.0;
				else if (Math.abs(b11b11r[i][j]) < 1e-10)
					b11b11r[i][j] = 0.0;
			}
		System.out.println("====================== B11*B11^-1 Matrix ======================");
		printMatrix(b11b11r, noBus);
		
		Complex[][] y = ref.getY();
		System.out.println("====================== Y Matrix ======================");
		printMatrix(y, noBus);
		
		Complex[][] yr = ref.getYr();
		System.out.println("====================== Y^-1 Matrix ======================");
		printMatrix(yr, noBus);
		
		Complex[][] yyr = new Complex[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				yyr[i][j] = new Complex(0.0, 0.0);
				if (i != 0 && j != 0)
				for (int k=0; k<noBus; ++k)
					yyr[i][j] = yyr[i][j].add(y[i][k].multiply(yr[k][j]));
				if (yyr[i][j].subtract(Complex.ONE).abs() < 1e-10)
					yyr[i][j] = Complex.ONE;
				else if (yyr[i][j].abs() < 1e-10)
					yyr[i][j] = Complex.ZERO;
			}
		System.out.println("====================== Y*Y^-1 Matrix ======================");
		printMatrix(yyr, noBus);
		
		
	}
	
	public void seePrintf() {
		//report 
		System.out.printf("[REPORT] %20s time use(s) = %10.1f, in which init time use(s) = %8.3f\n", "LoadFlowGenerator", 23579/1000.0, 599/1000.0);
	}
	
	public void seeContributedLnG() throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		IpssCorePlugin.init();
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		for (AclfBus bus:net.getBusList()) {
			System.out.println("Bus "+bus.getId());
			for (AclfGen gen:bus.getContributeGenList()) {
				System.out.println("Gen "+gen.getId()+" toString ="+gen.toString());
			}
			for (AclfLoad load:bus.getContributeLoadList()) {
				System.out.println("Load "+load.getId()+" toString ="+load.toString());
			}
		}
		
		
	}
	
	public void seeLF() throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		IpssCorePlugin.init();
		AclfNetwork net = CorePluginFactory
							.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
							.load(baseCasePath)
							.getAclfNet();
		
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		
		algo.loadflow();
		
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		LFOut.showlf(net);
		
		for (AclfBranch b:net.getBranchList()) {
			System.out.println(b.getFromBus().getId()+"\t"+b.getToBus().getId());
		}
	}
	
	public static void main(String[] args) throws InterpssException {
		LFTest t = new LFTest();
		
//		t.seeBusCodeAndQLimit();
//		t.seeOverrideTitle();
//		t.seeQLimit();
//		t.seeBranch();
//		t.seePowerIntoNet();
//		t.seeBase();
//		t.seeBitCal();
//		t.seeDclf();
//		t.seeComplex();
//		t.seePointer();
//		t.seeSetVoltage();
//		t.callDaddy();
//		t.seeMatrix();
//		t.seePrintf();
//		t.seeContributedLnG();
		t.seeLF();
	}
	
	private void printMatrix(Complex[][] m, int n) {
		String str = new String("");
		for (int i=0; i<n; ++i) {
			for (int j=0; j<n; ++j)
				str += "  "+m[i][j];
			str += "\n";
		}
		System.out.println(str);
	}
	
	private void printMatrix(double[][] m, int n) {
		String str = new String("");
		for (int i=0; i<n; ++i) {
			for (int j=0; j<n; ++j)
				str += "  "+m[i][j];
			str += "\n";
		}
		System.out.println(str+"\n");
	}
}
