 /*
  * @(#)NetCaseConfiguration.java   
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.interpss.service.UtilFunction;

/**
 * 
 * @author Mike
 *
 */
public class NetCaseConfiguration {
	/** Network operation pattern list*/
	protected List<NetOptPattern> netOptPatterns;
	/** Bus id to NN model bus array index mapping */
	protected HashMap<String,Integer> busId2NoMapping;
	/** Branch id to NN model branch array index mapping */
	protected HashMap<String,Integer> branchId2NoMapping;	
	
	public void createBusId2NoMapping(String filename) {
		this.busId2NoMapping = new HashMap<>();
		loadTextFile(filename, line -> {
			// format: Bus1 0
			String[] strAry = line.split(" ");
			this.busId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
	}

	public void createBranchId2NoMapping(String filename) {
		this.branchId2NoMapping = new HashMap<>();
		loadTextFile(filename, line -> {
			// format: Bus1->Bus2(1) 0
			String[] strAry = line.split(" ");
			this.branchId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
	}
	
	public void createNetOptPatternList(String filename) {
		this.netOptPatterns = new ArrayList<>();
		loadTextFile(filename, line -> {
			// Pattern-1, missingBus [ Bus15 ], missingBranch [ Bus9->Bus15(1) Bus13->Bus15(1) ]
			NetOptPattern p = UtilFunction.createNetOptPattern(line);
			this.netOptPatterns.add(p);
		});
	}	
	
	protected void loadTextFile(String filename, Consumer<String> processor) {
		try (Stream<String> stream = Files.lines(Paths.get(filename))) {
			stream.filter(line -> {return !line.startsWith("#") && 
					                      !line.trim().equals("");})
				  .forEach(processor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
