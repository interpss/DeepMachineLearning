package lfgen.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.math3.complex.Complex;

import com.interpss.common.exp.InterpssException;

import lfgen.algo.impl.AlgoObject;
import lfgen.algo.impl.LFComparator;
import lfgen.algo.impl.QChecker;
import lfgen.condition_gen.PVCG2;
import lfgen.condition_gen.PVConditionGenerator;
import lfgen.datatype.AclfCase;
import lfgen.platform.LoadFlowCaseGenerator;

/**
* @author JeremyChenlk
* @version 2019年4月12日 下午8:16:38
*
* Class description:
*	
*/

public class CaseBuilder300 extends LoadFlowCaseGenerator {

	public static final String NAME = "CaseBuilder300";
	public static final String NAME_IN_SHORT = "Case300";
	public static final String PARA_NEEDED = "NONE";
	/**
	 * validation
	 */
	private int nCase = 0;
	private int okCase = 0;
	
	private int caseCount = 0;
	private PrintStream f = null;
	
	private Complex[] branchY = null;

	public CaseBuilder300(String baseCasePath) throws InterpssException {
		super(baseCasePath);
		long startTime = System.currentTimeMillis();
		platformName = NAME;
		nameInShort = NAME_IN_SHORT;
		System.out.print("[REPORT] new "+platformName+"... ...ready.");
		
		System.out.println("[Validation] ON...");
		System.out.println("\tItem 1\tif swing bus V = 1, 0"
				+"\n\tItem 2\tif other v in internal"+LFComparator.V_MIN+", "+LFComparator.V_MAX
				+"\n\tItem 3\tif other th less than LFComparator.TH_ABS_MAX = "+LFComparator.TH_ABS_MAX
				+"\n\tItem 4\tif P less than LFComparator.P_ABS_MAX = "+LFComparator.P_ABS_MAX
				+"\n\tItem 5\tif Q less than LFComparator.Q_ABS_MAX = "+LFComparator.Q_ABS_MAX
				+"\n\tItem 6\tif Q of ONLY_PV bus in internal refInfo.QLimit"
				+"\n\tItem 7\tif PQ of CONNECT bus = 0, 0");
		nCase = 0;
		okCase = 0;
		addInitTime(startTime);
	}
	
	/**
	 * 差异化的初始化方法，仅构造后被显式调用执行一次
	 * 需要继承后自己重写
	 * @throws InterpssException 
	 */
	@Override
	public void init() {
		long startTime = System.currentTimeMillis();
		//“其余照旧”
		super.init();
		//“除了这个”
		voltageGenConditionBuilder = new PVCG2(refInfo);
//		voltageGenConditionBuilder = new PVConditionGenerator(refInfo);
		
		
		this.branchY = new Complex[5];
		Complex[][] y = refInfo.getY();
		this.branchY[0] = new Complex(y[2][149].getReal(), y[2][149].getImaginary());
		this.branchY[1] = new Complex(y[6][130].getReal(), y[6][130].getImaginary());
		this.branchY[2] = new Complex(y[68][210].getReal(), y[68][210].getImaginary());
		this.branchY[3] = new Complex(y[78][210].getReal(), y[78][210].getImaginary());
		this.branchY[4] = new Complex(y[79][210].getReal(), y[79][210].getImaginary());

		//new 
		FileOutputStream fs = null;
		SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
		try {
			fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/Case300"
					+"-"+((AlgoObject) voltageGenConditionBuilder).keyMsg()
					+"-"+((AlgoObject) qChecker).keyMsg()
					+"-"+caseCount
					+"-"+dataFormat.format(new Date())+".txt"));
			} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		f = new PrintStream(fs);
		this.caseCount = 0;
		
		addInitTime(startTime);
	}
	
	@Override
	public void boom() throws InterpssException {
		
		double qbf = 0.95;
		
		((PVCG2)voltageGenConditionBuilder).init(1.0, 0.03, 0.06);
		((QChecker)qChecker).setQLimitSparseFactor(qbf);
		this.setMaxIter(3);
		
		double[] vfactor = new double[] {1};//样例重要性序列
		for (double vf:vfactor) {
			
			this.resetStatistics();
			
			FileOutputStream fs = null;
			SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
			
			try {
				fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/Case300Report"
						+"-"+((AlgoObject)voltageGenConditionBuilder).keyMsg()
						+"-"+((AlgoObject)qChecker).keyMsg()
						+"-"+caseCount
						+"-"+dataFormat.format(new Date())+".txt"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			PrintStream f = new PrintStream(fs);
			f.println(getReportTitle());
			
			int i;
			for (i=0; i<(10000000); ++i) {
				go(voltageGenConditionBuilder.nextCondition());
				//每1m次输出一次，大约是20分钟输出一次
				if (i % 10000 == 0) {
					System.out.println("=================================[REPORT] 第 "+i+"次=================================");
					report();
					System.out.println("===================================================================================");
					f.println(getReportStr());
				}
				if (okCase == 1000000) {
					break;
				}
			}
			
			System.out.println("=================================[REPORT] 第 "+i+"次=================================");
			report();
			System.out.println("===================================================================================");
			f.println(getReportStr());
			
			f.close();
		}
	}
	
	/**
	 * 输出特定的样例
	 * @param onGoNet
	 * @param voltage
	 * @param power
	 */
	@Override
	protected void successGenReaction(AclfCase aclfCase) {
		nCase += 1;
//		if (LFComparator.checkCase(refInfo, aclfCase)) {
			okCase += 1;
			outCaseStr(aclfCase);
//		}
	}
	
	private void outCaseStr(AclfCase aclfCase) {
		caseCount += 1;
		
		Complex[] voltage = aclfCase.getVoltage();
		Complex[] power = aclfCase.getPower();
		if (caseCount % 100000 == 0) {
			f.close();
			//new 
			FileOutputStream fs = null;
			SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
			try {
				fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/PVCase"
						+"-"+((AlgoObject) voltageGenConditionBuilder).keyMsg()
						+"-"+((AlgoObject) qChecker).keyMsg()
						+"-"+dataFormat.format(new Date())+".txt"));
				} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			f = new PrintStream(fs);
		}
		
		//report pqvth
		String report = new String("");
		String[] busCode = refInfo.getBusCode();
		for (int i=0; i<refInfo.getNoBus(); ++i) {
			if (busCode[i].equals("Swing")) {
				report += voltage[i].abs() +"\t"+ voltage[i].getArgument()+"\t";
			}else if (busCode[i].equals("GenPV")) {
				report += power[i].getReal() +"\t"+ voltage[i].abs()+"\t";
			}else {
				report += power[i].getReal() +"\t"+ power[i].getImaginary()+"\t";
			}
		}
		
		// S = V(YdV)*
		report += voltage[2].multiply((branchY[0].multiply(voltage[2].subtract(voltage[149]))).conjugate()).getReal()+"\t";
		report += voltage[6].multiply((branchY[1].multiply(voltage[6].subtract(voltage[130]))).conjugate()).getReal()+"\t";
		report += voltage[68].multiply((branchY[2].multiply(voltage[68].subtract(voltage[210]))).conjugate()).getReal()+"\t";
		report += voltage[78].multiply((branchY[3].multiply(voltage[78].subtract(voltage[210]))).conjugate()).getReal()+"\t";
		report += voltage[79].multiply((branchY[4].multiply(voltage[79].subtract(voltage[210]))).conjugate()).getReal()+"\n";
		
		f.print(report);
		
	}
	
	@Override
	public void report() {
		System.out.println("\n\t==============================================\n"
				+"\t[REPORT] nSuccessCase = "+"\t"+nCase+"\tokCase = "+okCase+"\n"
				+"\t==============================================");
		super.report();
	}
	
	@Override
	public String getReportTitle() {
		return super.getReportTitle()+"\tnSuccessCase\nOKCase";
	}
	
	@Override
	public String getReportStr() {
		return super.getReportStr()+"\t"+nCase+"\t"+okCase;
	}
	
	/**
	 * 
	 * @param args
	 * @throws InterpssException
	 */
	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee300.ieee";
		CaseBuilder300 g = new CaseBuilder300(baseCasePath);
		g.init();
		g.boom();
	}

}
