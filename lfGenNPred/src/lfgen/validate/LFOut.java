package lfgen.validate;

import java.text.DecimalFormat;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

/**
* @author JeremyChenlk
* @version 2019年4月7日 下午9:46:46
*
* Class description:
*	
*/

public class LFOut {

	public LFOut() {
	}
	
	public static void showlf(AclfNetwork net) {
		DecimalFormat df = new DecimalFormat("0.0000000000");
		String s = new String();
		for (int i=0; i<net.getNoBus(); ++i) {
			AclfBus bus = net.getBusList().get(i);
			s += i;
			s += "\t" + df.format(bus.getGenP() - bus.getLoadP());
			s += "\t" + df.format(bus.getGenQ() - bus.getLoadQ());
			s += "\t" + df.format(bus.getVoltageMag());
			s += "\t" + df.format(bus.getVoltageAng());
			s += "\n";
		}
		System.out.println(s);
//		String s2 = new String();
//		for (int i=0; i<net.getNoBus(); ++i) {
//			AclfBus bus = net.getBusList().get(i);
//			s2 += i;
//			s2 += "\t" + df.format(bus.getGenP());
//			s2 += "\t" + df.format(bus.getLoadP());
//			s2 += "\t" + df.format(bus.getGenQ());
//			s2 += "\t" + df.format(bus.getLoadQ());
//			s2 += "\t" + df.format(bus.getVoltageMag());
//			s2 += "\t" + df.format(bus.getVoltageAng());
//			s2 += "\n";
//		}
//		System.out.println(s2);
	}
	
}
