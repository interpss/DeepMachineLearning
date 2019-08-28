package cpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.math3.complex.Complex;
import org.interpss.CorePluginFactory;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.fadapter.IpssFileAdapter;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.algo.LoadflowAlgorithm;

import lfgen.algo.impl.AlgoObject;
import lfgen.datatype.AclfCase;
import lfgen.datatype.RefInfo;
import lfgen.datatype.VoltageGenCondition;
import lfgen.output.TimeUseBetweenLFAndSub;
import lfgen.platform.LoadFlowCaseGenerator;
import lfgen.validate.LFOut;

/**
* @author JeremyChenlk
* @version 2019年4月13日 上午5:32:25
*
* Class description:
*	
*/

public class CpfCaseBuilder14 extends LoadFlowCaseGenerator {

	public static final String NAME = "Cpf";
	public static final String NAME_IN_SHORT = "Cpf";
	public static final String PARA_NEEDED = "NONE";
	
	private AclfNetwork net = null;
	private double k = 0;
	private Complex oBase = null;
	private PrintStream f = null;
	private int caseCount = 0;
	private Complex[] branchY = null;

	public CpfCaseBuilder14(String baseCasePath) throws InterpssException {
		super(baseCasePath);
		long startTime = System.currentTimeMillis();
		platformName = NAME;
		nameInShort = NAME_IN_SHORT;
		System.out.print("[REPORT] new "+platformName+"...");
		
		timeUse = 0;
		initTimeUse = 0;
		callTimes = 0;
		totalIterTimes = 0;
		
		IpssCorePlugin.init();
		net = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();

		AclfNetwork net2 = CorePluginFactory
				.getFileAdapter(IpssFileAdapter.FileFormat.IEEECDF)
				.load(baseCasePath)
				.getAclfNet();
		k = gotoLimit2(net2);

		this.branchY = new Complex[5];
		Complex[][] y = refInfo.getY();
		this.branchY[0] = new Complex(y[0][1].getReal(), y[0][1].getImaginary());
		this.branchY[1] = new Complex(y[0][4].getReal(), y[0][4].getImaginary());
		this.branchY[2] = new Complex(y[1][2].getReal(), y[1][2].getImaginary());
		this.branchY[3] = new Complex(y[1][3].getReal(), y[1][3].getImaginary());
		this.branchY[4] = new Complex(y[1][4].getReal(), y[1][4].getImaginary());
		//report
		System.out.println(" ...ready.");
		addInitTime(startTime);
	}
	
	public void go(int nCase) throws InterpssException {
		long startTime = System.currentTimeMillis();
		//new 
		
		FileOutputStream fs = null;
		SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
		try {
			fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/CpfCase"
					+"-"+dataFormat.format(new Date())+".txt"));
			} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		f = new PrintStream(fs);
		
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		
		for (int i=0; i<nCase; ++i) {
			net.getBusList().get(9).getContributeLoadList().get(0).setLoadCP(oBase.multiply((i+0.0)/nCase * k * 0.4));
			algo.loadflow();
			f.print(outlf());
			if (i % 10000 == 0) {
				System.out.println("case = "+i);
			}
		}
		f.close();
		addTimeUse(startTime);

		System.out.println("Case = "+caseCount);
		System.out.println("nCase = "+nCase);
		System.out.println("Time use = "+this.getTimeUse());
	}
	
	private String outlf() {
		caseCount += 1;
		
//		//report pqvth
		String report = new String("");
		String[] busCode = refInfo.getBusCode();
		for (int i=0; i<refInfo.getNoBus(); ++i) {
			AclfBus bus = net.getBusList().get(i);
			if (busCode[i].equals("Swing")) {
				report += bus.getVoltageMag() +"\t"+ bus.getVoltageAng() +"\t";
			}else if (busCode[i].equals("GenPV")) {
				report += (bus.getGenP()-bus.getLoadP()) +"\t"+ bus.getVoltageMag() +"\t";
			}else {
				report += (bus.getGenP()-bus.getLoadP()) +"\t"+ (bus.getGenQ()-bus.getLoadQ()) +"\t";
			}
		}
		Complex v0 = net.getBusList().get(0).getVoltage();
		Complex v1 = net.getBusList().get(1).getVoltage();
		Complex v2 = net.getBusList().get(2).getVoltage();
		Complex v3 = net.getBusList().get(3).getVoltage();
		Complex v4 = net.getBusList().get(4).getVoltage();
		
		// S = V(YdV)*
		report += v0.multiply((branchY[0].multiply(v0.subtract(v1))).conjugate()).getReal()+"\t";
		report += v0.multiply((branchY[1].multiply(v0.subtract(v4))).conjugate()).getReal()+"\t";
		report += v1.multiply((branchY[2].multiply(v1.subtract(v2))).conjugate()).getReal()+"\t";
		report += v1.multiply((branchY[3].multiply(v1.subtract(v3))).conjugate()).getReal()+"\t";
		report += v1.multiply((branchY[4].multiply(v1.subtract(v4))).conjugate()).getReal()+"\n";
				
		
		return report;
	}
	private double gotoLimit2(AclfNetwork net) throws InterpssException {
		
		int busi = 9;
		System.out.println("============ Go to Limit ============");
		LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
		algo.loadflow();
//		LFOut.showlf(net);
		
		
		AclfBus bus = net.getBusList().get(9);
		Complex base = bus.getLoadPQ();
		oBase = bus.getLoadPQ();
		Complex delta = base.multiply(10);
		Complex result = base;

//		System.out.println("base = "+base+", delta = "+delta);
		bus.getContributeLoadList().get(0).setLoadCP(base.add(delta));
//		System.out.println("\tbus load pq = "+bus.getLoadPQ());
		
		int iter = 0;
		while(delta.abs() > 1e-10) {
			iter += 1;
			//当前单根区间
//			System.out.println("base = "+base+", delta = "+delta);
			
			//试探中点
			delta = delta.multiply(0.5);
			bus.getContributeLoadList().get(0).setLoadCP(base.add(delta));
			//中点坐标
//			System.out.println("bus 9 load pq = "+bus.getLoadPQ());
			
			//若试探成功则...
			if (algo.loadflow()) {
				base = base.add(delta);
				result = base;
//				System.out.println("a success bus 9 load PQ = "+bus.getLoadPQ());
			}
//			System.out.println(AclfOutFunc.loadFlowSummary(net));
		}
		
//		System.out.println("iter = "+iter);
		bus.getContributeLoadList().get(0).setLoadCP(result);
//		System.out.println("after searching, bus 9 load PQ = "+result+", lf result = "+algo.loadflow());
//		System.out.println(AclfOutFunc.loadFlowSummary(net));
		
		return result.divide(oBase).abs();
	}
	
	

	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		CpfCaseBuilder14 g = new CpfCaseBuilder14(baseCasePath);
		g.go(1000000);
	}

	@Override
	protected void successGenReaction(AclfCase aclfCase) {
		// TODO Auto-generated method stub
		
	}

}
