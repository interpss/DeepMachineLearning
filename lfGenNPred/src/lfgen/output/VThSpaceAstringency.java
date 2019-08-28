package lfgen.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.interpss.common.exp.InterpssException;

import lfgen.algo.impl.LFComparator;
import lfgen.algo.impl.QChecker;
import lfgen.condition_gen.VThScanConditionBuilder;
import lfgen.datatype.AclfCase;
import lfgen.platform.LoadFlowCaseGenerator;

/**
* @author JeremyChenlk
* @version 2019年1月31日 下午6:30:51
*
* Class description:
*	此类提供的数据用来：比较电压空间的稀疏性，迭代方法对电压空间的改进
*/

public class VThSpaceAstringency extends LoadFlowCaseGenerator{

	public static final String NAME = "VThSpaceAstringency";
	public static final String NAME_IN_SHORT = "VTh";
	public static final String PARA_NEEDED = "NONE";
	
	/**
	 * validation
	 */
	private int nCase = 0;
	private int okCase = 0;
	
	public VThSpaceAstringency(String baseCasePath) throws InterpssException {
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
	 * 用于自定义生成器、校正器
	 * 输入生成器的参数
	 */
	@Override
	public void init() {
		long startTime = System.currentTimeMillis();
		//“其余照旧”
		super.init();
		//“除了这个”
		voltageGenConditionBuilder = new VThScanConditionBuilder(refInfo);
		
		addInitTime(startTime);
	}
	
	@Override
	public void boom() throws InterpssException {
		
		double qbf = 0.95;
		
		((VThScanConditionBuilder)voltageGenConditionBuilder).setVBoundaryFactor(1.0);
		((QChecker)qChecker).setQLimitSparseFactor(qbf);
		this.setMaxIter(10);
		
		double[] vfactor = new double[] {0.9, 1, 0.7, 0.5, 0.9, 0.8, 0.6, 0.95, 0.85, 0.75, 0.65, 0.55};//样例重要性序列
		for (double vf:vfactor) {
			
			this.resetStatistics();
			((VThScanConditionBuilder)voltageGenConditionBuilder).setVBoundaryFactor(vf);
			
			FileOutputStream fs = null;
			SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
			
			try {
				fs = new FileOutputStream(new File("doc/Data/"+NAME_IN_SHORT+"/VTh-vbf-"+vf+"-qbf-"+qbf+"-"+dataFormat.format(new Date())+".txt"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			PrintStream f = new PrintStream(fs);
			f.println(getReportTitle());
			
			int i;
			for (i=0; i<(1<<26); ++i) {
				go(voltageGenConditionBuilder.nextCondition());
				//每1m次输出一次，大约是20分钟输出一次
				if (i % 1000000 == 0) {
					System.out.println("=================================[REPORT] 第 "+i+"次=================================");
					report();
					System.out.println("===================================================================================");
					f.println(getReportStr());
				}
			}
			
			System.out.println("=================================[REPORT] 第 "+i+"次=================================");
			report();
			System.out.println("===================================================================================");
			f.println(getReportStr());
			
			f.close();
		}
	}
	
	@Override
	protected void successGenReaction(AclfCase aclfCase) {
		nCase += 1;
		if (Math.random() > 1-1e-7)
			System.out.println(aclfCase);
		if (LFComparator.checkCase(refInfo, aclfCase)) {
			okCase += 1;
		}
	}
	
	@Override
	public String getReportTitle() {
		return super.getReportTitle()+"\tnSuccessCase\nOKCase";
	}
	
	@Override
	public String getReportStr() {
		return super.getReportStr()+"\t"+nCase+"\t"+okCase;
	}
	
	@Override
	public void report() {
		System.out.println("\n\t==============================================\n"
				+"\t[REPORT] nSuccessCase = "+"\t"+nCase+"\tokCase = "+okCase+"\n"
				+"\t==============================================");
		super.report();
	}

	public static void main(String[] args) throws InterpssException {
		String baseCasePath = "testdata/cases/ieee14.ieee";
		VThSpaceAstringency g = new VThSpaceAstringency(baseCasePath);
		g.init();
		g.boom();
	}
}
