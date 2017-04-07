package org.interpss.service;

import static org.interpss.pssl.plugin.IpssAdapter.FileFormat.IEEECommonFormat;

import org.interpss.IpssCorePlugin;
import org.interpss.pssl.plugin.IpssAdapter;
import org.interpss.service.train.ITrainCaseBuilder;
import org.interpss.service.train.TrainDataBuilderFactory;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;

import py4j.GatewayServer;

/**
 * InterPSS AC Loadflow training data service gateway
 * 
 * @author Mike
 *
 */ 
public class AclfPyGateway {
	private ITrainCaseBuilder trainCaseBuilder;
	
	/**
	 * Load a loadflow case and crete the TrainCaseBuilder object
	 * 
	 * @param filename
	 * @return no of active buses in the network
	 */
	public int loadCase(String filename) {
		IpssCorePlugin.init();
		
		try {
			AclfNetwork aclfNet = IpssAdapter.importAclfNet(filename)
					.setFormat(IEEECommonFormat)
					.load()
					.getImportedObj();
			
			this.trainCaseBuilder = TrainDataBuilderFactory.createITrainCaseBuilder(aclfNet);
			System.out.println(filename + " aclfNet case loaded");
		} catch ( InterpssException e) {
			e.printStackTrace();
			return 0;
		}

		return this.trainCaseBuilder.getNoBus();
	}

	/**
	 * create and return a set of training cases, 
	 *   Data format: [2][points][2*NoBus]
	 *              [
	 *     (input)    [[NetPQ]   ... [NetPQ]   ],
	 *     (output)   [[NetVolt] ... [NetVolt] ]
	 *              ]         
	 * @param points number of training cases
	 * @return the training set
	 */
	public double[][][] getTrainSet(int points) {
		double[][][] set = new double[2][points][];
		
		for ( int i = 0; i < points; i++) {
			this.trainCaseBuilder.createTrainCase();
			
			set[0][i] = this.trainCaseBuilder.getNetInputPQ();
			set[1][i] = this.trainCaseBuilder.getNetOutputVolt();
		}
		
		return set;
	}
	
	public static void main(String[] args) {
		AclfPyGateway app = new AclfPyGateway();
		// app is now the gateway.entry_point
		GatewayServer server = new GatewayServer(app);
		System.out.println("Starting Py4J " + app.getClass().getTypeName() + " ...");
		server.start();
	}	
}
