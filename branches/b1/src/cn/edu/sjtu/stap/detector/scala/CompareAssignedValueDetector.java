package cn.edu.sjtu.stap.detector.scala;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class CompareAssignedValueDetector extends OpcodeStackDetector {

	private static final String SCALA_UNIT_SIG="Lscala/runtime/BoxedUnit;";
	
	private static final String	COMP_ASSIGNED_VALUE="COMP_ASSIGNED_VALUE";
	
	private BugReporter bugReporter;
	
	public CompareAssignedValueDetector(BugReporter bugReporter){
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void sawOpcode(int seen) {
		//检测使用复制后的值与null相比
		if (seen == IFNULL || seen == IFNONNULL && stack.getStackDepth()==1) {
			if (stack.getStackItem(0).getSignature().equals(SCALA_UNIT_SIG)) {
				bugReporter.reportBug(new BugInstance(COMP_ASSIGNED_VALUE, Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
				        .addSourceLine(this, getPC()));
			}
		}
		
		//检测使用复制后的值与其他类型的值相比
		if(seen==INVOKEVIRTUAL && getClassConstantOperand().equals("java/lang/Object") && getNameConstantOperand().equals("equals") && stack.getStackDepth()==2){
			Item item0 = stack.getStackItem(0);
			Item item1 = stack.getStackItem(1);
			if(item0.getSignature().equals(SCALA_UNIT_SIG) || item1.getSignature().equals(SCALA_UNIT_SIG)){
				bugReporter.reportBug(new BugInstance(COMP_ASSIGNED_VALUE, Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
				        .addSourceLine(this, getPC()));
			}
		}
	}

}
