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

package cn.edu.sjtu.se.mengpingLi;

import java.util.List;
import java.util.ArrayList;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.SignatureParser;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author WUTONG
 */
public class MissHashCodeDetector extends OpcodeStackDetector {
	BugReporter bugReporter;
	boolean hasEqual = false, hasHash = false;

	public MissHashCodeDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void visit(Code code) {
		super.visit(code);		
	}
	
	@Override
	public void visitMethod(Method obj){
		String name = this.getMethodName();
		String s = getMethodSig();
		SignatureParser sp = new SignatureParser(s);
		
		if ("equals".equals(name) && sp.getNumParameters() == 1 && ("Ljava/lang/Object;").equals(sp.getParameter(0)))
				hasEqual = true;
		if ("hashCode".equals(name) && sp.getNumParameters() == 0)
			hasHash = true;
	}
	
	@Override
	public void visitAfter(JavaClass obj){
		if (hasEqual && (!hasHash)){
			bugReporter.reportBug(new BugInstance(this, "MISS_HASHCODE_METHOD", NORMAL_PRIORITY).addClass(this));
		}
		
		hasEqual = hasHash = false;
	}
	
	@Override
	public void visitJavaClass(JavaClass obj){
		if (!obj.getClassName().contains("$$")) super.visitJavaClass(obj);
	}
	
	@Override
	public void sawOpcode(int seen) {
	}

}
