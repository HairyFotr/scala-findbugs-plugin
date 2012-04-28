package cn.edu.sjtu.se.puyuDuan;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class StrAndCharMethodConfusing extends OpcodeStackDetector {
	BugReporter bugReporter;
	
	public StrAndCharMethodConfusing(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	

	@Override
	public void visit(Code obj){
	super.visit(obj);
	}
	@Override
	public void sawOpcode(int seen) {
		// TODO Auto-generated method stub
		
		if(seen==INVOKEVIRTUAL&&this.getDottedClassConstantOperand().equals("scala.collection.mutable.StringBuilder")){
			String s = this.getMethodDescriptorOperand().getSignature().substring(1,2);
			if(s.equals("I")){
				System.out.println(this.getPC());
				bugReporter.reportBug(new BugInstance("METHOD_CONFUSING", NORMAL_PRIORITY).addClassAndMethod(this)
				        .addSourceLine(this, getPC()).addInt(getPC()));
			}			
		}
		
	}

}
