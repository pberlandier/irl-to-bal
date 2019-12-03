package com.ibm.odm.mapping.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ibm.odm.mapping.type.RulePartType;

import ilog.rules.factory.IlrRuleFactory;

@SuppressWarnings("deprecation")
public class Rule {

	private IlrRuleFactory factory;
	private Set<BomMember> conditionUsedMembers = new HashSet<BomMember>();
	private Set<BomMember> actionUsedMembers = new HashSet<BomMember>();

	public Rule(IlrRuleFactory factory) {
		this.factory = factory;
	}

	public IlrRuleFactory getFactory() {
		return factory;
	}

	public String getName() {
		return factory.name;
	}

	public Collection<BomMember> getConditionUsedMembers() {
		return conditionUsedMembers;
	}

	public Collection<BomMember> getActionUsedMembers() {
		return actionUsedMembers;
	}

	public Collection<BomMember> getUsedMembers(RulePartType partType) {
		if (partType == RulePartType.CONDITION) {
			return conditionUsedMembers;
		} else if (partType == RulePartType.ACTION) {
			return actionUsedMembers;
		} else {
			Collection<BomMember> usedMembers = new HashSet<BomMember>(conditionUsedMembers);
			usedMembers.addAll(actionUsedMembers);
			return usedMembers;
		}

	}

	public void addUsedMember(BomMember member, RulePartType partType) {
		if (partType == RulePartType.CONDITION) {
			conditionUsedMembers.add(member);
		} else if (partType == RulePartType.ACTION) {
			actionUsedMembers.add(member);
		}
	}
}
