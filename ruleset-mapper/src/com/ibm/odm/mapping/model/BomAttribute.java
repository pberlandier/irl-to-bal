package com.ibm.odm.mapping.model;

import java.io.PrintStream;

import ilog.rules.bom.IlrAttribute;

public class BomAttribute extends BomMember {

	public BomAttribute(IlrAttribute attrib, BomClass bomClass) {
		super(attrib, bomClass);
	}

	public void show(PrintStream os) {
		os.println("    Attribute: " + getName());
	}
}
