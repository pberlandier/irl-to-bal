package com.ibm.odm.mapping.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.ibm.odm.mapping.type.MemberType;
import com.ibm.odm.mapping.type.RulePartType;

public class Bom {

	/**
	 * The BOM is organized through entries.
	 */
	private ArrayList<BomEntry> entries;

	public Bom(ArrayList<BomEntry> entries) {
		this.entries = entries;
	}

	public Collection<BomClass> getClasses() {
		List<BomClass> classes = new ArrayList<BomClass>();
		for (BomEntry entry : entries) {
			classes.addAll(entry.getClasses());
		}
		return classes;
	}

	public Collection<BomMember> getMembers() {
		List<BomMember> members = new ArrayList<BomMember>();
		for (BomEntry entry : entries) {
			members.addAll(entry.getMembers());
		}
		return members;
	}

	/**
	 * Returns the given class from one of the BOM's entries.
	 * 
	 * @param className
	 * @return
	 */
	public BomClass getClass(String className) {
		for (BomEntry entry : entries) {
			BomClass bomClass = entry.getClass(className);
			if (bomClass != null) {
				return bomClass;
			}
		}
		return null;
	}

	/**
	 * Returns the given member from one of the BOM's entries.
	 * 
	 * @param className
	 * @param memberName
	 * @return
	 */
	public BomMember getMember(String className, String memberName) {
		BomClass bomClass = getClass(className);
		if (bomClass != null) {
			return bomClass.getMember(memberName);
		}
		return null;
	}

	public Collection<Rule> getUsingRules(MemberType memberType, RulePartType partType) {
		Collection<Rule> rules = new HashSet<Rule>();
		for (BomEntry entry : entries) {
			rules.addAll(entry.getUsingRules(memberType, partType));
		}
		return rules;
	}
}
