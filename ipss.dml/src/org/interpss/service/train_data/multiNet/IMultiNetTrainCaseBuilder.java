 /*
  * @(#)IMultiNetTrainCaseBuilder.java   
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

import java.util.List;

/**
 * A training case builder interface for the multi-network cases
 * 
 * @author Mike
 *
 */ 
public interface IMultiNetTrainCaseBuilder {
	/**
	 * Record to hold network case relavent info
	 * 
	 */
	public static class AclfNetCase {
		/** netCase filename */
		public String filename;
		/** netCase Network Option pattern number, it is the sequence number in the netOptPatterns list */
		public int noNetOptPattern = -1;
		
		public AclfNetCase(String name) { this.filename = name; }
	}
	
	/**
	 * get the current NetCase object
	 * 
	 * @return the curNetCase
	 */
	AclfNetCase getCurNetCase(); 	
	
	/**
	 * create network operation pattern list 
	 * by loading the pattern info stored in the file
	 * 
	 * @param filename network operation pattern info filename
	 */
	void createNetOptPatternList(String filename);
	
	/**
	 * get the network operation patterns
	 * 
	 * @return
	 */
	List<NetOptPattern> getNetOptPatterns();	
}