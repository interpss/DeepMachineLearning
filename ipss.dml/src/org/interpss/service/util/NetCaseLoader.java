 /*
  * @(#)NetCaseLoader.java   
  *
  * Copyright (C) 2005-17 www.interpss.org
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
  * @Date 04/7/2017
  * 
  *   Revision History
  *   ================
  *
  */

package org.interpss.service.util;

import static org.interpss.pssl.plugin.IpssAdapter.FileFormat.IEEECommonFormat;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.interpss.pssl.plugin.IpssAdapter;
import org.interpss.service.pattern.NetOptPattern;
import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.impl.TrainDataBuilderFactory;
import org.interpss.service.train_data.multiNet.IMultiNetTrainCaseBuilder;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfNetwork;

/**
 * Utility functions for the case loading
 * 
 * @author Mike
 *
 */
public class NetCaseLoader {
	/**
	 * load AclfNetwork file
	 * 
	 * @param filename
	 * @return
	 * @throws InterpssException
	 */
	public static AclfNetwork loadAclfNet(String filename) throws InterpssException {
		AclfNetwork aclfNet = loadAclfNetIEEECDF(filename);
		//System.out.println(filename + " loaded");
		aclfNet.setId(filename);
		return aclfNet;
	}
	
	/**
	 * load AclfNetwork file in IEEE Common Format
	 * 
	 * @param filename
	 * @return
	 * @throws InterpssException
	 */
	private static AclfNetwork loadAclfNetIEEECDF(String filename) throws InterpssException {
		return IpssAdapter.importAclfNet(filename)
				.setFormat(IEEECommonFormat)
				.load()
				.getImportedObj();
	}
}
