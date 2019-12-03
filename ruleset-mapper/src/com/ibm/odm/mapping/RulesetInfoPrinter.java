package com.ibm.odm.mapping;

import com.ibm.odm.mapping.model.BomClass;
import com.ibm.odm.mapping.model.BomMember;

public class RulesetInfoPrinter {

	/**
	 * Prints a list of the BOM members that are not used in the ruleset.
	 * 
	 * @param map
	 */
	public static void printUnusedBOMMembers(RulesetMapper map) {
		System.out.println();
		System.out.println("+------------------+");
		System.out.println("|Unused BOM members|");
		System.out.println("+------------------+");
		for (BomClass bomClass : map.getBom().getClasses()) {
			System.out.println();
			System.out.println("Class: " + bomClass.getName());
			for (BomMember member : bomClass.getMembers()) {
				if (member.isUnused()) {
					System.out.println("   Member: " + member.getFullyQualifiedName());
				}
			}
		}
	}

}
