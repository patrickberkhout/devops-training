package com.github.patrickberkhout.devopstraining;

import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.github.patrickberkhout.devopstraining.TeiidConnector.TeiidQueryResultFiles;

public class TeiidIT {

	Properties properties = FileUtils.readPropertiesFromFile("src/test/resources/teiid/connector.properties");
	String selectSysTables = FileUtils.readStringFromFile(Paths.get("src/test/resources/teiid/select_sys_tables.sql"));

	TeiidConnector connector = (TeiidConnector) new TeiidConnector().withProperties(properties);

	@Test
	public void testAllOutput() {
		// conditions
		String id = "select_sys_tables";
		TeiidQueryResultFiles resultFiles = new TeiidQueryResultFiles();
		resultFiles.setResultCSVFile(Paths.get("target/" + id + "_result.csv"));
		resultFiles.setPlanTxtFile(Paths.get("target/" + id + "_plan.txt"));
		resultFiles.setPlanXmlFile(Paths.get("target/" + id + "_plan.xml"));
		resultFiles.setDebugFile(Paths.get("target/" + id + "_debug.txt"));
		
		// execute
		int count = connector.query( selectSysTables, resultFiles);
		
		// assertions
		Assert.assertEquals(40, count);
	
		Assert.assertEquals(FileUtils.readStringFromFile(Paths.get("src/test/resources/teiid/expected/" + id + "_result.csv")), FileUtils.readStringFromFile(resultFiles.getResultCSVFile()));
		
		// plans are not exactly equal because of different planning times.
		// Assert.assertEquals(FileUtils.readStringFromFile(Paths.get("src/test/resources/teiid/expected/" + id + "_plan.txt")), FileUtils.readStringFromFile(resultFiles.getPlanTxtFile()));
		// Assert.assertEquals(FileUtils.readStringFromFile(Paths.get("src/test/resources/teiid/expected/" + id + "_plan.xml")), FileUtils.readStringFromFile(resultFiles.getPlanXmlFile()));
		
		Assert.assertEquals(FileUtils.readStringFromFile(Paths.get("src/test/resources/teiid/expected/" + id + "_debug.txt")), FileUtils.readStringFromFile(resultFiles.getDebugFile()));
	}
	
	@Test
	public void testNoOutput() {
		int count = connector.query( selectSysTables);
		Assert.assertEquals(40, count);
	}
}
