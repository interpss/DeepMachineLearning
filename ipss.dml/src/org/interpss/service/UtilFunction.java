 /*
  * @(#)UtilFunc.java   
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

package org.interpss.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.interpss.service.train_data.ITrainCaseBuilder;
import org.interpss.service.train_data.TrainDataBuilderFactory;

import com.interpss.common.exp.InterpssException;

/**
 * Utility functions for the PyGateway
 * 
 * @author Mike
 *
 */
public class UtilFunction {
	/**
	 * convert a double[] to a String "x[0],x[1],..."
	 * 
	 * @param array
	 * @return
	 */
	public static String array2String(double[] array){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(String.valueOf(array[i]) + " ");
		}
		return sb.toString();
		
	}
	
	/**
	 * get file names from the name/dir string
	 * 
	 * @param filenames format: "file1,file2,dir1,dir2,...". 
	 * @return filename array
	 */
	public static String[] getFilenames(String filenames) {
		String[] aryNames = filenames.split(",");
		List<String> list = new ArrayList<>();
		for (String s : aryNames) {
			File path = new File(s.trim());

			if (path.isDirectory()) {
				path.listFiles( new FilenameFilter() { 
					@Override public boolean accept(File dir, String name) { 
						list.add(s.trim() + "/" + name);
						return true; 
					} 
				}); 				
			}
			else
				list.add(s.trim());
		}
		return list.toArray(aryNames);
	}

	/**
	 * create a training case builder for the single network case
	 * 
	 * @param filename
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java)
	 * @return
	 * @throws InterpssException
	 */
	public static ITrainCaseBuilder createSingleNetBuilder(String filename, String buildername) throws InterpssException {
		return 	createSingleNetBuilder(filename, buildername, null, null);

	}
	
	/**
	 * create a training case builder for the single network case
	 * 
	 * @param filename
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java)
	 * @param busIdMappingFile
	 * @param branchIdMappingFile
	 * @return
	 * @throws InterpssException
	 */
	public static ITrainCaseBuilder createSingleNetBuilder(String filename, String buildername, 
	           		String busIdMappingFile, String branchIdMappingFile) throws InterpssException {
		ITrainCaseBuilder trainCaseBuilder = TrainDataBuilderFactory.createTrainCaseBuilder(buildername);

		// load busId/BranchId mapping files, if exist. trainCaseBuilder.noBus, trainCaseBuilder.noBranch
		// are calculated in the loading process
		if (busIdMappingFile != null)
			trainCaseBuilder.createBusId2NoMapping(busIdMappingFile);
		if (branchIdMappingFile != null)
			trainCaseBuilder.createBranchId2NoMapping(branchIdMappingFile);
			
		// set the AclfNetwork object. This step should be placed after the
		// mapping relationship loading steps.
		trainCaseBuilder.loadConfigureAclfNet(filename);
		//System.out.println(filename + " aclfNet case loaded, no buses/branches: " + trainCaseBuilder.getNoBus() +
		//			", " + trainCaseBuilder.getNoBranch());

		return trainCaseBuilder;	
	}
	
	/**
	 * create a training case builder for the multi-network case
	 * 
	 * @param filenames Loadflow case filesnames "file1,file2,...". It could be a dir path.
	 * @param buildername training set builder name (see details in TrainDataBuilderFactory.java)
	 * @param busIdMappingFile
	 * @param branchIdMappingFile
	 * @return
	 * @throws InterpssException
	 */
	public static ITrainCaseBuilder createMultiNetBuilder(String filenames, String buildername, 
	           String busIdMappingFile, String branchIdMappingFile) throws InterpssException {
		String[] aryNmes = UtilFunction.getFilenames(filenames);
		ITrainCaseBuilder trainCaseBuilder = TrainDataBuilderFactory.createMultiNetTrainCaseBuilder(aryNmes, buildername);

		trainCaseBuilder.createBusId2NoMapping(busIdMappingFile);
		trainCaseBuilder.createBranchId2NoMapping(branchIdMappingFile);

		return trainCaseBuilder;	
	}	
}
