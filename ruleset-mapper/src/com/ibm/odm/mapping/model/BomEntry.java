package com.ibm.odm.mapping.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.odm.mapping.type.MemberType;
import com.ibm.odm.mapping.type.RulePartType;

import ilog.rules.bom.IlrClass;
import ilog.rules.bom.IlrObjectModel;

public class BomEntry {

	private String name;
	private IlrObjectModel model;
	private HashMap<String, BomClass> classes = new HashMap<String, BomClass>();

	@SuppressWarnings("rawtypes")
	public BomEntry(IlrObjectModel model, String name) {
		this.name = name;
		this.model = model;

		Iterator iter = model.allClasses();
		while (iter.hasNext()) {
			IlrClass clz = ((IlrClass) iter.next());
			classes.put(clz.getFullyQualifiedName(), new BomClass(clz, this));
		}
	}

	public BomClass getClass(String className) {
		return classes.get(className);
	}

	public void show(PrintStream os) {
		os.println("Entry: " + name);
		for (BomClass bomClass : classes.values()) {
			bomClass.show(os);
		}
	}

	public String getName() {
		return name;
	}

	public IlrObjectModel getModel() {
		return model;
	}

	public Collection<BomClass> getClasses() {
		return classes.values();
	}

	/**
	 * Returns all the members (attributes + methods) of the BOM entry.
	 * 
	 * @return
	 */
	public Collection<BomMember> getMembers() {
		Collection<BomMember> members = new ArrayList<BomMember>();
		for (BomClass bomClass : getClasses()) {
			members.addAll(bomClass.getMembers());
		}
		return members;
	}

	/**
	 * Returns the rules which are using in the given part type the members of a
	 * given type of the BOM entry
	 * 
	 * @param memberType
	 * @param partType
	 * @return
	 */
	public Collection<Rule> getUsingRules(MemberType memberType, RulePartType partType) {
		Collection<Rule> rules = new HashSet<Rule>();
		for (BomClass bomClass : getClasses()) {
			rules.addAll(bomClass.getUsingRules(memberType, partType));
		}
		return rules;
	}
}
