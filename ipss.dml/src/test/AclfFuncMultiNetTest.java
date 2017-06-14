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

package test;

import static org.junit.Assert.assertTrue;

import org.interpss.IpssCorePlugin;
import org.interpss.service.UtilFunction;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.multiNet.IMultiNetTrainCaseBuilder;
import org.interpss.service.train_data.multiNet.NetOptPattern;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.datatype.Mismatch;

public class AclfFuncMultiNetTest {
  	@Test 
	public void testNetOptPattern() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createMultiNetBuilder("testdata/ieee14.ieee, testdata/ieee14-1.ieee", 
  				"BusVoltageTrainCaseBuilder",
  				"c:/temp/temp/ieee14_busid2no.mapping", 
  				"c:/temp/temp/ieee14_branchid2no.mapping",
  				"c:/temp/temp/ieee14_netOpt.pattern");
  		
  		IMultiNetTrainCaseBuilder multiNetCaseBuilder = (IMultiNetTrainCaseBuilder)caseBuilder;
  		assertTrue("", multiNetCaseBuilder.getNoNetOptPatterns() == 2);
  		
  		NetOptPattern p = multiNetCaseBuilder.getNetOptPattern(0);
  		assertTrue("", p.getName().equals("Pattern-1"));

  		caseBuilder.loadConfigureAclfNet("testdata/ieee14.ieee");
  		
  		assertTrue("", p.isPattern(caseBuilder.getAclfNet()));
   	}	
  	@Test 
	public void testMultiNet() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createMultiNetBuilder("testdata/ieee14.ieee, testdata/ieee14-1.ieee", 
  				"BusVoltageTrainCaseBuilder",
  				"c:/temp/temp/ieee14_busid2no.mapping", 
  				"c:/temp/temp/ieee14_branchid2no.mapping",
  				"c:/temp/temp/ieee14_netOpt.pattern");
  		
  		IMultiNetTrainCaseBuilder multiNetCaseBuilder = (IMultiNetTrainCaseBuilder)caseBuilder;
  		
  		assertTrue("", multiNetCaseBuilder.getNoNetOptPatterns() == 2);
  		
  		NetOptPattern p = multiNetCaseBuilder.getNetOptPattern(0);
  		assertTrue("", p.getName().equals("Pattern-1"));
  		assertTrue("", p.getMissingBusIds().size() == 1);
  		assertTrue("", p.getMissingBranchIds().size() == 2);
  		
  		for (int i = 0; i < 5; i++) {
  	 		caseBuilder.createTestCase();
  	 		
  	 		IMultiNetTrainCaseBuilder.NetworkCase netCase = multiNetCaseBuilder.getCurNetCase();
  	 		// after a training case is created, its associated Net Operation Pattern number could be
  	 		// get in the following way.
  	 		int noNetOptPattern = netCase.noNetOptPattern;
  	  		assertTrue("", netCase.filename.equals("testdata/ieee14.ieee") && noNetOptPattern == 0 || 
  	  		               netCase.filename.equals("testdata/ieee14-1.ieee") && noNetOptPattern == 1 ); 
  	  		
  	  		double[] netVolt = caseBuilder.getNetOutput();
  	  		assertTrue("The length is decided by the info in the mapping file", netVolt.length == 15*2); 
  	  		
  	  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  	  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  	  				   mis.maxMis.abs() < 0.0001);
  	  		//System.out.println(caseBuilder.calMismatch(netVolt));
  		}
   	}
  	
 	@Test 
	public void testMultiNetDir() throws InterpssException {
		IpssCorePlugin.init();
		
  		ITrainCaseBuilder caseBuilder = UtilFunction.createMultiNetBuilder("testdata/cases", 
  				"BusVoltageTrainCaseBuilder",
  				"c:/temp/temp/ieee14_busid2no.mapping", 
  				"c:/temp/temp/ieee14_branchid2no.mapping",
  				"c:/temp/temp/ieee14_netOpt.pattern");
  		
  		for (int i = 0; i < 5; i++) {
  	 		caseBuilder.createTestCase();
  	  		
  	  		double[] netVolt = caseBuilder.getNetOutput();
  	  		assertTrue("The length is decided by the info in the mapping file", netVolt.length == 15*2); 
  	  		
  	  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  	  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  	  				   mis.maxMis.abs() < 0.0001);
  	  		//System.out.println(caseBuilder.calMismatch(netVolt));
  		}
   	}
}

