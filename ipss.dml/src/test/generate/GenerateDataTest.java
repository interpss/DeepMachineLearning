package test.generate;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.ieee.odm.adapter.IODMAdapter.NetType;
import org.ieee.odm.adapter.psse.PSSEAdapter;
import org.ieee.odm.adapter.psse.PSSEAdapter.PsseVersion;
import org.ieee.odm.model.dstab.DStabModelParser;
import org.interpss.IpssCorePlugin;
import org.interpss.mapper.odm.ODMDStabParserMapper;
import org.interpss.numeric.util.PerformanceTimer;
import org.junit.Test;

import com.interpss.CoreCommonFactory;
import com.interpss.CoreObjectFactory;
import com.interpss.SimuObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.msg.IPSSMsgHub;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.dstab.BaseDStabNetwork;
import com.interpss.dstab.DStabBus;
import com.interpss.simu.SimuContext;
import com.interpss.simu.SimuCtxType;

public class GenerateDataTest {
	
	@Test 
	public void test() throws InterpssException {
		//base network
		BaseDStabNetwork<?, ?> baseDsNet = this.getNetwork();
		//training network
		BaseDStabNetwork<?, ?> dsNet = this.getNetwork();
		//load bus
		int[] loadBusIdArray = new int[] { 20, 21, 22, 24 };
		ArrayList<DStabBus> loadBusList = new ArrayList<DStabBus>();
		//gen bus
		int[] genBusIdArray = new int[] { 30, 31, 32, 33, 34, 35, 36, 37, 38, 39 };
		ArrayList<DStabBus> genBusList = new ArrayList<DStabBus>();
		for (int id : loadBusIdArray) {
			loadBusList.add((DStabBus) dsNet.getBus("Bus" + id));
		}
		for (int id : genBusIdArray) {
			genBusList.add((DStabBus) dsNet.getBus("Bus" + id));
		}
		PerformanceTimer timer = new PerformanceTimer(IpssLogger.getLogger());
		timer.start();
		for (int i = 0; i < 5000; i++) {
			double factor = Math.random() + 0.5;
			loadBusList.forEach(bus -> {
				DStabBus baseBus = (DStabBus) baseDsNet.getBus(bus.getId());
				bus.getContributeLoadList().forEach(load -> {
					Complex baseLoadCP = baseBus.getContributeLoad(load.getId()).getLoadCP();
					load.setLoadCP(new Complex(baseLoadCP.getReal(), baseLoadCP.getImaginary() * factor));
				});
			});
			LoadflowAlgorithm aclfAlgo = CoreObjectFactory.createLoadflowAlgorithm(dsNet);

			assertTrue(aclfAlgo.loadflow());
//			System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
			genBusList.forEach(bus -> {
				DStabBus baseBus = (DStabBus) baseDsNet.getBus(bus.getId());
				bus.setDesiredVoltMag(baseBus.getDesiredVoltMag() + Math.random() * 0.1 - 0.05);
			});
			assertTrue(aclfAlgo.loadflow());
//			System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
		}
		timer.logStd("Finish!");

	}

	public BaseDStabNetwork<?,?> getNetwork() {
		IPSSMsgHub msg = CoreCommonFactory.getIpssMsgHub();
		IpssCorePlugin.init();
		PSSEAdapter adapter = new PSSEAdapter(PsseVersion.PSSE_30);
		assertTrue(adapter.parseInputFile(NetType.DStabNet, new String[]{
				"testData/psse/IEEE39Bus/IEEE39bus_v30.raw",
				"testData/psse/IEEE39Bus/IEEE39bus.dyr"
		}));
		DStabModelParser parser =(DStabModelParser) adapter.getModel();

		SimuContext simuCtx = SimuObjectFactory.createSimuNetwork(SimuCtxType.DSTABILITY_NET);
		if (!new ODMDStabParserMapper(msg)
					.map2Model(parser, simuCtx)) {
			System.out.println("Error: ODM model to InterPSS SimuCtx mapping error, please contact support@interpss.com");	
		}
//	    LoadflowAlgorithm aclfAlgo = CoreObjectFactory.createLoadflowAlgorithm(dsNet);
//
//		assertTrue(aclfAlgo.loadflow());
//		
//		System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
	    return simuCtx.getDStabilityNet();
	}
}
