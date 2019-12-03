package com.ibm.odm.mapping.model;

import java.io.PrintStream;

import ilog.rules.bom.IlrMethod;

public class BomMethod extends BomMember {

	public BomMethod(IlrMethod method, BomClass bomClass) {
		super(method, bomClass);
	}

	public void show(PrintStream os) {
		os.println("    Method: " + getName());
	}
}
