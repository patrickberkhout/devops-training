package com.github.patrickberkhout.devopstraining;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.opencsv.CSVWriter;

public class FileUtils {
	private static final String UTF_8 = "UTF-8";

	public static String readStringFromFile(Path filename) {
		try {
			return new String(Files.readAllBytes(filename), UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void writeStringToFile(Path filename, String text) {
		try {
			Files.write(filename, text.getBytes(UTF_8), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Properties readPropertiesFromFile(String filename) {
		Properties p = new Properties();
		try {
			p.load(new FileReader(new File(filename)));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return p;
	}

	/**
	 * @param filename
	 * @param resultSet
	 * @return number of data rows, header excluded
	 */
	public static int writeResultSetToCSVFile(Path filename, ResultSet resultSet) {
		int count;
		try {
			Files.deleteIfExists(filename);
			CSVWriter wr = new CSVWriter(new FileWriter(filename.toFile()));
			boolean includeColumnNames = true;
			count =  wr.writeAll(resultSet, includeColumnNames);
			wr.flush();
			wr.close();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return count - 1;
	}

}
