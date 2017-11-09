package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SampleCaseTest {

	public static void main(String[] args) {
		writeTextFile("temp/text.txt", new StringBuffer("Test line"));

	}

	private static void writeTextFile(String filename, StringBuffer text) {
		try {
			Files.write(Paths.get(filename), text.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
}
