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

/**
 * Utility functions for the PyGateway
 * 
 * @author Mike
 *
 */
public class UtilFunc {
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

}
