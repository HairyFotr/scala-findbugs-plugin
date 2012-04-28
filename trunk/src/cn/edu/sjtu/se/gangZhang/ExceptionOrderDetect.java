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

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;


import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;

import edu.umd.cs.findbugs.ba.Hierarchy;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author infear
 */
public class ExceptionOrderDetect extends OpcodeStackDetector {

	final static String bugInfo = "EXCEPTION_ORDER_PROBLEM";

	BugReporter bugReporter;

	ArrayList<CodeException[]> list = new ArrayList<CodeException[]>();

	public ExceptionOrderDetect(BugReporter reporter) {
		bugReporter = reporter;
	}

	/**
	 * visit��������ÿ�ν����ֽ��뷽����ʱ����� ��ÿ�ν����·�����ʱ����ձ�־λ
	 */
	@Override
	public void visit(Code obj) {
		super.visit(obj);
	}

	// @Override
	// public void visitClassContext(ClassContext classContext) {
	// JavaClass javaClass = classContext.getJavaClass();
	// Method[] methodList = javaClass.getMethods();
	// for(Method method:methodList)
	// {
	// ExceptionTable table=method.getExceptionTable();
	// CodeException ex1, ex2;
	// String class1, class2;
	// for (int i = 0; i < table.getLength(); i++) {
	// for (int t = i + 1; t < table.getLength(); t++) {
	// class1 =
	// getCode().getConstantPool().getConstantString(table.getExceptionIndexTable()[i],
	// CONSTANT_Class);
	// class2 =
	// getCode().getConstantPool().getConstantString(table.getExceptionIndexTable()[t],
	// CONSTANT_Class);
	// try {
	// if (Hierarchy.isSubtype(class1, class2)) {
	// BugInstance bug = new BugInstance(this, bugInfo,
	// NORMAL_PRIORITY).addClassAndMethod(this)
	// .addSourceLine(this, getPC());
	// bug.addString(class1 + " is covered by " + class2 + " for Hierarchy");
	// bugReporter.logError(class1 + " is covered by " + class2 +
	// " for Hierarchy");
	// //bugReporter.reportBug(bug);
	// }
	// } catch (ClassFormatException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
	 */
	@Override
	public void sawOpcode(int seen) {
		// TODO Auto-generated method stub
		CodeException[] table = getCode().getExceptionTable();
		if (table == null || table.length == 0)
			return;
		if (atCatchBlock() && !list.contains(table)) {
			list.add(table);
			CodeException ex1, ex2;
			String class1, class2;
			for (int i = 0; i < table.length; i++) {
				for (int t = i + 1; t < table.length; t++) {
					ex1 = table[i];
					ex2 = table[t];
					if (ex1.getCatchType() != 0 && ex2.getCatchType() != 0) {
						class1 = getConstantPool().getConstantString(ex1.getCatchType(), CONSTANT_Class);
						class2 = getConstantPool().getConstantString(ex2.getCatchType(), CONSTANT_Class);
						try {
							if (Hierarchy.isSubtype(class2, class1) && ex1.getStartPC() == ex2.getStartPC()
							        && ex1.getEndPC() == ex2.getEndPC()) {
								BugInstance bug = new BugInstance(this, bugInfo, NORMAL_PRIORITY).addClassAndMethod(this)
								        .addSourceLine(this, ex2.getEndPC());
								int handle_source = getCode().getLineNumberTable().getSourceLine(ex2.getHandlerPC() + 1);
								bug.addString(class2 + " handled in line " + handle_source + " is covered by " + class1
								        + " \nfor class Hierarchy, so it is unreachable");
								bugReporter.reportBug(bug);
							}
						} catch (ClassFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		}
	}

}
