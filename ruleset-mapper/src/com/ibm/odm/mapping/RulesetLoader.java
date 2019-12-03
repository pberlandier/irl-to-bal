package com.ibm.odm.mapping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

import com.ibm.odm.mapping.model.Bom;
import com.ibm.odm.mapping.model.BomEntry;
import com.ibm.odm.mapping.model.Flow;
import com.ibm.odm.mapping.model.Rule;
import com.ibm.odm.mapping.model.Ruleset;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import ilog.rules.archive.IlrJarArchiveLoader;
import ilog.rules.archive.IlrRulesetArchive;
import ilog.rules.archive.IlrRulesetArchiveLoader;
import ilog.rules.bom.dynamic.IlrDynamicObjectModel;
import ilog.rules.bom.serializer.IlrJavaSerializer;
import ilog.rules.engine.IlrRulesetArchiveParser;
import ilog.rules.factory.IlrFlowTaskFactory;
import ilog.rules.factory.IlrRuleFactory;
import ilog.rules.factory.IlrRulesetFactory;
import ilog.rules.factory.IlrTaskFactory;

@SuppressWarnings("deprecation")
public class RulesetLoader {

	private Ruleset ruleset;
	private Bom bom;
	private HashMap<String, Rule> rules;
	private HashMap<String, Flow> flows;

	public RulesetLoader(String rulesetArchivePath) throws MappingException {
		load(rulesetArchivePath);
	}

	/**
	 * Loads the ruleset to be mapped.
	 * 
	 * @param rulesetArchivePath
	 * @throws MappingException
	 */
	private void load(String rulesetArchivePath) throws MappingException {
		FileInputStream is;
		JarInputStream jis = null;
	
		try {
			is = new FileInputStream(rulesetArchivePath);
			jis = new JarInputStream(is);
		} catch (FileNotFoundException e) {
			throw new MappingException("Ruleset archive file not found");
		} catch (IOException e) {
			throw new MappingException("Error while loading ruleset archive");
		}

		IlrRulesetArchiveLoader rulesetLoader = new IlrJarArchiveLoader(jis);
		IlrRulesetArchiveParser rulesetParser = new IlrRulesetArchiveParser();
		IlrRulesetFactory rulesetFactory = rulesetParser.checkArchive(rulesetLoader);

		if (rulesetFactory == null) {
			throw new MappingException("Error in ruleset archive content");
		}
		Logger.getGlobal().fine("Parsed ruleset");

		IlrRulesetArchive rulesetArchive = rulesetLoader.getCurrentArchive();

		ruleset = new Ruleset(rulesetFactory);
		bom = collectBOMEntries(rulesetArchive);
		rules = collectRules(rulesetFactory);
		flows = collectFlows(rulesetFactory);

		rulesetLoader.endLoad();
	}

	/**
	 * Collects all rules from the ruleset factory.
	 * 
	 * @param rulesetFactory
	 * @return
	 */
	private HashMap<String, Rule> collectRules(IlrRulesetFactory rulesetFactory) {
		HashMap<String, Rule> rules = new HashMap<String, Rule>();
		IlrRuleFactory[] rawRules = rulesetFactory.getRules();

		for (IlrRuleFactory rule : rawRules) {
			Rule localRule = new Rule(rule);
			rules.put(localRule.getName(), localRule);
		}
		Logger.getGlobal().info(rules.size() + " rules collected");
		return rules;
	}

	/**
	 * Collects all flows from the ruleset factory.
	 * 
	 * @param rulesetFactory
	 * @return
	 * @throws MappingException
	 */
	private HashMap<String, Flow> collectFlows(IlrRulesetFactory rulesetFactory) throws MappingException {
		HashMap<String, Flow> ruleflows = new HashMap<String, Flow>();
		IlrTaskFactory[] tasks = rulesetFactory.getTasks();

		for (IlrTaskFactory task : tasks) {
			if (task instanceof IlrFlowTaskFactory) {
				IlrFlowTaskFactory flow = (IlrFlowTaskFactory) task;
				if (FactoryUtil.isRuleFlow(flow)) {
					Flow localFlow = new Flow(flow);
					ruleflows.put(localFlow.getName(), localFlow);
				}
			} else {
				String taskName = FactoryUtil.getFullBusinessName(task);
				for (Flow candidate : ruleflows.values()) {
					String candidateName = FactoryUtil.getFullBusinessName(candidate);
					if (taskName.startsWith(candidateName + ">")) {
						candidate.getTaskList().add(task);
						break;
					}
				}
			}
		}

		Logger.getGlobal().info(ruleflows.size() + " flows collected");
		return ruleflows;
	}

	/**
	 * Collects all the BOM entries from the ruleset archive.
	 * 
	 * @param rulesetArchive
	 * @return
	 * @throws MappingException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Bom collectBOMEntries(IlrRulesetArchive rulesetArchive) throws MappingException {
		ArrayList<BomEntry> bomEntries = new ArrayList<BomEntry>();
		Collection elements = rulesetArchive.getElements();
		try {
			for (IlrRulesetArchive.Element bomElement : (Collection<IlrRulesetArchive.Element>) elements) {
				String name = bomElement.getKey();
				if (name.endsWith(".bom")) {
					byte[] content = bomElement.getContent();
					InputStream in = new ByteInputStream(content, content.length);
					String entryName = name.substring(name.lastIndexOf("/") + 1);
					BomEntry bomEntry = loadBomEntry(in, entryName);
					in.close();
					bomEntries.add(bomEntry);
				}
			}
		} catch (IOException e) {
			throw new MappingException("Loading bom entries failed");
		}
		Logger.getGlobal().info(bomEntries.size() + " BOM entries collected");
		return new Bom(bomEntries);
	}

	private BomEntry loadBomEntry(InputStream in, String name) throws MappingException {
		InputStreamReader reader = new InputStreamReader(in);
		IlrDynamicObjectModel bom = new IlrDynamicObjectModel(ilog.rules.bom.IlrObjectModel.Kind.BUSINESS);
		try {
			new IlrJavaSerializer().readPartialObjectModel(bom, reader);
		} catch (Exception e) {
			throw new MappingException("BOM entry load failed");
		}
		return new BomEntry(bom, name);
	}

	public Ruleset getRuleset() {
		return ruleset;
	}

	public Bom getBom() {
		return bom;
	}

	public HashMap<String, Rule> getRules() {
		return rules;
	}

	public HashMap<String, Flow> getFlows() {
		return flows;
	}
}
