package cn.edu.sjtu.se.renjieQiu;

/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003-2005 University of Maryland
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


import org.apache.bcel.classfile.Code;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import java.util.HashMap;

public class FindReceiveInLoop extends OpcodeStackDetector {

    private BugReporter bugReporter;
    private HashMap<String, MethodDescriptor> record = new HashMap<String, MethodDescriptor>();

    public FindReceiveInLoop(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {
        if (seen == INVOKEINTERFACE &&
                "scala/actors/Combinators".equals(getClassConstantOperand()) &&
                "loop".equals(getNameConstantOperand())) {
            checkLooper(stack.getStackItem(0).getReturnValueOf().getClassName());
        }
        if (seen == INVOKEINTERFACE &&
                "scala/actors/Actor".equals(getClassConstantOperand()) &&
                "receive".equals(getNameConstantOperand())) {
            checkReceiver(getClassName());
        }
    }

    private void checkLooper(String sig) {
        if (record.containsKey(sig)) {
            bugReporter.reportBug(
                    new BugInstance("SCALA_RECEIVE_IN_LOOP", HIGH_PRIORITY)
                        .addClassAndMethod(getMethodDescriptor()));
        }
        record.put(sig, getMethodDescriptor());
    }

    private void checkReceiver(String sig) {
        if (record.containsKey(sig)) {
            bugReporter.reportBug(
                    new BugInstance("SCALA_RECEIVE_IN_LOOP", HIGH_PRIORITY)
                        .addClassAndMethod(record.get(sig)));
        } else {
            record.put(sig, null);
        }
    }

}
