package com.ibm.odm.mapping.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.ibm.odm.mapping.type.MemberType;
import com.ibm.odm.mapping.type.RulePartType;

import ilog.rules.bom.IlrAttribute;
import ilog.rules.bom.IlrClass;
import ilog.rules.bom.IlrMethod;

public class BomClass extends BomElement {

	private BomEntry bomEntry;
	private HashMap<String, BomAttribute> attributes = new HashMap<String, BomAttribute>();
	private HashMap<String, BomMethod> methods = new HashMap<String, BomMethod>();

	@SuppressWarnings("unchecked")
	public BomClass(IlrClass clazz, BomEntry bomEntry) {
		super(clazz);
		this.bomEntry = bomEntry;

		List<IlrAttribute> ilrAttributes = clazz.getAttributes();
		if (ilrAttributes != null) {
			for (IlrAttribute attrib : ilrAttributes) {
				attributes.put(attrib.getName(), new BomAttribute(attrib, this));
			}
		}

		List<IlrMethod> ilrMethods = clazz.getMethods();
		if (ilrMethods != null) {
			for (IlrMethod method : ilrMethods) {
				methods.put(method.getName(), new BomMethod(method, this));
			}
		}
	}

	public void show(PrintStream os) {
		os.println("  Class: " + getModelElement().getFullyQualifiedName());
		for (BomAttribute bomAttr : attributes.values()) {
			bomAttr.show(os);
		}
		for (BomMethod bomMethod : methods.values()) {
			bomMethod.show(os);
		}
	}

	public BomEntry getBomEntry() {
		return bomEntry;
	}

	public Collection<BomAttribute> getAttributes() {
		return attributes.values();
	}

	public Collection<BomMethod> getMethods() {
		return methods.values();
	}

	public Collection<BomMember> getMembers() {
		Collection<BomMember> members = new ArrayList<BomMember>(getAttributes());
		members.addAll(getMethods());
		return members;
	}

	public BomMember getMember(String memberName) {
		BomMember member = attributes.get(memberName);
		if (member == null) {
			member = methods.get(memberName);
		}
		return member;
	}

	public Collection<Rule> getUsingRules(MemberType memberType, RulePartType partType) {
		Collection<Rule> rules = new HashSet<Rule>();
		if (memberType == MemberType.MEMBER || memberType == MemberType.ATTRIBUTE) {
			for (BomMember member : attributes.values()) {
				rules.addAll(member.getUsingRules(partType));
			}
		}
		if (memberType == MemberType.MEMBER || memberType == MemberType.METHOD) {
			for (BomMember member : methods.values()) {
				rules.addAll(member.getUsingRules(partType));
			}
		}
		return rules;

	}
}
