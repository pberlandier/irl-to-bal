package com.ibm.odm.mapping;

import java.util.ArrayList;
import java.util.logging.Logger;

import ilog.rules.factory.IlrFlowTaskFactory;
import ilog.rules.factory.IlrFunctionFactory;
import ilog.rules.factory.IlrFunctionTaskFactory;
import ilog.rules.factory.IlrRuleTaskFactory;
import ilog.rules.factory.IlrStatement;
import ilog.rules.factory.IlrTaskForkNodeStatement;
import ilog.rules.factory.IlrTaskGotoNodeStatement;
import ilog.rules.factory.IlrTaskIfNodeStatement;
import ilog.rules.factory.IlrTaskInstanceStatement;
import ilog.rules.factory.IlrTaskJoinNodeStatement;
import ilog.rules.factory.IlrTaskSwitchNodeStatement;
import ilog.rules.factory.IlrTaskWhileNodeStatement;
import ilog.rules.factory.IlrTest;

@SuppressWarnings("deprecation")
public class FlowExplorer extends BaseExplorer {

	private IlrFlowTaskFactory currentFlow;
	private RulesetMapper rulesetMap;

	public FlowExplorer(RulesetMapper rulesetMap) {
		super(null, null);
		this.rulesetMap = rulesetMap;
	}

	protected void addUsage(String className, String memberName) {
		Logger.getGlobal()
				.info(" + " + className + "." + memberName + "(" + currentFlow.getName() + ")");
		rulesetMap.addFlowUsage(className, memberName, currentFlow.getName());
	}

	public void explore(IlrFlowTaskFactory flow) {
		this.currentFlow = flow;

		ArrayList<?> stmtList = flow.getAllStatements();

		for (Object stmt : stmtList) {
			IlrStatement statement = (IlrStatement) stmt;
			statement.exploreStatement(this);
		}
	}

	public Object exploreStatement(IlrTaskIfNodeStatement ifNode) {
		IlrTest test = ifNode.getTest();
		test.exploreTest(this);
		return null;
	}

	public Object exploreStatement(IlrTaskInstanceStatement instance) {
		instance.getTask().exploreTask(this);
		return null;
	}

	public Object exploreTask(IlrRuleTaskFactory ruleTask) {
		exploreFunction(ruleTask.getInitialActions());
		exploreFunction(ruleTask.getRuleSelector());
		exploreFunction(ruleTask.getFinalActions());
		return null;
	}

	public Object exploreTask(IlrFlowTaskFactory flowTask) {
		exploreFunction(flowTask.getInitialActions());
		exploreFunction(flowTask.getFinalActions());
		return null;
	}

	public Object exploreTask(IlrFunctionTaskFactory functionTask) {
		exploreFunction(functionTask.getInitialActions());
		exploreFunction(functionTask.getFunction());
		exploreFunction(functionTask.getFinalActions());
		return null;
	}

	public Object exploreFunction(IlrFunctionFactory function) {
		if (function != null) {
			IlrStatement[] statements = function.getStatements();
			for (IlrStatement stmt : statements) {
				stmt.exploreStatement(this);
			}
		}
		return null;
	}

	public Object exploreStatement(IlrTaskForkNodeStatement forkNode) {
		return null;
	}

	public Object exploreStatement(IlrTaskGotoNodeStatement gotoNode) {
		return null;
	}

	public Object exploreStatement(IlrTaskJoinNodeStatement joinNode) {
		return null;
	}

	public Object exploreStatement(IlrTaskSwitchNodeStatement switchNode) {
		return null;
	}

	public Object exploreStatement(IlrTaskWhileNodeStatement whileNode) {
		whileNode.getTest().exploreTest(this);
		return null;
	}
}
