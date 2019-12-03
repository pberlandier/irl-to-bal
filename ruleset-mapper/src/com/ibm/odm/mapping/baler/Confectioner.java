package com.ibm.odm.mapping.baler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class Confectioner {

	private String outputPath;

	public Confectioner(String outputPath) {
		this.outputPath = outputPath;
	}

	public static String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<ilog.rules.studio.model.brl:ActionRule xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:ilog.rules.studio.model.brl=\"http://ilog.rules.studio/model/brl.ecore\">\r\n"
			+ "  <name>%s</name>\r\n" + "  <uuid>%s</uuid>\r\n" + "  <locale>en_US</locale>\r\n"
			+ "  <definition><![CDATA[%s]]></definition>\r\n" + "</ilog.rules.studio.model.brl:ActionRule>\r\n";

	private String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public void make(String name, String body) {
		String balName = name + " as BAL";
		String uuid = getUUID();
		String rule = String.format(template, balName, uuid, body);

		String rulePath = outputPath + File.separator + balName + ".brl";
		try {
			BufferedWriter os = new BufferedWriter(new FileWriter(rulePath));
			os.write(rule);
			os.close();
		} catch (IOException e) {
			Logger.getGlobal().severe("Error generating rule " + balName + ": " + e.getMessage());
		}
	}
}
