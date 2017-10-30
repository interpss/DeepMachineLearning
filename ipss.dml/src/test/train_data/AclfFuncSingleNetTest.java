 /*
  * @(#)AclfFuncTest.java   
  *
  * Copyright (C) 2006-2014 www.interpss.org
  *
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0
    
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
  *
  * @Author Mike Zhou
  * @Version 1.0
  * @Date 04/05/2017
  * 
  *   Revision History
  *   ================
  *
  */

package test.train_data;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.interpss.IpssCorePlugin;
import org.interpss.service.UtilFunction;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.datatype.Mismatch;

public class AclfFuncSingleNetTest {
  	@Test 
	public void testSingleNet() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createSingleNetBuilder(
  				       "testdata/ieee14.ieee", "BusVoltageTrainCaseBuilder");
  		
  		// at this point, the caseBuilder.loadConfigureAclfNet() has been called and the 
  		// base case bus data has been cached
  		/*
  		System.out.println(caseBuilder.getBaseCaseData()[0]);
  		System.out.println(caseBuilder.getBaseCaseData()[2]);
  		System.out.println(caseBuilder.getBaseCaseData()[10]);
  		BusData: Bus1, 0, 0.0, 0.0
		BusData: Bus3, 1, 0.0, 0.0
		BusData: Bus11, 2, 0.035, 0.018
  		*/ 
  		ITrainCaseBuilder.BusData busdata = caseBuilder.getBaseCaseData()[0];
  		assertTrue("", busdata.id.equals("Bus1"));
  		assertTrue("", busdata.type == ITrainCaseBuilder.BusData.Swing);

  		busdata = caseBuilder.getBaseCaseData()[2];
  		assertTrue("", busdata.id.equals("Bus3"));
  		assertTrue("", busdata.type == ITrainCaseBuilder.BusData.PV);

  		busdata = caseBuilder.getBaseCaseData()[10];
  		assertTrue("", busdata.id.equals("Bus11"));
  		assertTrue("", busdata.type == ITrainCaseBuilder.BusData.PQ);
  		assertTrue("", busdata.loadP == 0.035);
  		assertTrue("", busdata.loadQ == 0.018);
  		
  		caseBuilder.createTestCase();
  		
  		double[] netVolt = caseBuilder.getNetOutput();
  		
  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  				   mis.maxMis.abs() < 0.0001);
  		//System.out.println(caseBuilder.calMismatch(netVolt));
   	}
  	
  	@Test 
	public void testSingleNet1() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createSingleNetBuilder("testdata/ieee14.ieee", "BusVoltageTrainCaseBuilder",
  				"c:/temp/temp/ieee14_busid2no.mapping", "c:/temp/temp/ieee14_branchid2no.mapping");
  		 
  		caseBuilder.createTestCase();
  		
  		double[] netVolt = caseBuilder.getNetOutput();
  		assertTrue("The length is decided by the info in the mapping file", netVolt.length == 15*2); 
  		
  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  				   mis.maxMis.abs() < 0.0001);
  		//System.out.println(caseBuilder.calMismatch(netVolt));
   	}
  	
  	@Test 
	public void testSingleNet_NNLF() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createSingleNetBuilder(
  				       "testdata/ieee14.ieee", "NNLFLoadChangeTrainCaseBuilder");
  		
  		caseBuilder.createTestCase();
  		
  		double[] netVolt = caseBuilder.getNetOutput();
  		
  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  		System.out.println("--->" + caseBuilder.calMismatch(netVolt));
  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  				   mis.maxMis.abs() < 0.0001);
   	}
  	
  	@Test 
	public void testSingleRandomNet() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createSingleNetBuilder("testdata/ieee14.ieee", "BusVoltageTrainCaseBuilder",
  				"c:/temp/temp/ieee14_busid2no.mapping", "c:/temp/temp/ieee14_branchid2no.mapping");
  		 
  		caseBuilder.createRandomCase();
  		double[] netPQ = caseBuilder.getNetInput();
  		System.out.println(Arrays.toString(netPQ));
  		double[] netVolt = caseBuilder.getNetOutput();
  		assertTrue("The length is decided by the info in the mapping file", netVolt.length == 15*2); 
  		
  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  				   mis.maxMis.abs() < 0.0001);
  		//System.out.println(caseBuilder.calMismatch(netVolt));
   	}
}

