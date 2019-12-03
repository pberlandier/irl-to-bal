package com.ibm.odm.mapping;

import java.util.Collection;
import java.util.logging.Logger;

import com.ibm.odm.mapping.baler.Confectioner;
import com.ibm.odm.mapping.baler.Verbalizer;
import com.ibm.odm.mapping.model.Bom;
import com.ibm.odm.mapping.model.BomMember;
import com.ibm.odm.mapping.model.Flow;
import com.ibm.odm.mapping.model.Rule;
import com.ibm.odm.mapping.model.Ruleset;
import com.ibm.odm.mapping.type.RulePartType;

public class RulesetMapper {

	RulesetLoader loader;

	public RulesetMapper(String rulesetArchivePath) throws MappingException {
		loader = new RulesetLoader(rulesetArchivePath);
	}

	/**
	 * Build usage maps from the rules.
	 */
	public void buildRuleMap(Verbalizer verbalizer, Confectioner confectioner) {
		Logger.getGlobal().info("Processing rules");

		RuleExplorer ruleExplorer = new RuleExplorer(this, verbalizer, confectioner);
		for (Rule rule : getRules()) {
			ruleExplorer.explore(rule.getFactory());
		}
	}

	/**
	 * Build usage maps from the flows.
	 */
	public void buildFlowMap() {
		Logger.getGlobal().info("Processing flows");

		FlowExplorer flowExplorer = new FlowExplorer(this);
		for (Flow flow : getFlows()) {
			flowExplorer.explore(flow.getFlowTask());
		}
	}

	public void addRuleUsage(String className, String memberName, String ruleName, RulePartType partType) {
		// Logger.getGlobal().info(" + " + ruleName + "(" + className + "." + memberName +
		// ")");
		BomMember member = loader.getBom().getMember(className, memberName);
		//
		// Member may be from the Boot BOM.
		//
		if (member != null) {
			Rule rule = loader.getRules().get(ruleName);
			member.addUsingRule(rule, partType);
			rule.addUsedMember(member, partType);
		}
	}

	public void addFlowUsage(String className, String memberName, String flowName) {
		Flow flow = loader.getFlows().get(flowName);
		BomMember member = getBom().getMember(className, memberName);
		member.addUsingFlow(flow);
		flow.addUsedMember(member);
	}

	public Ruleset getRuleset() {
		return loader.getRuleset();
	}

	public Bom getBom() {
		return loader.getBom();
	}

	public Collection<Rule> getRules() {
		return loader.getRules().values();
	}

	public Collection<Flow> getFlows() {
		return loader.getFlows().values();
	}
}
