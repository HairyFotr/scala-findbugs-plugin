package cn.edu.sjtu.se.jieliangDong;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class FindReturnDetector extends OpcodeStackDetector {
	BugAccumulator bugAccumulator;
	String lastSig;
	
	public FindReturnDetector(BugReporter bugReporter) {
	this.bugAccumulator = new BugAccumulator(bugReporter);
	this.lastSig  = null;
	}
	
	

	@Override
	public void visit(Code obj){
	super.visit(obj);
	bugAccumulator.reportAccumulatedBugs();
	}

	
	@Override
	
	public void sawOpcode(int seen) {
		if(seen==GETSTATIC&&this.getDottedClassConstantOperand().equals("scala.runtime.BoxedUnit")
				&&this.lastSig.equals("V")){
			//System.out.println(this.getDottedClassConstantOperand().equals("scala.runtime.BoxedUnit.UNIT"));
			bugAccumulator.accumulateBug(new BugInstance(this,
                    "RETURN_MISSING", NORMAL_PRIORITY).addClassAndMethod(this), this);
		}
		
		if(seen==INVOKEVIRTUAL||seen==INVOKESPECIAL||seen==INVOKESTATIC){
			lastSig = this.getMethodSig();			
			lastSig = lastSig.substring(lastSig.indexOf(')')+1);
			System.out.println(lastSig);
		}else{
			lastSig = null;
		}
	}
}
