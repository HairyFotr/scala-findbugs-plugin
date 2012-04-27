package cn.edu.sjtu.stap.detector.scala;

import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class MultiAssignWithPatternDetector extends OpcodeStackDetector {

	private BugReporter bugReporter;

	private int state;

	public MultiAssignWithPatternDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	public void visit(Method method) {
		super.visit(method);
		state = 0;
	}

	@Override
	public void sawOpcode(int seen) {
		
		if (seen == INSTANCEOF
				&& getClassConstantOperand().equals(
						"scala/collection/immutable/List"))
			state = 1;

		if (seen == INSTANCEOF
				&& state == 1
				&& getNextOpcode() == IFEQ
				&& getClassConstantOperand().equals(
						"scala/collection/immutable/$colon$colon"))
			state = 2;

		if (seen == CHECKCAST
				&& state == 2
				&& getClassConstantOperand().equals(
						"scala/collection/immutable/$colon$colon"))
			state = 3;

		if (seen == NEW && state == 3
				&& getClassConstantOperand().startsWith("scala/Tuple"))
			state = 4;

		if (seen == INVOKESPECIAL && state == 4
				&& getClassConstantOperand().startsWith("scala/Tuple"))
			state = 5;

		if (seen == CHECKCAST
				&& state == 5
				&& getClassConstantOperand().equals(
						"scala/collection/immutable/List"))
			state = 6;

		if (seen == NEW && state == 6
				&& getClassConstantOperand().equals("scala/MatchError")) {
			bugReporter.reportBug(new BugInstance("MUTIL_ASSIGN_VIA_PATTERN",
					Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
					.addSourceLine(this, getPC()));
			state = 0;
		}

	}

}
