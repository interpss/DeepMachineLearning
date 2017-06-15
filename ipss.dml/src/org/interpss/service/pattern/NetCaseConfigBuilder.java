 /*
  * @(#)NetCaseConfigBuilder.java   
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
  * @Date 06/15/2017
  * 
  *   Revision History
  *   ================
  *
  */
package org.interpss.service.pattern;

import java.util.List;

import org.interpss.service.util.NetCaseLoader;
import org.interpss.service.util.UtilFunction;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;

/**
 * Net case configuration info builder
 * 
 * @author Mike
 *
 */
public class NetCaseConfigBuilder {
	/** network case file names for building the configuration*/
	private String[] filenames;
	
	//private NetCaseConfiguration netCaseConfig;
	
	public NetCaseConfigBuilder(String dir) {
		this.filenames = UtilFunction.getFilenames(dir);
	}
	
	public NetCaseConfiguration build() {
		NetCaseConfiguration netCaseConfig = new NetCaseConfiguration();
		
		if (this.filenames.length == 0 ) {
			System.out.println("There is no file in the directory!");
			return netCaseConfig;
		}
		
		buildBaseCase(this.filenames[0], netCaseConfig);
	
		for (int i = 1; i < this.filenames.length; i++) {
			buildAdditionalCase(this.filenames[i], netCaseConfig);
		}
		
		return netCaseConfig;
	}
	
	private void buildBaseCase(String filename, NetCaseConfiguration netCaseConfig) {
		netCaseConfig.createOptPattern(filename);
		
		try {
			AclfNetwork aclfNet = NetCaseLoader.loadAclfNet(filename);
			
			int cnt = 0;
			for (AclfBus bus : aclfNet.getBusList()) {
				if (bus.isActive())
					netCaseConfig.busId2NoMapping.put(bus.getId(), cnt++);
			}
			
			cnt = 0;
			for (AclfBranch branch : aclfNet.getBranchList()) {
				if (branch.isActive())
					netCaseConfig.branchId2NoMapping.put(branch.getId(), cnt++);
			}
		} catch (InterpssException e) {
			e.printStackTrace();
		}
	}

	private void buildAdditionalCase(String filename, NetCaseConfiguration netCaseConfig) {
		try {
			// load the net case file
			AclfNetwork aclfNet = NetCaseLoader.loadAclfNet(filename);
			
			/*
			 * find bus/branch in the aclfNet, which are missing in the current mapping relationship
			 */
			List<String> busListMissingInMapping = netCaseConfig.findBusIdsMissingInMapping(aclfNet);
			List<String> branchListMissingInMapping = netCaseConfig.findBranchIdsMissingInMapping(aclfNet);
			/*
			System.out.println("Filename: " + filename +
			           ", missing Bus in Mapping: " + busListMissingInMapping.size() + 
			           ", missing Branch in Mapping: " + branchListMissingInMapping.size());
	        */
			// update the mapping relationship
			busListMissingInMapping.forEach(busId -> {
				netCaseConfig.addBus2Mapping(busId);
			});
			branchListMissingInMapping.forEach(branchId -> {
				netCaseConfig.addBranch2Mapping(branchId);
			});

			/*
			 * if the aclfNet is a new network operation pattern, add the new pattern to
			 * the NetCaseConfiguration object
			 */
			if (!netCaseConfig.hasNetOptPattern(aclfNet)) {
				// find bus/branch in the mapping, which are missing in the aclfNet object
				List<String> busListMissingInAclfNet = netCaseConfig.findBusIdsMissingInNetwork(aclfNet);
				List<String> branchListMissingInAclfNet = netCaseConfig.findBranchIdsMissingInNetwork(aclfNet);
				/*
				System.out.println("Filename: " + filename +
				           ", missing Bus in AclfNet: " + busListMissingInAclfNet.size() + 
				           ", missing Branch in AclfNet: " + branchListMissingInAclfNet.size());
				*/
				// if there are missing bus/branch, create a new NetOptPattern and add them to the pattern 
				if (busListMissingInAclfNet.size() > 0 || branchListMissingInAclfNet.size() > 0) {
					NetOptPattern pattern = netCaseConfig.createOptPattern(filename);
			
					busListMissingInAclfNet.forEach(id -> {
						pattern.getMissingBusIds().add(id);
					});
					branchListMissingInAclfNet.forEach(id -> {
						pattern.getMissingBranchIds().add(id);
					});
				}
			}
		} catch (InterpssException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		String str = "";
		for(String s : this.filenames ) { str += s + ", ";}
		return "filenames: " + str;
	}
}
