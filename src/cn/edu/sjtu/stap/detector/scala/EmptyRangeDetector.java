package cn.edu.sjtu.stap.detector.scala;

import cn.edu.sjtu.stap.detect.util.OpcodeMap;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class EmptyRangeDetector extends OpcodeStackDetector{

	private BugReporter bugReporter;
	private int firstPC = -1;
	private int left = 0;
	public EmptyRangeDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	
	@Override
	public void sawOpcode(int seen) {
		if(seen==INVOKEVIRTUAL && getNameConstantOperand().equals("intWrapper") &&getDottedClassConstantOperand().equals("scala.Predef$")){
			Item item = stack.getStackItem(0);
			if(item.getSignature().equals("I")){
				firstPC = getPC();
				left = (Integer)item.getConstant();
			}
		}
		
		if(seen==INVOKEVIRTUAL && getNameConstantOperand().equals("to") &&getDottedClassConstantOperand().equals("scala.runtime.RichInt")){
			Item item = stack.getStackItem(0);
			if(item.getSignature().equals("I")){
				int right  = (Integer)item.getConstant();
				if(getPrevOpcode(2)==INVOKEVIRTUAL && right<left)
					bugReporter.reportBug(new BugInstance("EMPTY_RANGE",Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
					        .addSourceLine(this, getPC()));
			}
		}
	
	
	}

}
