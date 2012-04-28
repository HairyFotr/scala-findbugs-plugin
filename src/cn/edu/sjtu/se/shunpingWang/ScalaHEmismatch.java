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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author shuang79
 */
public class ScalaHEmismatch extends OpcodeStackDetector {

    private BugReporter bugReporter;

    private boolean hasCompareToObject;

    private boolean hasCompareToSelf;

    private boolean usesDefaultEquals;

    private boolean hasCompareToNoIntReturn;

    MethodAnnotation compareToObjectMethod = null;

    MethodAnnotation compareToSelfMethod = null;

    List<MethodAnnotation> compareToNoIntReturnList = null;

    public ScalaHEmismatch(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(JavaClass obj) {
        hasCompareToObject = false;
        hasCompareToSelf = false;
        usesDefaultEquals = true;
        hasCompareToNoIntReturn = false;
        compareToNoIntReturnList = new ArrayList<MethodAnnotation>();
    }

    @Override
    public void visit(Method obj) {
        String name = obj.getName();
        String sig = obj.getSignature();
        if (name.equals("compareTo")) {
            MethodAnnotation tmp = MethodAnnotation.fromVisitedMethod(this);
            if (sig.equals("(Ljava/lang/Object;)I")) {
                this.hasCompareToObject = true;
                this.compareToObjectMethod = tmp;
            } else if (sig.equals("(L" + getClassName() + ";)I")) {
                this.hasCompareToSelf = true;
                this.compareToSelfMethod = tmp;
            } else {
                hasCompareToNoIntReturn = true;
                compareToNoIntReturnList.add(tmp);
            }
        } else if (name.equals("equals")) {
            if (sig.equals("(Ljava/lang/Object;)Z")) {
                usesDefaultEquals = false;
            }
        }
    }

    @Override
    public void visitAfter(JavaClass obj) {
        if (hasCompareToObject && usesDefaultEquals) {
            BugInstance bug = new BugInstance(this, "SCALA_EQ_COMPARETO_USE_OBJECT_EQUALS", NORMAL_PRIORITY).addClass(this)
                    .addMethod(compareToObjectMethod);
            bugReporter.reportBug(bug);
        }
        if (hasCompareToSelf && usesDefaultEquals) {
            BugInstance bug = new BugInstance(this, "SCALA_EQ_COMPARETO_USE_OBJECT_EQUALS", NORMAL_PRIORITY).addClass(this)
                    .addMethod(compareToSelfMethod);
            bugReporter.reportBug(bug);
        }
        if (hasCompareToNoIntReturn) {
            for (int i = 0; i < compareToNoIntReturnList.size(); i++) {
                BugInstance bug = new BugInstance(this, "SCALA_CO_COMPARETO_NON_INT_RETURN", NORMAL_PRIORITY).addClass(this)
                        .addMethod(compareToNoIntReturnList.get(i));
                bugReporter.reportBug(bug);

            }
        }
    }

    public static int opcode(byte code[], int offset) {
        return code[offset] & 0xff;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
     */
    @Override
    public void sawOpcode(int seen) {
        // TODO Auto-generated method stub
    }

}
