package com.ibm.odm.mapping.model;

import java.util.ArrayList;
import java.util.List;

import ilog.rules.factory.IlrFlowTaskFactory;
import ilog.rules.factory.IlrPackageElement;
import ilog.rules.factory.IlrPropertyList.Entry;
import ilog.rules.factory.IlrRuleTaskFactory;
import ilog.rules.factory.IlrRulesetElement;
import ilog.rules.factory.IlrTaskFactory;

@SuppressWarnings("deprecation")
public class Flow implements IlrRulesetElement {

	private IlrFlowTaskFactory flowTask;
	private List<BomMember> usedMembers = new ArrayList<BomMember>();
	private List<IlrTaskFactory> taskList = new ArrayList<IlrTaskFactory>();

	public Flow(IlrFlowTaskFactory flow) {
		this.flowTask = flow;
	}

	@Override
	public IlrPackageElement getPackageElement() {
		return flowTask.getPackageElement();
	}

	@Override
	public String getShortName() {
		return flowTask.getName();
	}

	public String getName() {
		return flowTask.getName();
	}

	public List<IlrTaskFactory> getTaskList() {
		return taskList;
	}

	public int getTaskCount() {
		return taskList.size();
	}

	public IlrTaskFactory[] getTasks() {
		IlrTaskFactory[] result = new IlrTaskFactory[taskList.size()];
		taskList.toArray(result);
		return result;
	}

	public void addUsedMember(BomMember member) {
		usedMembers.add(member);
	}

	public IlrFlowTaskFactory getFlowTask() {
		return flowTask;
	}

	public int getRuleSelectorCount() {
		int i = 0;
		for (IlrTaskFactory task : taskList) {
			if (task instanceof IlrRuleTaskFactory) {
				if (((IlrRuleTaskFactory) task).getRuleSelector() != null) {
					i++;
				}
			}
		}

		return i;
	}

	public boolean isMainFlow() {
		boolean bMain = false;

		Entry property = flowTask.getProperties().get("mainflowtask");
		Object value = property.getValue();
		if (value != null && "true".equals(value.toString())) {
			bMain = true;
		}

		return bMain;
	}

	public boolean isSubFlow() {
		return !isMainFlow();
	}

}
