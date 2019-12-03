package com.ibm.odm.mapping.baler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Verbalizer {

	private Hashtable<String, String> verbalizations = new Hashtable<String, String>();
	private Hashtable<String, String> variables = new Hashtable<String, String>();
	private Hashtable<String, String> aliases;
	private boolean multivalued = false;

	//private static final Pattern memberPattern = Pattern.compile("^.*\\.([^.]*)#");
	//private static final Pattern subjectPattern = Pattern.compile("^.*\\{(.*)\\}.*\\{(.*)\\}"); 
	
	public enum MemberRole {
		action, navigation, method
	};

	public Verbalizer(String vocPath, String varPath) {
		loadVoc(vocPath);
		loadVar(varPath);
	}

	public void resetAliases() {
		aliases = new Hashtable<String, String>();
	}

	public void addAlias(String alias, String forName) {
		aliases.put(alias, forName);
	}

	public String verbalizeMember(MemberRole role, String key, String object, String[] params) {
		// Get the template verbalized phrase.
		String result = getTemplatePhrase(role, key);
		if (result == null) {
			Logger.getGlobal().severe("Cannot find verbalization for member " + key);
		}

		// Fill-in the place-holders.
		if (object != null) {
			result = result.replace("{this}", verbalizeObject(object));
		}
		if (isMethod(key)) {
			for (int i = 0; i < params.length; i++) {
				result = result.replace("{" + i + "}", params[i]);
			}
		} else {
			String verbSubject = multivalued ? "the $1s" : "the $1";
			result = result.replaceAll("\\{([^\\}]*)\\}", verbSubject);
		}
		return result;
	}

	/**
	 * Iteratively traverses the table of aliases.
	 * 
	 * @param alias
	 * @return
	 */
	public String getAliasedVariable(String alias) {
		String from = alias;
		String to;
		while ((to = aliases.get(from)) != null) {
			from = to;
		}
		return from;
	}

	/**
	 * Load the ruleset variables from the given var file.
	 * 
	 * @param varPath
	 */
	private void loadVar(String varPath) {

		try {
			File varFile = new File(varPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(varFile);
			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName("variables");

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String name = element.getAttribute("name");
					String verbalization = element.getAttribute("verbalization");
					// If verbalization is multiple words, surround it with quotes.
					if (verbalization.contains(" ")) {
						verbalization = "'" + verbalization + "'";
					}
					variables.put(name, verbalization);
				}
			}
		} catch (Exception e) {
			Logger.getGlobal().severe("Error reading variables file: " + e.getMessage());
		}
	}

	/**
	 * Load the vocabulary from the given voc file.
	 * 
	 * @param vocPath
	 */
	private void loadVoc(String vocPath) {
		try {
			BufferedReader bis = new BufferedReader(new FileReader(vocPath));
			String line;
			while ((line = bis.readLine()) != null) {
				String[] kvp = line.split("=", 2);
				if (kvp.length == 2) {
					String key = kvp[0].trim();
					String value = kvp[1].trim();
					if (key.contains("#")) {
						verbalizations.put(key, value);
					}
				}
			}
			bis.close();
		} catch (IOException e) {
			Logger.getGlobal().severe("Error reading vocabulary file: " + e.getMessage());
		}
	}

	private String getFullKey(MemberRole role, String key) {
		if (role == MemberRole.navigation) {
			return key + "#phrase.navigation";
		} else {
			return key + "#phrase.action";
		}
	}

	private String getTemplatePhrase(MemberRole role, String key) {
		String vocKey = getFullKey(role, key);
		String result = verbalizations.get(vocKey);
		return result;
	}

	private String verbalizeObject(String variableName) {
		String verbalized = variables.get(variableName);
		return (verbalized != null) ? verbalized : "?";

	}

	/**
	 * Return true if the key represents a method, as opposed to an attribute.
	 */
	private boolean isMethod(String key) {
		return key.contains("(");
	}
	
	public void setMultivalued(boolean multivalued)
	{
		this.multivalued = multivalued;
	}
}
