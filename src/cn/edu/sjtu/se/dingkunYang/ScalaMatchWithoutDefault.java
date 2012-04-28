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

package cn.edu.sjtu.se.dingkunYang;

import java.util.BitSet;
import java.util.Iterator;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.IndexedInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.LocalVariableAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.SystemProperties;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Dataflow;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.LiveLocalStoreAnalysis;
import edu.umd.cs.findbugs.ba.Location;
import edu.umd.cs.findbugs.props.WarningProperty;
import edu.umd.cs.findbugs.props.WarningPropertySet;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Detector;

/**
 * @author ydkwhitebear@sjtu
 */
public class ScalaMatchWithoutDefault implements Detector  {
	private static final boolean DEBUG = SystemProperties.getBoolean("s_mwd.debug");
	
	private BugReporter bugReporter;

	public ScalaMatchWithoutDefault(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		if (DEBUG)
			System.out.println("Debugging Scala MatchWithoutDefault detector");
	}

	
	public void visitClassContext(ClassContext classContext) {
		JavaClass javaClass = classContext.getJavaClass();

		Method[] methodList = javaClass.getMethods();
			
		for (Method method : methodList) {
			MethodGen methodGen = classContext.getMethodGen(method);
			if (methodGen == null)
				continue;

			if (!prescreen(classContext, method))
				continue;

			try {
				analyzeMethod(classContext, method);
			} catch (DataflowAnalysisException e) {
				bugReporter.logError("Error analyzing " + method.toString(), e);
			} catch (CFGBuilderException e) {
				bugReporter.logError("Error analyzing " + method.toString(), e);
			}
		}
	}
	
	private void analyzeMethod(ClassContext classContext, Method method) throws DataflowAnalysisException, CFGBuilderException {
		if (method.isSynthetic() || (method.getAccessFlags() & Constants.ACC_BRIDGE) == Constants.ACC_BRIDGE) return;
		if (DEBUG) {
		}
		
		JavaClass javaClass = classContext.getJavaClass();
		BugAccumulator accumulator = new BugAccumulator(bugReporter);
		
		String byteCodeString = method.getCode().toString();
		String tempByteCodeString = byteCodeString;
		int lineNumberOfByteCode  = 0;
		while(tempByteCodeString.indexOf(":") != -1) {
			lineNumberOfByteCode++;
			int byteCodeStringLength = tempByteCodeString.length();
			tempByteCodeString = tempByteCodeString.substring(tempByteCodeString.indexOf(":") + 1, byteCodeStringLength - 1);
		}
		String[] ByteCodes = new String[lineNumberOfByteCode];
		int firstIndex = 0;
		int nextIndex = 0;
		String currentSubString = byteCodeString;
		String nextSubString = null;
		int currentStringLength = currentSubString.length();
		boolean matchWithoutCatchSuspected[] = new boolean[lineNumberOfByteCode];
		boolean hasSuspected = false;
		for(int ii = 0; ii < lineNumberOfByteCode - 1; ii++) {
			matchWithoutCatchSuspected[ii] = false;
			firstIndex = currentSubString.indexOf(":");
			nextSubString = currentSubString.substring(firstIndex + 1, currentStringLength-1);
			nextIndex = firstIndex + 1 + nextSubString.indexOf(":");
			ByteCodes[ii] = currentSubString.substring(firstIndex + 1, nextIndex).trim();
			if(ByteCodes[ii].toLowerCase().contains("matcherror") || 
			   ((ByteCodes[ii].toLowerCase().contains("error")) && ByteCodes[ii].toLowerCase().contains("match"))) {
				matchWithoutCatchSuspected[ii] = true;
				hasSuspected = true;
			}
			currentSubString = nextSubString;	
			currentStringLength = currentSubString.length();
		}
		
		BugInstance pendingBugReportScalaMatchWithoutDefault = null;
		try {
			if (hasSuspected) {
				//int jj = 0;
				//while(!matchWithoutCatchSuspected[jj]) {
					//jj++;
				//}
				int priority = NORMAL_PRIORITY;
				pendingBugReportScalaMatchWithoutDefault = new BugInstance("SCALA_MATCH_WITHOUT_DEFAULT", priority);
				bugReporter.reportBug(pendingBugReportScalaMatchWithoutDefault.addClassAndMethod(javaClass, method));					
			}
		} finally {
			if (pendingBugReportScalaMatchWithoutDefault != null)
				bugReporter.reportBug(pendingBugReportScalaMatchWithoutDefault);
		}
	    
	}
	
	private boolean prescreen(ClassContext classContext, Method method) {
		return true;
	}
	public void report() {
	}
}
