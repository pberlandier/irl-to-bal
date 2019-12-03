package com.ibm.odm.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.odm.mapping.model.BomElement;
import com.ibm.odm.mapping.model.BomEntry;

import ilog.rules.bom.IlrModelElement;
import ilog.rules.factory.IlrFlowTaskFactory;
import ilog.rules.factory.IlrPackageFactory;
import ilog.rules.factory.IlrRuleFactory;
import ilog.rules.factory.IlrRulesetElement;
import ilog.rules.factory.IlrRulesetFactory;
import ilog.rules.util.IlrIdConverter;

/**
 * 
 * @author pberland@us.ibm.com
 *
 */
@SuppressWarnings("deprecation")
public class FactoryUtil {

	public static String getRulePackageName(String ruleName) {
		String pkgName = "";
		if (ruleName.indexOf('.') != -1) {
			pkgName = ruleName.substring(0, ruleName.lastIndexOf('.'));
		}
		return pkgName;
	}

	public static String getShortName(String fullName) {
		return fullName.substring(fullName.lastIndexOf('.') + 1);
	}

	public static boolean checkNaming(String name, String format) {

		Pattern p = Pattern.compile(format);
		Matcher m = p.matcher(name);
		return m.matches();
	}

	public static boolean isDTRule(IlrRuleFactory rule) {
		String dtName = getDTName(rule);
		return dtName != null && !dtName.isEmpty();
	}

	public static boolean isRuleFlow(IlrFlowTaskFactory flow) throws MappingException {
		return getShortBusinessName(flow).indexOf(">") == -1;
	}

	public static String getDTName(IlrRuleFactory rule) {
		return (String) rule.getPropertyValue("ilog.rules.dt");
	}

	public static String getShortBusinessName(Object node) throws MappingException {
		String fullName = getFullBusinessName(node);
		return getShortName(fullName);
	}

	public static String getFullBusinessName(Object node) throws MappingException {
		String buzName = "";
		if (node instanceof IlrRulesetFactory) {
			buzName = toBusinessName(((IlrRulesetFactory) node).getName());
		} else if (node instanceof IlrPackageFactory) {
			buzName = toBusinessName(((IlrPackageFactory) node).getName());
		} else if (node instanceof IlrRulesetElement) {
			buzName = toBusinessName(getFullTechnicalName((IlrRulesetElement) node));
		} else if (node instanceof BomEntry) {
			buzName = ((BomEntry) node).getName();
		} else if (node instanceof BomElement) {
			buzName = ((BomElement) node).getFullyQualifiedName();
		} else if (node instanceof IlrModelElement) {
			buzName = ((IlrModelElement) node).getFullyQualifiedName();
		} else {
			throw new MappingException("unsupported ruleset element : " + node.getClass().getName());
		}

		return buzName;
	}

	private static String toBusinessName(String technicalName) {
		return IlrIdConverter.getBusinessIdentifier(technicalName);
	}

	private static String getFullTechnicalName(IlrRulesetElement element) {

		String name = element.getShortName();

		if (element.getPackageElement() != null) {
			if (!element.getPackageElement().isDefaultPackage()) {
				name = element.getPackageElement().getName() + "." + name;
			}
		}

		return name;
	}
}
