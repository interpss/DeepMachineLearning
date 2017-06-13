 /*
  * @(#)NetOptPattern.java   
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
package org.interpss.service.train_data.multiNet;

import java.util.ArrayList;
import java.util.List;

import com.interpss.core.aclf.AclfNetwork;

/**
 * Class to represent Network operation pattern concept. With the
 * Bus/Branch id list stored in the mapping file as the reference, a 
 * network operation pattern is defined using a set of missing bus/branch ids.
 * 
 * @author Mike
 *
 */
public class NetOptPattern {
	/** pattern name */
	private String name;
	
	/** missing bus id list*/
	private List<String> missingBusIds;
	
	/** missing branch id list*/
	private List<String> missingBranchIds;
	
	/**
	 * constructor
	 * 
	 * @param name pattern name
	 */
	public NetOptPattern(String name) {
		this.name = name;
		this.missingBusIds = new ArrayList<>();
		this.missingBranchIds = new ArrayList<>();
	}

	/**
	 * get the pattern name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the missing bus id list
	 * 
	 * @return the missingBusIds
	 */
	public List<String> getMissingBusIds() {
		return missingBusIds;
	}

	/**
	 * get the missing branch id list
	 * 
	 * @return the missingBranchIds
	 */
	public List<String> getMissingBranchIds() {
		return missingBranchIds;
	}
	
	/**
	 * check if the AclfNetwork object is with this network operation pattern
	 * 
	 * @param net
	 * @return
	 */
	public boolean isPattern(AclfNetwork net) {
		boolean t = true;
		for (String busid : this.getMissingBusIds()) {
			if (net.getBus(busid) != null)
				t = false;
		}
		for (String braid : this.getMissingBranchIds()) {
			if (net.getBranch(braid) != null)
				t = false;			
		}
		return t;
	}
	
	public String toString() {
		return "NetOptPattern: name " + this.name +
				", missingBusIds " + this.missingBusIds.toString() + 
				", missingBranchIds " + this.missingBranchIds.toString();
	}
}
