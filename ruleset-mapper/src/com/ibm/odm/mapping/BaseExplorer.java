package com.ibm.odm.mapping;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import com.ibm.odm.mapping.baler.Confectioner;
import com.ibm.odm.mapping.baler.Verbalizer;

import ilog.rules.factory.IlrArrayElement;
import ilog.rules.factory.IlrArrayLength;
import ilog.rules.factory.IlrAsValue;
import ilog.rules.factory.IlrAssertAction;
import ilog.rules.factory.IlrAssignable;
import ilog.rules.factory.IlrAssignment;
import ilog.rules.factory.IlrBinaryOperator;
import ilog.rules.factory.IlrBinaryTest;
import ilog.rules.factory.IlrBinaryTester;
import ilog.rules.factory.IlrBinaryValue;
import ilog.rules.factory.IlrBindStatement;
import ilog.rules.factory.IlrBreakStatement;
import ilog.rules.factory.IlrCastValue;
import ilog.rules.factory.IlrClassTypeValue;
import ilog.rules.factory.IlrCollectInSourceValue;
import ilog.rules.factory.IlrComponentPropertyValue;
import ilog.rules.factory.IlrCondition;
import ilog.rules.factory.IlrConstantValue;
import ilog.rules.factory.IlrContextValue;
import ilog.rules.factory.IlrContinueStatement;
import ilog.rules.factory.IlrFactoryAdapter;
import ilog.rules.factory.IlrFieldValue;
import ilog.rules.factory.IlrForStatement;
import ilog.rules.factory.IlrForeachStatement;
import ilog.rules.factory.IlrFunctionInvocation;
import ilog.rules.factory.IlrIfStatement;
import ilog.rules.factory.IlrIfStatement.ElseBlock;
import ilog.rules.factory.IlrIndexedComponentPropertyValue;
import ilog.rules.factory.IlrInstanceOfTest;
import ilog.rules.factory.IlrInstanceValue;
import ilog.rules.factory.IlrIntervalValue;
import ilog.rules.factory.IlrMethodInvocation;
import ilog.rules.factory.IlrModifyAction;
import ilog.rules.factory.IlrNaryTest;
import ilog.rules.factory.IlrNewArrayInstanceValue;
import ilog.rules.factory.IlrNewInstanceValue;
import ilog.rules.factory.IlrNotTest;
import ilog.rules.factory.IlrObjectValue;
import ilog.rules.factory.IlrPropertyAccessValue;
import ilog.rules.factory.IlrRetractAction;
import ilog.rules.factory.IlrReturnStatement;
import ilog.rules.factory.IlrScopeValue;
import ilog.rules.factory.IlrSimpleCondition;
import ilog.rules.factory.IlrStatement;
import ilog.rules.factory.IlrStatementBlock;
import ilog.rules.factory.IlrStaticFieldValue;
import ilog.rules.factory.IlrStaticMethodInvocation;
import ilog.rules.factory.IlrTest;
import ilog.rules.factory.IlrTestValue;
import ilog.rules.factory.IlrThrowStatement;
import ilog.rules.factory.IlrTryCatchFinallyStatement;
import ilog.rules.factory.IlrTryCatchFinallyStatement.FinallyBlock;
import ilog.rules.factory.IlrUnaryOperator;
import ilog.rules.factory.IlrUnaryTest;
import ilog.rules.factory.IlrUnaryValue;
import ilog.rules.factory.IlrUnknownTest;
import ilog.rules.factory.IlrUpdateAction;
import ilog.rules.factory.IlrValue;
import ilog.rules.factory.IlrVariable;
import ilog.rules.factory.IlrVariableElementValue;
import ilog.rules.factory.IlrWhileStatement;

/**
 * 
 * @author pberland@us.ibm.com
 *
 */
@SuppressWarnings("deprecation")
public abstract class BaseExplorer extends IlrFactoryAdapter {

	protected Verbalizer verbalizer;
	protected Confectioner confectioner;

	protected abstract void addUsage(String className, String memberName);

	public BaseExplorer(Verbalizer verbalizer, Confectioner confectioner) {
		this.verbalizer = verbalizer;
		this.confectioner = confectioner;
	}

	/**
	 * IlrStaticMethodInvocation
	 */
	public Object exploreValue(IlrStaticMethodInvocation method) {
		addUsage(method.getClassName(), method.getName());
		IlrValue var = method.getObject();
		if (var != null) {
			var.exploreValue(this);
		}

		for (IlrValue val : method.getArguments()) {
			val.exploreValue(this);
		}
		return null;
	}

	/**
	 * >> IlrMethodInvocation
	 */
	public Object exploreValue(IlrMethodInvocation method) {
		addUsage(method.getClassName(), method.getName());
		IlrValue[] args = method.getArguments();
		//
		// Create the key for the VOC entry.
		//
		String key = "";
		key += method.getClassName() + "." + method.getName();
		key += "(";
		for (int i = 0; i < args.length; i++) {
			key += args[i].getXOMType().getFullyQualifiedRawName();
			key += (i == args.length - 1) ? ")" : ",";
		}

		String verbObject = (String) method.getObject().exploreValue(this);
		verbObject = verbalizer.getAliasedVariable(verbObject);
		String[] verbArgs = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			verbArgs[i] = (String) args[i].exploreValue(this);
		}
		String result = verbalizer.verbalizeMember(Verbalizer.MemberRole.method, key, verbObject, verbArgs);
		return result;
	}

	/**
	 * IlrStaticFieldValue
	 */
	public Object exploreValue(IlrStaticFieldValue val) {
		addUsage(val.getClassName(), val.getName());
		return null;
	}

	/**
	 * IlrTestValue
	 */
	public Object exploreValue(IlrTestValue testValue) {
		IlrTest test = testValue.getTest();
		String result = (String)test.exploreTest(this);
		return result;
	}

	/**
	 * >> IlrFieldValue
	 */
	public Object exploreValue(IlrFieldValue val) {
		addUsage(val.getClassName(), val.getName());

		String verbObject = null;
		if (val.getObject() instanceof IlrVariableElementValue) {
			verbObject = ((IlrVariableElementValue) val.getObject()).getElement().getName();
		} else if (val.getObject() instanceof IlrObjectValue) {
			IlrCondition cond = ((IlrObjectValue) val.getObject()).getCondition();
			if (cond instanceof IlrSimpleCondition) {
				IlrSimpleCondition simpleCond = (IlrSimpleCondition) cond;
				verbObject = simpleCond.getObjectBinding().getName();
			} else {
				Logger.getGlobal().warning("Condition type not handled for field value");
			}
		} else {
			verbObject = (String) val.getObject().exploreValue(this);
		}

		verbObject = verbalizer.getAliasedVariable(verbObject);
		String key = val.getClassName() + "." + val.getName();

		String result = verbalizer.verbalizeMember(Verbalizer.MemberRole.navigation, key, verbObject, null);

		return result;
	}

	/**
	 * >> IlrCastValue
	 */
	public Object exploreValue(IlrCastValue val) {
		String arg = (String) val.getValue().exploreValue(this);
		return arg;
	}

	/**
	 * >> IlrVariable
	 */
	public Object exploreValue(IlrVariable val) {
		return val.getName();
	}

	/**
	 * >> IlrBinaryValue
	 */
	public Object exploreValue(IlrBinaryValue value) {
		String arg1 = (String) value.getFirstArgument().exploreValue(this);
		String arg2 = (String) value.getSecondArgument().exploreValue(this);
		String op;
		switch (value.getKind()) {
		case IlrBinaryOperator.ADD:
			op = "+";
			break;
		case IlrBinaryOperator.DIVIDE:
			op = "/";
			break;
		case IlrBinaryOperator.SUBTRACT:
			op = "-";
			break;
		case IlrBinaryOperator.MULTIPLY:
			op = "*";
			break;
		default:
			Logger.getGlobal().warning("Unhandled binary operator: " + value.getKind());
			op = "?";
		}
		return arg1 + " " + op + " " + arg2;
	}

	/**
	 * >> IlrUnaryValue
	 */
	public Object exploreValue(IlrUnaryValue value) {
		System.out.println(value.getArgument().getClass());
		String arg = (String) value.getArgument().exploreValue(this);
		String op;
		switch (value.getKind()) {
		case IlrUnaryOperator.NEGATIVE:
			op = "-";
			break;
		case IlrUnaryOperator.NOT:
			op = "it is not true that";
			break;
		default:
			Logger.getGlobal().warning("Unhandled unary operator: " + value.getKind());
			op = "?";
		}
		return op + " " + arg;
	}

	/**
	 * >> IlrConstantValue
	 */
	public Object exploreValue(IlrConstantValue value) {
		String type = value.getXOMType().getRawName();
		if (type.equals("String")) {
			return "\"" + value.getValue() + "\"";
		} else {
			return value.getValue().toString();
		}
	}

	/**
	 * IlrClassTypeValue
	 */
	public Object exploreValue(IlrClassTypeValue value) {
		return null;
	}

	/**
	 * IlrAsValue
	 */
	public Object exploreValue(IlrAsValue value) {
		return null;
	}

	/**
	 * IlrContextValue
	 */
	public Object exploreValue(IlrContextValue value) {
		return null;
	}

	/**
	 * IlrInstanceValue
	 */
	public Object exploreValue(IlrInstanceValue value) {
		return null;
	}

	/**
	 * IlrScopeValue
	 */
	public Object exploreValue(IlrScopeValue value) {
		return null;
	}

	/**
	 * IlrObjectValue
	 */
	public Object exploreValue(IlrObjectValue value) {
		return null;
	}

	/**
	 * IlrArrayLength
	 */
	public Object exploreValue(IlrArrayLength value) {
		verbalizer.setMultivalued(true);
		String arrayValue = (String)value.getArray().exploreValue(this);
		verbalizer.setMultivalued(false);
		return "the number of elements in " + arrayValue;
	}

	/**
	 * IlrArrayElement
	 */
	public Object exploreValue(IlrArrayElement value) {
		return null;
	}

	/**
	 * IlrFunctionInvocation
	 */
	public Object exploreValue(IlrFunctionInvocation value) {
		for (IlrValue argValue : value.getArguments()) {
			argValue.exploreValue(this);
		}
		return null;
	}

	/**
	 * IlrNewInstanceValue TODO Include the cast class as used.
	 */
	public Object exploreValue(IlrNewInstanceValue value) {
		return null;
	}

	/**
	 * IlrNewArrayInstanceValue
	 */
	public Object exploreValue(IlrNewArrayInstanceValue value) {
		return null;
	}

	/**
	 * IlrIntervalValue
	 */
	public Object exploreValue(IlrIntervalValue value) {
		value.getLeftValue().exploreValue(this);
		value.getRightValue().exploreValue(this);
		return null;
	}

	/**
	 * IlrComponentPropertyValue
	 */
	public Object exploreValue(IlrComponentPropertyValue value) {
		return null;
	}

	/**
	 * IlrIndexedComponentPropertyValue
	 */
	public Object exploreValue(IlrIndexedComponentPropertyValue value) {
		return null;
	}

	/**
	 * IlrCollectInSourceValue
	 */
	public Object exploreValue(IlrCollectInSourceValue value) {
		return null;
	}

	/**
	 * IlrPropertyAccessValue
	 */
	public Object exploreValue(IlrPropertyAccessValue value) {
		return null;
	}

	/**
	 * >> IlrFieldValue
	 */
	public Object exploreAssignable(IlrFieldValue val) {
		String result = (String)val.exploreValue(this);
		return result;
	}

	/**
	 * IlrVariable
	 */
	public Object exploreAssignable(IlrVariable var) {
		var.exploreValue(this);
		return null;
	}

	/**
	 * IlrArrayElement
	 */
	public Object exploreAssignable(IlrArrayElement array) {
		array.exploreValue(this);
		return null;
	}

	/**
	 * IlrComponentPropertyValue
	 */
	public Object exploreAssignable(IlrComponentPropertyValue value) {
		value.exploreValue(this);
		return null;
	}

	/**
	 * IlrIndexedComponentPropertyValue
	 */
	public Object exploreAssignable(IlrIndexedComponentPropertyValue value) {
		value.exploreValue(this);
		return null;
	}

	/**
	 * IlrStaticFieldValue
	 */
	public Object exploreAssignable(IlrStaticFieldValue field) {
		field.exploreValue(this);
		return null;
	}

	/**
	 * IlrNaryTest
	 */
	public Object exploreTest(IlrNaryTest test) {
		IlrTest[] tests = test.getTests();
		for (IlrTest entry : tests) {
			entry.exploreTest(this);
		}
		return null;
	}

	/**
	 * >> IlrUnaryTest
	 */
	public Object exploreTest(IlrUnaryTest test) {
		IlrValue value = test.getArgument();
		String result = (String) value.exploreValue(this);
		return result;
	}

	/**
	 * >> IlrBinaryTest
	 */
	public Object exploreTest(IlrBinaryTest test) {
		String a1 = (String) test.getFirstArgument().exploreValue(this);
		String a2 = (String) test.getSecondArgument().exploreValue(this);
		String op;
		switch (test.getKind()) {
		case IlrBinaryTester.EQUAL:
			op = "is";
			break;
		case IlrBinaryTester.GREATER_OR_EQUAL:
			op = "is at least";
			break;
		case IlrBinaryTester.GREATER_THAN:
			op = "is more than";
			break;
		case IlrBinaryTester.IN:
			op = "is one of";
			break;
		case IlrBinaryTester.LESS_OR_EQUAL:
			op = "is at most";
			break;
		case IlrBinaryTester.LESS_THAN:
			op = "is less than";
			break;
		case IlrBinaryTester.NOT_EQUAL:
			op = "is not";
			break;
		case IlrBinaryTester.NOT_IN:
			op = "is not one of";
			break;
		default:
			Logger.getGlobal().severe("Unhandled binary test: " + test.getKind());
			op = "?";
		}
		return a1 + " " + op + " " + a2;
	}

	/**
	 * IlrInstanceOfTest
	 */
	public Object exploreTest(IlrInstanceOfTest iot) {
		iot.getValue().exploreValue(this);
		return null;
	}

	/**
	 * IlrUnknownTest
	 */
	public Object exploreTest(IlrUnknownTest test) {
		return null;
	}

	/**
	 * >> IlrNotTest
	 */
	public Object exploreTest(IlrNotTest test) {
		String testString = (String) test.getArgument().exploreTest(this);
		String result = "it is not true that " + testString;
		return result;
	}

	/**
	 * IlrThrowStatement
	 */
	public Object exploreStatement(IlrThrowStatement throwStatement) {
		throwStatement.getValue().exploreValue(this);
		return null;
	}

	/**
	 * IlrBreakStatement
	 */
	public Object exploreStatement(IlrBreakStatement arg0) {
		return null;
	}

	/**
	 * IlrContinueStatement
	 */
	public Object exploreStatement(IlrContinueStatement arg0) {
		return null;
	}

	/**
	 * IlrUnaryValue
	 */
	public Object exploreStatement(IlrUnaryValue value) {
		value.exploreValue(this);
		return null;
	}

	/**
	 * IlrAssertAction
	 */
	public Object exploreStatement(IlrAssertAction action) {
		action.getObject().exploreValue(this);
		return null;
	}

	/**
	 * IlrRetractAction
	 */
	public Object exploreStatement(IlrRetractAction action) {
		action.getObject().exploreValue(this);
		return null;
	}

	/**
	 * IlrModifyAction
	 */
	public Object exploreStatement(IlrModifyAction action) {
		action.getObject().exploreValue(this);
		return null;
	}

	/**
	 * IlrUpdateAction
	 */
	public Object exploreStatement(IlrUpdateAction action) {
		action.getObject().exploreValue(this);
		return null;
	}

	/**
	 * >> IlrStaticMethodInvocation
	 */
	public Object exploreStatement(IlrStaticMethodInvocation method) {
		String result = (String) exploreValue(method);
		return result;
	}

	/**
	 * >> IlrMethodInvocation
	 */
	public Object exploreStatement(IlrMethodInvocation method) {
		String result = (String) exploreValue(method) + ";";
		return result;
	}

	/**
	 * >> IlrAssignment
	 */
	public Object exploreStatement(IlrAssignment node) {
		IlrAssignable assign = node.getAssignable();
		String assigned = (String) assign.exploreAssignable(this);
		String value = (String) node.getValue().exploreValue(this);
		return "set " + assigned + " to " + value + ";";
	}

	/**
	 * >> IlrReturnStatement
	 * Used in functions only
	 */
	public Object exploreStatement(IlrReturnStatement stmt) {
		stmt.getValue().exploreValue(this);
		return null;
	}

	/**
	 * IlrBindStatement
	 */
	public Object exploreStatement(IlrBindStatement bind) {
		bind.getValue().exploreValue(this);
		return null;
	}

	/**
	 * IlrFunctionInvocation
	 */
	public Object exploreStatement(IlrFunctionInvocation fun) {
		return null;
	}

	/**
	 * IlrForStatement
	 */
	public Object exploreStatement(IlrForStatement forBlock) {
		return exploreStatementBlock(forBlock);
	}

	/**
	 * IlrWhileStatement
	 */
	public Object exploreStatement(IlrWhileStatement whileBlock) {
		return exploreStatementBlock(whileBlock);
	}

	/**
	 * IlrForeachStatement
	 */
	public Object exploreStatement(IlrForeachStatement forEachBlock) {
		return exploreStatementBlock(forEachBlock);
	}

	/**
	 * IlrIfStatement
	 */
	public Object exploreStatement(IlrIfStatement ifBlock) {
		ElseBlock elseBlock = ifBlock.getElseBlock();
		if (elseBlock != null) {
			exploreStatementBlock(elseBlock);
		}

		exploreStatementBlock(ifBlock);
		return null;
	}

	/**
	 * IlrTryCatchFinallyStatement
	 */
	@SuppressWarnings("rawtypes")
	public Object exploreStatement(IlrTryCatchFinallyStatement tryBlock) {
		exploreStatementBlock(tryBlock);

		Vector catchBlocks = tryBlock.getCatchBlocks();
		if (catchBlocks != null) {
			Iterator iter = catchBlocks.iterator();
			while (iter.hasNext()) {
				IlrTryCatchFinallyStatement.CatchBlock catchBlock = (IlrTryCatchFinallyStatement.CatchBlock) iter
						.next();
				exploreStatementBlock(catchBlock);
			}
		}

		FinallyBlock finallyBlock = tryBlock.getFinallyBlock();
		if (finallyBlock != null) {
			exploreStatementBlock(finallyBlock);
		}

		return null;
	}

	private Object exploreStatementBlock(IlrStatementBlock forBlock) {
		IlrStatement[] statements = forBlock.getStatements();
		if (statements != null) {
			for (IlrStatement statement : statements) {
				statement.exploreStatement(this);
			}
		}
		return null;
	}
}
