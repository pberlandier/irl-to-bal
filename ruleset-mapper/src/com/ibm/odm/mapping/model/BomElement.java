package com.ibm.odm.mapping.model;

import ilog.rules.bom.IlrModelElement;

public class BomElement {

	private IlrModelElement modelElement;

	public BomElement(IlrModelElement element) {
		this.modelElement = element;
	}

	public IlrModelElement getModelElement() {
		return modelElement;
	}

	public Object getPropertyValue(String propertyKey) {
		return modelElement.getPropertyValue(propertyKey);
	}

	public String getName() {
		return modelElement.getName();
	}

	public String getFullyQualifiedName() {
		return modelElement.getFullyQualifiedName();
	}
}
