 /*
  * @(#)NetContainerTest.java   
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

import static org.interpss.pssl.plugin.IpssAdapter.FileFormat.IEEECommonFormat;
import static org.junit.Assert.assertTrue;

import org.interpss.IpssCorePlugin;
import org.interpss.pssl.plugin.IpssAdapter;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.TrainDataBuilderFactory;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.datatype.Mismatch;

public class AclfFuncTest {
  	@Test 
	public void test() {
  		ITrainCaseBuilder caseBuilder = createCaseBuilder("testdata/ieee14.ieee");
  		 
  		caseBuilder.createTestCase();
  		
  		double[] netVolt = caseBuilder.getNetOutput();
  		
  		Mismatch mis = caseBuilder.calMismatch(netVolt);
  		assertTrue("netVolt is a Loadflow solution, therefore the mismatch should be very small! ", 
  				   mis.maxMis.abs() < 0.0001);
  		//System.out.println(caseBuilder.calMismatch(netVolt));
   	}
  	
  	private ITrainCaseBuilder createCaseBuilder(String filename) {
		IpssCorePlugin.init();
		
		try {
			AclfNetwork aclfNet = IpssAdapter.importAclfNet(filename)
					.setFormat(IEEECommonFormat)
					.load()
					.getImportedObj();
			
			System.out.println(filename + " aclfNet case loaded");
			ITrainCaseBuilder builder = TrainDataBuilderFactory.createITrainCaseBuilder("BusVoltageTrainCaseBuilder", 1);
			builder.setAclfNetConfig(aclfNet);
			
			return builder;
		} catch ( InterpssException e) {
			e.printStackTrace();
		}

		return null;
	}
}

