package com.ibm.odm.mapping.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.ibm.odm.mapping.type.RulePartType;

import ilog.rules.bom.IlrMember;

public class BomMember extends BomElement {

	private BomClass bomClass;
	private Set<Rule> conditionUsingRules = new HashSet<Rule>();
	private Set<Rule> actionUsingRules = new HashSet<Rule>();
	private Set<Flow> usingFlows = new HashSet<Flow>();

	public BomMember(IlrMember member, BomClass bomClass) {
		super(member);
		this.bomClass = bomClass;
	}

	public BomClass getBomClass() {
		return bomClass;
	}

	public boolean isUnused() {
		return ((usingFlows.size() == 0) && (conditionUsingRules.size() == 0) && (actionUsingRules.size() == 0));
	}

	public boolean isUpdateObjectState() {
		boolean enabled = false;
		if (getPropertyValue("update") != null) {
			enabled = true;
		}
		return enabled;
	}

	public Collection<Rule> getUsingRules(RulePartType partType) {
		Collection<Rule> rules = null;
		if (partType == RulePartType.CONDITION) {
			rules = conditionUsingRules;
		} else if (partType == RulePartType.ACTION) {
			rules = actionUsingRules;
		} else if (partType == RulePartType.RULE) {
			rules = new HashSet<Rule>(conditionUsingRules);
			rules.addAll(actionUsingRules);
		}
		return rules;
	}

	public Collection<Flow> getUsingFlows() {
		return usingFlows;
	}

	public void addUsingRule(Rule rule, RulePartType partType) {
		if (partType == RulePartType.CONDITION) {
			conditionUsingRules.add(rule);
		} else if (partType == RulePartType.ACTION) {
			actionUsingRules.add(rule);
		} else {
			Logger.getGlobal().severe("Unexpected rule part type");
		}
	}

	public void addUsingFlow(Flow flow) {
		usingFlows.add(flow);
	}
}
