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

package cn.edu.sjtu.stap.detector.scala;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author wangqi
 */
public class OptionCompNullDetector extends OpcodeStackDetector {

	final BugReporter bugReporter;
	private static final String SCALA_CLASS_OPTION="Lscala/Option;";
	
	public OptionCompNullDetector(BugReporter bugReporter){
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void sawOpcode(int seen) {
		if (seen == IFNULL || seen == IFNONNULL) {
			if (stack.getStackItem(0).getSignature().equals(SCALA_CLASS_OPTION)) {
				bugReporter.reportBug(new BugInstance("COMPARE_OPTION_NULL", NORMAL_PRIORITY).addClassAndMethod(this)
				        .addSourceLine(this, getPC()).addInt(getPC()));
			}
		}
	}

}
