 /*
  * @(#)NetCaseConfigTest.java   
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

import org.interpss.IpssCorePlugin;
import org.interpss.service.pattern.NetCaseConfigBuilder;
import org.interpss.service.pattern.NetCaseConfiguration;
import org.junit.Test;

public class NetCaseConfigTest {
  	@Test 
	public void loadFileTest() {
  		NetCaseConfiguration config = new NetCaseConfiguration();
  		
  		config.createNetOptPatternSet("testdata/ieee14_netOpt.pattern");
  		config.createBusId2NoMapping("testdata/ieee14_busid2no.mapping");
  		config.createBranchId2NoMapping("testdata/ieee14_branchid2no.mapping");
  		
  		assertTrue("", config.getNoBuses() == 15);
  		assertTrue("", config.getBusIndex("Bus15") == 14);

  		assertTrue("", config.getNoBranches() == 22);
  		assertTrue("", config.getBranchIndex("Bus13->Bus15(1)") == 21);
  		
  		assertTrue("", config.getNoOptPatterns() == 2);
  		assertTrue("", config.getOptPattern("Pattern-1") != null);
  		assertTrue("", config.getOptPattern("Pattern-1").getMissingBusIds().size() == 1);
  		assertTrue("", config.getOptPattern("Pattern-1").getMissingBranchIds().size() == 2);
  		
  		config.saveNetOptPatternSet("temp/netOpt.pattern");
  		config.saveBusId2NoMapping("temp/busid2no.mapping");
  		config.saveBranchId2NoMapping("temp/branchid2no.mapping");
   	}
  	
  	@Test 
	public void buidNetConfigTest() {
		IpssCorePlugin.init();
		
  		NetCaseConfigBuilder builder = new NetCaseConfigBuilder("testdata/netConfigCases");
  		System.out.println(builder);
  		
  		NetCaseConfiguration config = builder.build();
  		
 		assertTrue("", config.getNoOptPatterns() == 2);
  		assertTrue("", config.getNoBuses() == 15);
  		assertTrue("", config.getNoBranches() == 22);

  		config.saveNetOptPatternSet("temp/netOpt.pattern");
  		config.saveBusId2NoMapping("temp/busid2no.mapping");
  		config.saveBranchId2NoMapping("temp/branchid2no.mapping");
  	}
}

