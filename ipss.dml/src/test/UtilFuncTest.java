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
import org.interpss.service.train_data.TrainDataBuilderFactory;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.datatype.Mismatch;

public class UtilFuncTest {
  	@Test 
	public void test() {
  		String[] aryStr = UtilFunction.getFilenames("s1, s2, s3");
  		
  		assertTrue("", aryStr.length == 3);
  		assertTrue("", aryStr[1].equals("s2"));
  		
  		aryStr = UtilFunction.getFilenames("c:/temp/temp/cases");
  		assertTrue("", aryStr.length == 2);
  		assertTrue("", aryStr[0].equals("c:/temp/temp/cases/ieee14-1.ieee"));
  		assertTrue("", aryStr[1].equals("c:/temp/temp/cases/ieee14.ieee"));
   	}
}

