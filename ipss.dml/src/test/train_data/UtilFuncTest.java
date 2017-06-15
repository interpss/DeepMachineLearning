 /*
  * @(#)UtilFuncTest.java   
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

import org.interpss.service.UtilFunction;
import org.interpss.service.pattern.NetOptPattern;
import org.junit.Test;

public class UtilFuncTest {
  	@Test 
	public void getFilenameTest() {
  		String[] aryStr = UtilFunction.getFilenames("s1, s2, s3");
  		
  		assertTrue("", aryStr.length == 3);
  		assertTrue("", aryStr[1].equals("s2"));
  		
  		aryStr = UtilFunction.getFilenames("testdata/cases");
  		assertTrue("", aryStr.length == 2);
  		assertTrue("", aryStr[0].equals("testdata/cases/ieee14-1.ieee"));
  		assertTrue("", aryStr[1].equals("testdata/cases/ieee14.ieee"));
   	}
  	
  	@Test 
	public void netOptPatternTest() {
  		String line = "Pattern-1, missingBusIds [Bus15], missingBranchIds [Bus9->Bus15(1), Bus13->Bus15(1)]";
  		
		//System.out.println(line.split(",", 2)[0]);
  		
  		NetOptPattern p = UtilFunction.createNetOptPattern(line);
		System.out.println(p);
  		
  		assertTrue("", p.getName().equals("Pattern-1"));
  		assertTrue("", p.getMissingBusIds().size() == 1);
  		assertTrue("", p.getMissingBranchIds().size() == 2);
  	}  	
}

