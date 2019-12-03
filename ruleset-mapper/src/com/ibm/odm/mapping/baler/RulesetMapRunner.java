package com.ibm.odm.mapping.baler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.odm.mapping.MappingException;
import com.ibm.odm.mapping.RulesetInfoPrinter;
import com.ibm.odm.mapping.RulesetMapper;

public class RulesetMapRunner {

	private static Properties readProperties()
	{
	    Properties properties = new Properties();
	    InputStream is = RulesetMapRunner.class.getClassLoader().getResourceAsStream("input.properties");
	      try {
	        properties.load(is);
	      } catch (IOException e) {
	    	  Logger.getGlobal().severe("Error loading input properties: " + e.getMessage());
	      }
	      return properties;
	  }

	public static void main(String[] args) {
		String formatName = "java.util.logging.SimpleFormatter.format";
		System.setProperty(formatName, "%4$s: %5$s%n");
		Logger.getGlobal().setLevel(Level.FINEST);
		
		Properties props = readProperties();
		String projectPath = props.getProperty("ruleproject.root");
		String confectionPath = projectPath + props.getProperty("confection.path");	
		String rulesetArchivePath = projectPath + props.getProperty("ruleset.path");	
		String varPath = projectPath + props.getProperty("variables.path");	
		String vocPath = projectPath + props.getProperty("vocabulary.path");	
		try {			
			Logger.getGlobal().info("Mapping ruleset " + rulesetArchivePath);
			RulesetMapper map = new RulesetMapper(rulesetArchivePath);
			Verbalizer verbalizer = new Verbalizer(vocPath, varPath);
			Confectioner confectioner = new Confectioner(confectionPath);
			
			map.buildRuleMap(verbalizer, confectioner);
			
			// Print ruleset information
			RulesetInfoPrinter.printUnusedBOMMembers(map);
		} catch (MappingException e) {
			Logger.getGlobal().severe(e.getMessage());
		}
	}

}
