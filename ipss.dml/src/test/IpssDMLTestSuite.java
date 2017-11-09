 /*
  * @(#)IpssDMLTestSuite.java   
  *
  * Copyright (C) 2006-2017 www.interpss.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * @Author Mike Zhou
  * @Version 1.0
  * @Date 07/01/2017
  * 
  *   Revision History
  *   ================
  *
  */

package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.train_data.AclfFuncMultiNetTest;
import test.train_data.AclfFuncSingleNetTest;
import test.train_data.NetCaseConfigTest;
import test.train_data.UtilFuncTest;

@RunWith(Suite.class)
@SuiteClasses({
	UtilFuncTest.class,
	
	AclfFuncMultiNetTest.class,
	AclfFuncSingleNetTest.class,
	
	NetCaseConfigTest.class,
})
public class IpssDMLTestSuite {
}
