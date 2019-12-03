package com.ibm.odm.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.odm.mapping.baler.Confectioner;
import com.ibm.odm.mapping.baler.Verbalizer;
import com.ibm.odm.mapping.type.RulePartType;

import ilog.rules.factory.IlrCollectCondition;
import ilog.rules.factory.IlrCollectElement;
import ilog.rules.factory.IlrCondition;
import ilog.rules.factory.IlrEvaluateCondition;
import ilog.rules.factory.IlrExistsCondition;
import ilog.rules.factory.IlrNotCondition;
import ilog.rules.factory.IlrRuleFactory;
import ilog.rules.factory.IlrSimpleCondition;
import ilog.rules.factory.IlrStatement;
import ilog.rules.factory.IlrTest;
import ilog.rules.factory.IlrValue;
import ilog.rules.factory.IlrVariable;

/**
 * Used to explore the content of a rule. In particular, gathers references to
 * BOM members in conditions and actions.
 * 
 * @author pberland@us.ibm.com
 * 
 */
@SuppressWarnings("deprecation")
public class RuleExplorer extends BaseExplorer {

	private IlrRuleFactory currentRule;
	private RulePartType rulePartType;
	private RulesetMapper rulesetMap;

	public RuleExplorer(RulesetMapper rulesetMap, Verbalizer verbalizer, Confectioner confectioner) {
		super(verbalizer, confectioner);
		this.rulesetMap = rulesetMap;
	}

	protected void addUsage(String className, String memberName) {
		rulesetMap.addRuleUsage(className, memberName, currentRule.name, rulePartType);
	}

	/**
	 * Explores one rule.
	 * 
	 * @param rule
	 */
	public void explore(IlrRuleFactory rule) {
		Logger.getGlobal().info("Exploring rule " + rule.getShortName());
		currentRule = rule;
		verbalizer.resetAliases();

		rulePartType = RulePartType.CONDITION;
		String ifPart = (String) exploreConditionNode(rule.getConditions());

		rulePartType = RulePartType.ACTION;
		String thenPart = (String) exploreActionNode(rule.getStatements());
		String elsePart = (String) exploreActionNode(rule.getElseStatements());

		// Create the rule string representation.
		String balRuleString = "";
		if (!ifPart.isEmpty()) {
			balRuleString += "if\n" + ifPart;
		}
		balRuleString += "then\n" + thenPart;
		if (!elsePart.isEmpty()) {
			balRuleString += "else\n" + elsePart;
		}

		// Write-out the rule.
		confectioner.make(rule.getShortName(), balRuleString);
	}

	/**
	 * Explores the rule conditions.
	 * 
	 * @param conditions
	 * @return
	 */
	public Object exploreConditionNode(IlrCondition[] conditions) {
		
		List<String> condStrings = new ArrayList<String>();
		for (IlrCondition cond : conditions) {
			String condString = (String) cond.exploreCondition(this);
			if (!condString.isEmpty()) {
				condStrings.add(condString);
			}
		}

		String result = "";
		int count = condStrings.size();
		for (int i = 0; i < count; i++ )
		{
			String condString = condStrings.get(i);
			result += "  " + condString;
			result += (i == count-1) ? "\n" : " and\n";
		}
		return result;
	}

	/**
	 * Explores the rule actions.
	 * 
	 * @param statements
	 * @return
	 */
	public Object exploreActionNode(IlrStatement[] statements) {
		String result = "";
		for (IlrStatement stmt : statements) {
			String stmtString = (String) stmt.exploreStatement(this);
			if (!stmtString.isEmpty()) {
				result += "  " + stmtString + "\n";
			}
		}
		return result;
	}

	/**
	 * >> IlrEvaluateCondition
	 */
	public Object exploreCondition(IlrEvaluateCondition cond) {
		IlrVariable[] varArray = cond.getBindings();
		for (IlrVariable var : varArray) {
			var.exploreValue(this);
		}

		String result = "";
		IlrTest[] tests = cond.getTests();
		for (int i = 0; i < tests.length; i++) {
			IlrTest test = tests[i];
			if (i > 0 ) { result += "  "; }
			result += (String) test.exploreTest(this);
			if (i != tests.length - 1) {
				result += " and\n";
			}
		}
		return result;
	}

	/**
	 * IlrCollectCondition
	 */
	public Object exploreCondition(IlrCollectCondition cond) {
		IlrCollectElement element = cond.getCollectElement();
		if (element == null) {
			return null;
		}
		IlrValue val = element.getEnumerator();
		if (val == null) {
			return null;
		}
		val.exploreValue(this);
		return null;
	}

	/**
	 * >> IlrSimpleCondition
	 */
	public Object exploreCondition(IlrSimpleCondition cond) {
		IlrVariable[] varArray = cond.getBindings();
		for (IlrVariable var : varArray) {
			String binding = (String) var.exploreValue(this);
			Logger.getGlobal().warning("Unhandled binding: " + binding);
		}

		// Note that some simple conditions are just performing variable binding, no
		// test.
		IlrVariable var = cond.getObjectBinding();
		if (var != null) {
			String bound = (String) var.exploreValue(this);

			IlrValue val = cond.getEnumerator();
			if (val != null) {
				switch (cond.getEnumeratorClause()) {
				case "from":
					verbalizer.addAlias(bound, (String) val.exploreValue(this));
					break;
				case "in":
					Logger.getGlobal().warning("Unhandled enumerator: in");
					break;
				}
			}
		}

		String result = "";
		IlrTest[] tests = cond.getTests();
		for (int i = 0; i < tests.length; i++) {
			IlrTest test = tests[i];
			result += (String) test.exploreTest(this);
			if (i != tests.length - 1) {
				result += " and\n";
			}
		}
		return result;
	}

	/**
	 * >> IlrNotCondition
	 */
	public Object exploreCondition(IlrNotCondition cond) {
		IlrVariable[] varArray = cond.getBindings();
		for (IlrVariable var : varArray) {
			String binding = (String)var.exploreValue(this);
			Logger.getGlobal().warning("Unhandled binding: " + binding);
		}

		IlrValue val = cond.getEnumerator();
		if (val != null) {
			val.exploreValue(this);
		}

		String result = "";
		IlrTest[] tests = cond.getTests();
		for (int i = 0; i < tests.length; i++) {
			IlrTest test = tests[i];
			result += "NOT " + (String) test.exploreTest(this);
			if (i != tests.length - 1) {
				result += " and\n";
			}
		}
		return result;
	}

	/**
	 * IlrExistsCondition
	 */
	public Object exploreCondition(IlrExistsCondition cond) {
		IlrVariable[] varArray = cond.getBindings();
		for (IlrVariable var : varArray) {
			var.exploreValue(this);
		}

		IlrValue val = cond.getEnumerator();
		if (val != null) {
			val.exploreValue(this);
		}

		IlrTest[] tests = cond.getTests();
		for (IlrTest test : tests) {
			test.exploreTest(this);
		}
		return null;
	}
}
