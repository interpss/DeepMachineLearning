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
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.interpss.service.UtilFunction;

/**
 * Class for storing network case configuration info, including:
 * 
 *  1) Network operation pattern list
 *  2) Bus id to NN model bus array index mapping
 *  3) Branch id to NN model bus array index mapping
 * 
 * @author Mike
 *
 */
public class NetCaseConfiguration {
	/** Network operation pattern list*/
	protected HashMap<String,NetOptPattern> netOptPatterns;
	/** Bus id to NN model bus array index mapping */
	protected HashMap<String,Integer> busId2NoMapping;
	/** Branch id to NN model branch array index mapping */
	protected HashMap<String,Integer> branchId2NoMapping;	
	
	/**
	 * default constructor
	 */
	public NetCaseConfiguration() {
		this.netOptPatterns = new HashMap<>();
		this.busId2NoMapping = new HashMap<>();
		this.branchId2NoMapping = new HashMap<>();
	}
	
	/**
	 * get number of buses in the NN-model
	 * 
	 * @return
	 */
	public int getNoBuses() {
		return this.busId2NoMapping.size();
	}

	/**
	 * get number of branches in the NN-model
	 * 
	 * @return
	 */
	public int getNoBranches() {
		return this.branchId2NoMapping.size();
	}
	
	/**
	 * get number of network operation patterns in the NN-model
	 * 
	 * @return
	 */
	public int getNoOptPatterns() {
		return this.netOptPatterns.size();
	}
	
	/**
	 * get network operation pattern by name
	 * 
	 * @param name
	 * @return
	 */
	public NetOptPattern getOptPattern(String name) {
		return this.netOptPatterns.get(name);
	}

	/**
	 * get bus NN-model index by busId
	 * 
	 * @param busId
	 * @return
	 */
	public int getBusIndex(String busId) {
		return this.busId2NoMapping.get(busId);
	}

	/**
	 * get branch NN-model index by branchId
	 * 
	 * @param branchId
	 * @return
	 */
	public int getBranchIndex(String branchId) {
		return this.branchId2NoMapping.get(branchId);
	}
	
	/**
	 * create BusId to model array number mapping relationship 
	 * by loading the mapping info stored in the mapping file
	 * 
	 * @param filename BusId to model array number mapping filename
	 */
	public void createBusId2NoMapping(String filename) {
		this.busId2NoMapping = new HashMap<>();
		loadTextFile(filename, line -> {
			// format: Bus1 0
			String[] strAry = line.split(" ");
			this.busId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
	}

	/**
	 * create BranchId to model array number mapping relationship 
	 * by loading the mapping info stored in the mapping file
	 * 
	 * @param filename BranchId to model array number mapping filename
	 */
	public void createBranchId2NoMapping(String filename) {
		this.branchId2NoMapping = new HashMap<>();
		loadTextFile(filename, line -> {
			// format: Bus1->Bus2(1) 0
			String[] strAry = line.split(" ");
			this.branchId2NoMapping.put(strAry[0], new Integer(strAry[1]));
		});
	}
	
	/**
	 * create network operation pattern list 
	 * by loading the pattern info stored in the file
	 * 
	 * @param filename network operation pattern info filename
	 */	
	public void createNetOptPatternSet(String filename) {
		this.netOptPatterns = new HashMap<>();
		loadTextFile(filename, line -> {
			// Pattern-1, missingBus [ Bus15 ], missingBranch [ Bus9->Bus15(1) Bus13->Bus15(1) ]
			NetOptPattern p = UtilFunction.createNetOptPattern(line);
			this.netOptPatterns.put(p.getName(), p);
		});
	}	
	
	private void loadTextFile(String filename, Consumer<String> processor) {
		try (Stream<String> stream = Files.lines(Paths.get(filename))) {
			stream.filter(line -> {return !line.startsWith("#") && 
					                      !line.trim().equals("");})
				  .forEach(processor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * save the network operation pattern info to a file
	 * 
	 * @param filename
	 */
	public void saveNetOptPatternSet(String filename) {
		StringBuffer buffer = new StringBuffer();
		this.netOptPatterns.forEach((n,v) -> {
			buffer.append(v.toString() + "\n");
		});
		writeTextFile(filename, buffer);
	}

	/**
	 * save the brnachId to NN-Model index info to a file
	 * 
	 * @param filename
	 */
	public void saveBranchId2NoMapping(String filename) {
		StringBuffer buffer = new StringBuffer();
		this.branchId2NoMapping.forEach((n,v) -> {
			buffer.append(n + " " + v + "\n");
		});
		writeTextFile(filename, buffer);
	}

	/**
	 * save the busId to NN-model index info to a file
	 * 
	 * @param filename
	 */
	public void saveBusId2NoMapping(String filename) {
		StringBuffer buffer = new StringBuffer();
		this.busId2NoMapping.forEach((n,v) -> {
			buffer.append(n + " " + v + "\n");
		});
		writeTextFile(filename, buffer);
	}	
	
	private void writeTextFile(String filename, StringBuffer text) {
		try {
			Files.write(Paths.get(filename), text.toString().getBytes());
			System.out.println("Text file: " + filename + " generated");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
