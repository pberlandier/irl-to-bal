package com.ibm.odm.mapping.model;

import ilog.rules.factory.IlrRulesetFactory;

@SuppressWarnings("deprecation")
public class Ruleset {

	private IlrRulesetFactory factory;

	public Ruleset(IlrRulesetFactory factory) {
		this.factory = factory;
	}

	public IlrRulesetFactory getFactory() {
		return factory;
	}
}
