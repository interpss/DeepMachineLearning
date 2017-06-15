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

import org.interpss.service.UtilFunction;

/**
 * 
 * @author Mike
 *
 */
public class NetCaseConfigBuilder {
	private String[] filenames;
	
	public NetCaseConfigBuilder(String dir) {
		this.filenames = UtilFunction.getFilenames(dir);
	}
	
	public NetCaseConfiguration build() {
		NetCaseConfiguration config = new NetCaseConfiguration();
		
		if (this.filenames.length == 0 ) {
			System.out.println("There is no file in the directory!");
			return config;
		}
		
		buildBaseCase(this.filenames[0]);
	
		for (int i = 1; i < this.filenames.length; i++) {
			buildAdditionalCase(this.filenames[i]);
		}
		
		return config;
	}
	
	private void buildBaseCase(String filename) {
		
	}

	private void buildAdditionalCase(String filename) {
		
	}
	
	public String toString() {
		String str = "";
		for(String s : this.filenames ) { str += s + ", ";}
		return "filenames: " + str;
	}
}
