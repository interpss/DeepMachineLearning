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
package org.interpss.service.train_data.multiNet.aclf.load_change;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent Network operation concept
 * 
 * @author Mike
 *
 */
public class NetOptPattern {
	private String name;
	private List<String> missingBusIds;
	private List<String> missingBranchIds;
	
	public NetOptPattern(String name) {
		this.name = name;
		this.missingBusIds = new ArrayList<>();
		this.missingBranchIds = new ArrayList<>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the missingBusIds
	 */
	public List<String> getMissingBusIds() {
		return missingBusIds;
	}

	/**
	 * @return the missingBranchIds
	 */
	public List<String> getMissingBranchIds() {
		return missingBranchIds;
	}
	
	public String toString() {
		return "NetOptPattern: name " + this.name +
				", missingBusIds " + this.missingBusIds.toString() + 
				", missingBranchIds " + this.missingBranchIds.toString();
	}
}
