package org.interpss.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class UtilFunc {
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
