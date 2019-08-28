package lfgen.datatype;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.numeric.sparse.ISparseEqnComplex;
import org.interpss.numeric.sparse.ISparseEqnDouble;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

/**
* @author JeremyChenlk
* @version 2019年2月9日 下午4:17:48
*
* Class description:
*	用来概括参考网络的信息
*	想法是：给定了一个参考网络，那生成过程中的什么量就确定不变了，这些量就放在这里
*/

public class RefInfo {
	
	public static int SWING_BUS_TYPE = 0;
	public static int ONLY_PV_BUS_TYPE = 1;
	public static int CONNECT_BUS_TYPE = 2;
	public static int OTHER_BUS_TYPE = 3;
	public static int PV_PQ_BUS_TYPE = 4;
	
	private int noBus = 0;
	private int swingNo = 0;
	private int[] busType = null;
	private String[] busCode = null;
	private double[][] b = null;
	private double[][] br = null;
	private double[][] b1 = null;
	private double[][] b1r = null;
	private double[][] b11 = null;
	private double[][] b11r = null;
	private Complex[][] y = null;
	private Complex[][] yr = null;
	
	/**
	 * 预防有什么没想到的信息还要从这里获取
	 */
	private AclfNetwork net = null;
	
	/**
	 * 需要存储无功限制向量来加速
	 * 因为网络是确定的，节点的无功限制也是确定的
	 */
	private double[] maxQGenLimit = null;
	private double[] minQGenLimit = null;
	
	public AclfNetwork getNet() {
		return net;
	}
	
	private boolean printReport = false;

	public boolean isPrintReport() {
		return printReport;
	}

	public void setPrintReport(boolean printReport) {
		this.printReport = printReport;
	}

	public RefInfo(AclfNetwork refNet) throws InterpssException {

		//report
		System.out.println("[REPORT] new NetInfoImpl...");
		
		this.printReport = true;
		
		this.net = refNet;
		this.noBus = refNet.getNoBus();
		
		this.busType = new int[noBus];
		this.busCode = new String[noBus];
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = refNet.getBusList().get(i);
			String genCode = bus.getGenCode().getName();
			if (genCode.equals("Swing")) {//0 - 平衡节点
				this.busType[i] = RefInfo.SWING_BUS_TYPE;
				this.swingNo = i;
			}else if (genCode.equals("GenPV")) {//1 - 纯PV节点
				if (Complex.equals(bus.getLoadPQ(), Complex.ZERO))
					this.busType[i] = RefInfo.ONLY_PV_BUS_TYPE;
				else
					this.busType[i] = RefInfo.PV_PQ_BUS_TYPE;
			}else if (genCode.equals("GenPQ") && bus.getGenP() == 0 && bus.getGenQ() == 0 && bus.getLoadP() == 0 && bus.getLoadQ() == 0) {//2 - 联络节点
				this.busType[i] = RefInfo.CONNECT_BUS_TYPE;
			}else//3 - 其他PQ节点
				this.busType[i] = RefInfo.OTHER_BUS_TYPE;
			this.busCode[i] = genCode;
		}
		
		/*
		 * get Y matrix
		 * AX=B: 
		 * 	A--Y
		 * 	X--0
		 * 	B--0
		 */
		this.y = new Complex[noBus][noBus];
		this.b = new double[noBus][noBus];
		this.yr = new Complex[noBus][noBus];
		ISparseEqnComplex yeqn = refNet.formYMatrix();
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j) {
				this.y[i][j] = yeqn.getA(i, j);
				this.b[i][j] = y[i][j].getImaginary();
			}
		for (int i=0; i<noBus; ++i) {
			//按列求逆
			for (int j=0; j<noBus; ++j)
				yeqn.setBi(new Complex(0.0, 0.0), j);
			yeqn.setBi(new Complex(1.0, 0.0), i);
			
			try {
				yeqn.solveEqn();
			} catch (IpssNumericException e) {
				e.printStackTrace();
			}
			
			for (int j=0; j<refNet.getNoBus(); ++j)
				this.yr[j][i] = yeqn.getX(j);
		}
		
		/*
		 * get B matrix
		 * AX=B: 
		 * 	A--B
		 * 	X--0
		 * 	B--0
		 */
		this.b1 = new double[noBus][noBus];
		this.b1r = new double[noBus][noBus];
		ISparseEqnDouble beqn = refNet.formB1Matrix();
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j)
				this.b1[i][j] = beqn.getAij(i, j);
		for (int i=0; i<noBus; ++i) {
			//按列求逆
			for (int j=0; j<noBus; ++j)
				beqn.setBi(0.0, j);
			beqn.setBi(1, i);
			
			try {
				beqn.solveEqn();
			} catch (IpssNumericException e) {
				e.printStackTrace();
			}
			
			for (int j=0; j<noBus; ++j)
				this.b1r[j][i] = beqn.getXi(j);
		}
		this.br = new double[noBus][noBus];
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j)
				beqn.setAij(b[i][j], i, j);
		for (int i=0; i<noBus; ++i) {
			//按列求逆
			for (int j=0; j<noBus; ++j)
				beqn.setBi(0.0, j);
			beqn.setBi(1, i);
			
			try {
				beqn.solveEqn();
			} catch (IpssNumericException e) {
				e.printStackTrace();
			}
			
			for (int j=0; j<noBus; ++j)
				this.br[j][i] = beqn.getXi(j);
		}

		/*
		 * get B matrix
		 * AX=B: 
		 * 	A--B
		 * 	X--0
		 * 	B--0
		 */
		this.b11 = new double[noBus][noBus];
		this.b11r = new double[noBus][noBus];
		ISparseEqnDouble b11eqn = refNet.formB11Matrix();
		for (int i=0; i<noBus; ++i)
			for (int j=0; j<noBus; ++j)
				this.b11[i][j] = b11eqn.getAij(i, j);
		for (int i=0; i<noBus; ++i) {
			//按列求逆
			for (int j=0; j<noBus; ++j)
				b11eqn.setBi(0.0, j);
			b11eqn.setBi(1, i);
			
			try {
				b11eqn.solveEqn();
			} catch (IpssNumericException e) {
				e.printStackTrace();
			}
			
			for (int j=0; j<noBus; ++j)
				this.b11r[j][i] = b11eqn.getXi(j);
		}
		
		maxQGenLimit = new double[noBus];
		minQGenLimit = new double[noBus];
		EList<AclfBus> busList = refNet.getBusList();
		for (int i=0; i<noBus; ++i) {
			AclfBus bus = busList.get(i);
			if (busType[i] == RefInfo.ONLY_PV_BUS_TYPE || busType[i] == RefInfo.PV_PQ_BUS_TYPE/*PV节点*/) {
				maxQGenLimit[i] = bus.getQGenLimit().getMax();
				minQGenLimit[i] = bus.getQGenLimit().getMin();
			}else {
				maxQGenLimit[i] = -1;
				minQGenLimit[i] = -1;
			}
		}
	}

	public int getNoBus() {
		return noBus;
	}

	public int getSwingNo() {
		return swingNo;
	}

	public int[] getBusType() {
		return busType;
	}

	public double[][] getB() {
		return b;
	}

	public double[][] getB1() {
		return b1;
	}

	public double[][] getBr() {
		return br;
	}

	public double[][] getB1r() {
		return b1r;
	}

	public double[][] getB11() {
		return b11;
	}

	public double[][] getB11r() {
		return b11r;
	}

	public String[] getBusCode() {
		return busCode;
	}

	public Complex[][] getY() {
		return y;
	}

	public Complex[][] getYr() {
		return yr;
	}

	public double[] getMaxQGenLimit() {
		return maxQGenLimit;
	}

	public double[] getMinQGenLimit() {
		return minQGenLimit;
	}

}
