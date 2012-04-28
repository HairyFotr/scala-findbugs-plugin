/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package cn.edu.sjtu.se.gangZhang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Signature;



import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

import edu.umd.cs.findbugs.OpcodeStack.Item;

import edu.umd.cs.findbugs.ba.generic.GenericSignatureParser;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;


/**
 * @author infear
 */
public class MethodUseGenericDetect extends OpcodeStackDetector {

	final static String bugInfo = "GENERIC_MISUSED";

	BugReporter reporter;

	// method list declared in the source
	List<String> methodList = new ArrayList<String>();
	   
	// params part declared in the signature of method
	ArrayList<String> paramList = new ArrayList<String>();
	
	// generic part declared in the signature of method
	private List<String> genericPartList = new ArrayList<String>();

	public MethodUseGenericDetect(BugReporter re) {
		reporter = re;
	}

	@Override
	public void visit(Code obj) {
		super.visit(obj);
	}

	@Override
	public void report() {

		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
	 */
	@Override
	public void sawOpcode(int seen) {
		// TODO Auto-generated method stub
		/* 	identical method in different class will have same hashcode,for the 
			implementation of the signature.hashcode^name.hashcode.So use both class
		 	and method descriptor to distinguish them*/
		if (visitingMethod()) { 
			if (!methodList.contains(getClassDescriptor() + ":" + getMethodDescriptor())) {
				genericPartList.clear();
				paramList.clear();
				methodList.add(getClassDescriptor() + ":" + getMethodDescriptor());
				Attribute attributes[] = getMethod().getAttributes();
				for (Attribute attr : attributes) {
					if (attr instanceof Signature) {
						String methodSignature = ((Signature) attr).getSignature();
						if (Signature.isFormalParameterList(methodSignature)) {
							getParamAndGenericList(getMethod(), methodSignature);
						}
					}
				}
//				JavaClass info=getThisClass();
//				for(Attribute attr:info.getAttributes())
//					if(attr instanceof Signature)
//					{
//						String classSignature=((Signature)attr).getSignature();
//						if (Signature.isFormalParameterList(classSignature)) {
//							getParamAndGenericList(getMethod(), classSignature);
//						}
//					}
			}
			if (seen == INSTANCEOF) {// seen instanceoOf
				/*get the top of the stack in the method*/
				Item paramOrder = stack.getStackItem(0);
				if (paramOrder.getRegisterNumber() < paramList.size()) {
					for (String generic : genericPartList) {
						checkUsingGeneric(generic, paramOrder);
					}
				}
			}
		} else {
		}
	}

	public void checkUsingGeneric(String generic, Item paramOrder) {
		String param = null;
		try {
			System.out.println(paramOrder.toString()+" "+paramOrder.getRegisterNumber());
			if(paramOrder.getRegisterNumber()>=0)
			{
			param = paramList.get(paramOrder.getRegisterNumber());
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		if(param!=null)
		{
		String genericPart = getGenericPart(param, new String());
		System.out.println(generic+" "+param+" "+genericPart);
		if (genericPart != null) {
			// check for each param use generic type and instanceOf
			if (genericPart.contains("T" + generic)) {
				BugInstance bug = new BugInstance(this, bugInfo, NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this,
				        getPC());
				bug.addInt(getPC());
				bug.addClass(getClassDescriptor());

				LocalVariable var = getCode().getLocalVariableTable().getLocalVariableTable()[paramOrder.getRegisterNumber()];
				bug.addString(param + " " + var.getName() + " use generic type " + generic);
				reporter.reportBug(bug);
			}
		}
		}
	}
	
	// get the generic part of one param contains generic info
	public String getGenericPart(String param, String result) {
		result = param;
		int length = param.length();
		int begin = 0, end;
		while (begin < length && param.charAt(begin) != '<')
			begin++;
		if (begin == length)
			return param;//for some param not involved generic type container class 
						 //which will be compiled to "T"+generic Type
		else {
			end = begin;
			while (end < length && (param.charAt(end)) != '>') {
				end++;
			}
			if (end == length)
				return null;
			else {
				result = param.substring(begin, end + 1);
				return result;
			}
		}

	}
	
	// divide method signature to three parts and get paramList and genericList
	public void getParamAndGenericList(Method method, String methodSignature) {
		int begin = methodSignature.indexOf('(');
		int end = methodSignature.indexOf(')');
		if (begin > 0) {
			String generic = methodSignature.substring(0, begin);// declared generic
			                                             // would be used
			String[] genericType = generic.substring(1, generic.length() - 1).split(";");
			for (int i = 0; i < genericType.length; i++)
				genericPartList.add(genericType[i].split(":")[0]);
			//System.out.println(method.toString());
		}
		if (begin < end) {
			String params = methodSignature.substring(begin, end + 1);
			GenericSignatureParser parser = new GenericSignatureParser(params);
			if (!method.isStatic())
				paramList.add("this");
			for (Iterator<String> i = parser.parameterSignatureIterator(); i.hasNext();) {
				String s = i.next();
				paramList.add(s);
			}
			return;
		}
		if (end < methodSignature.length() - 1) {
			//String returnType= methodSignature.substring(end + 1, methodSignature.length());
		}
	}

	/*last version:method without using genericParser*/
	
	/*second step:get each param divided by ";" except for conditions like
	 * ";BBL" ,which will be dealed with in the last step
	 */
	public void getParamStep1(Method method, String content, ArrayList<String> list) {
		ArrayList<String> result = new ArrayList<String>();
		int length = content.length();
		boolean tag = false;
		int begin = 0, end = begin;
		while (end < length) {
			char ch = content.charAt(end);
			if (ch == '<')
				tag = true;
			if (ch == ';' && !tag) {
				result.add(content.substring(begin, end));
				begin = ++end;
				continue;
			}
			if (ch == '>')
				tag = false;
			end++;
		}
		getParamStep2(method, result, list);

	}

	// last step:get params list contains the generic info(dealed with value
	// type params)
	public void getParamStep2(Method method, ArrayList<String> list, ArrayList<String> paramsList) {
		if (!method.isStatic())
			paramsList.add("this");
		for (String str : list) {
			// System.out.println("getStep2:"+str);
			int pos = 0;
			int length = str.length();
			while (pos < length) {
				if (str.charAt(pos) == 'L') {
					paramsList.add(str.substring(pos, length));
					break;
				} else
					paramsList.add(str.substring(pos, pos + 1));
				pos++;
			}
		}
	}

}
