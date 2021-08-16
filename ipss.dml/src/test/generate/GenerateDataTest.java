package test.generate;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
import com.interpss.core.CoreObjectFactory;
import com.interpss.SimuObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.msg.IPSSMsgHub;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.dstab.BaseDStabNetwork;
import com.interpss.dstab.DStabBus;
import com.interpss.simu.SimuContext;
import com.interpss.simu.SimuCtxType;

public class GenerateDataTest {
	double vm_max = 1.06;
	double vm_min = 0.94;
	double Pg;
	double Pd;
	int time = 5000;

	@Test
	public void test() throws InterpssException {
		// base network
		BaseDStabNetwork<?, ?> baseDsNet = this.getNetwork();
		// training network
		BaseDStabNetwork<?, ?> dsNet = this.getNetwork();

		Pd = dsNet.getBusList().stream().mapToDouble(bus -> bus.getLoadP()).sum();

		// load bus
		int[] loadBusIdArray = new int[] { 20, 21, 22, 24 };
		ArrayList<DStabBus> loadBusList = new ArrayList<DStabBus>();
		// gen bus
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
		ArrayList<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < time; i++) {
			HashMap<String, Object> output = new HashMap<String, Object>();
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
			double[][][] st = this.getMatrix(dsNet);
			// System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
			genBusList.forEach(bus -> {
				DStabBus baseBus = (DStabBus) baseDsNet.getBus(bus.getId());
				bus.setDesiredVoltMag(baseBus.getDesiredVoltMag() + Math.random() * 0.1 - 0.05);
			});
			double[] at = genBusList.stream().mapToDouble(bus -> {
				DStabBus baseBus = (DStabBus) baseDsNet.getBus(bus.getId());
				return bus.getDesiredVoltMag() - baseBus.getDesiredVoltMag();
			}).toArray();
			assertTrue(aclfAlgo.loadflow());
			Pg = dsNet.getBusList().stream()
					.filter(bus -> bus.getGenCode() == AclfGenCode.GEN_PV || bus.getGenCode() == AclfGenCode.SWING)
					.mapToDouble(bus -> bus.getGenP()).sum();
			// System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
			double[][][] st_new = this.getMatrix(dsNet);
			double rt = this.getRa(dsNet);
			output.put("st", st);
			output.put("at", at);
			output.put("rt", rt);
			output.put("st_new", st_new);
			outputList.add(output);
		}
		outputList.forEach(output -> {
			double[] at = (double[]) output.get("at");
			double rt = (double) output.get("rt");
			double[][][] st = (double[][][]) output.get("st");
			double[][][] st_new = (double[][][]) output.get("st_new");
			buffer.append(printMatrix(st)+"\n");
			buffer.append(Arrays.toString(at)+"\n");
			buffer.append(rt+"\n");
			buffer.append(printMatrix(st_new)+"\n");
		});
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File("testdata/data.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(buffer);
		pw.flush();
		pw.close();
		timer.logStd("Finish!");

	}

	public StringBuffer printMatrix(double[][][] st) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < st.length; i++) {
			for (int j = 0; j < st[i].length; j++) {
				for (int k = 0; k < st[i][j].length; k++) {
					buffer.append(st[i][j][k] + ", ");
				}
				buffer.append("\n");// 二维换行
			}
			buffer.append("\n");// 三维换行
		}
		return buffer;
	}

	public double getRa(BaseDStabNetwork<?, ?> dsNet) {
		double[] dVoltageArray = dsNet.getBusList().stream()
				.mapToDouble(bus -> (bus.getVoltageMag() - (vm_max + vm_min) / 2) / (vm_max - vm_min)).toArray();
		double dvm2 = 0;
		for (int i = 0; i < dVoltageArray.length; i++) {
			dvm2 += dVoltageArray[i] * dVoltageArray[i];
		}

		double ra = 1 - dvm2 / dsNet.getBusList().size() * 8 - (Pg - Pd);

		return ra;

	}

	public BaseDStabNetwork<?, ?> getNetwork() {
		IPSSMsgHub msg = CoreCommonFactory.getIpssMsgHub();
		IpssCorePlugin.init();
		PSSEAdapter adapter = new PSSEAdapter(PsseVersion.PSSE_30);
		assertTrue(adapter.parseInputFile(NetType.DStabNet,
				new String[] { "testData/psse/IEEE39Bus/IEEE39bus_v30.raw", "testData/psse/IEEE39Bus/IEEE39bus.dyr" }));
		DStabModelParser parser = (DStabModelParser) adapter.getModel();

		SimuContext simuCtx = SimuObjectFactory.createSimuNetwork(SimuCtxType.DSTABILITY_NET);
		if (!new ODMDStabParserMapper(msg).map2Model(parser, simuCtx)) {
			System.out
					.println("Error: ODM model to InterPSS SimuCtx mapping error, please contact support@interpss.com");
		}
		// LoadflowAlgorithm aclfAlgo =
		// CoreObjectFactory.createLoadflowAlgorithm(dsNet);
		//
		// assertTrue(aclfAlgo.loadflow());
		//
		// System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
		return simuCtx.getDStabilityNet();
	}

	public double[][][] getMatrix(BaseDStabNetwork<?, ?> dsNet) {
		double[] voltageArray = dsNet.getBusList().stream().mapToDouble(bus -> bus.getVoltageMag()).toArray();

		double[] pArray = dsNet.getBusList().stream().mapToDouble(bus -> bus.powerIntoNet().getReal()).toArray();
		double[] qArray = dsNet.getBusList().stream().mapToDouble(bus -> bus.powerIntoNet().getImaginary()).toArray();

		double[][] pBranchArray = new double[dsNet.getBusList().size()][dsNet.getBusList().size()];
		dsNet.getBranchList().forEach(branch -> {
			pBranchArray[(int) (branch.getFromAclfBus().getNumber() - 1)][(int) (branch.getToAclfBus().getNumber()
					- 1)] = branch.powerFrom2To().getReal();
			pBranchArray[(int) (branch.getToAclfBus().getNumber() - 1)][(int) (branch.getFromAclfBus().getNumber()
					- 1)] = branch.powerTo2From().getReal();
		});

		double[][] qBranchArray = new double[dsNet.getBusList().size()][dsNet.getBusList().size()];
		dsNet.getBranchList().forEach(branch -> {
			qBranchArray[(int) (branch.getFromAclfBus().getNumber() - 1)][(int) (branch.getToAclfBus().getNumber()
					- 1)] = branch.powerFrom2To().getImaginary();
			qBranchArray[(int) (branch.getToAclfBus().getNumber() - 1)][(int) (branch.getFromAclfBus().getNumber()
					- 1)] = branch.powerTo2From().getImaginary();
		});

		return new double[][][] { getMatrix(voltageArray), getMatrix(pArray), getMatrix(qArray), pBranchArray,
				qBranchArray };
	}

	public double[][] getMatrix(double[] vector) {
		double[][] matrix = new double[vector.length][vector.length];
		for (int i = 0; i < vector.length; i++) {
			matrix[i][i] = vector[i];
		}
		return matrix;
	}

}
