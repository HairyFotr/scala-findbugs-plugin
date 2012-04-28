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

package cn.edu.sjtu.se.shunpingWang;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author shuang79
 */
public class ScalaFindPuzzlers extends OpcodeStackDetector{

    final BugAccumulator bugAccumulator;
    final BugReporter bugReporter;
    public ScalaFindPuzzlers(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
        this.bugAccumulator = new BugAccumulator(bugReporter);
    }
    
    @Override
    public void visit(Code obj)
    {
        super.visit(obj);
        bugAccumulator.reportAccumulatedBugs();
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
     */
    @Override
    public void sawOpcode(int seen) {
        if (seen == INEG) {
            OpcodeStack.Item top = stack.getStackItem(0);
            XMethod m = top.getReturnValueOf();
            if (m != null) {
                if (m.getName().equals("compareTo") || m.getName().equals("compare"))
                    bugAccumulator.accumulateBug(new BugInstance(this, "SCALA_RV_NEGATING_RESULT_OF_COMPARETO", NORMAL_PRIORITY)
                    .addClassAndMethod(this)
                    .addCalledMethod(m).addValueSource(top, this), this);
            }
        }
        if (seen == IRETURN && (getMethod().getName().equals("compareTo") || getMethod().getName().equals("compare"))) {
            OpcodeStack.Item top = stack.getStackItem(0);
            Object o = top.getConstant();
            if (o instanceof Integer && ((Integer)o).intValue() == Integer.MIN_VALUE)
                    bugAccumulator.accumulateBug(new BugInstance(this, "SCALA_CO_COMPARETO_RESULTS_MIN_VALUE", NORMAL_PRIORITY)
                    .addClassAndMethod(this).addValueSource(top, this), this);

        }
    }

}
