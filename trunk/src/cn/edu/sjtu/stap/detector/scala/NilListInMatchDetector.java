package cn.edu.sjtu.stap.detector.scala;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class NilListInMatchDetector extends OpcodeStackDetector{

	private static final String SCALA_NIL_SIG = "Lscala/collection/immutable/Nil$;";
	private static final String SCALA_LIST = "scala/collection/immutable/List";
	
	private BugReporter bugReporter;
	
	private int cmpNilPC = 0;
	private int cmpNilFailPC = 0;
	private String srcObjSig="";
	
	public NilListInMatchDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void sawOpcode(int seen) {
		if(seen==INVOKEVIRTUAL && getNextOpcode()==IFEQ &&stack.getStackDepth()==2 && stack.getStackItem(1).getSignature().equals(SCALA_NIL_SIG)){
			srcObjSig = stack.getStackItem(0).getSignature();
			cmpNilPC = getPC();
		}
		if(seen==INSTANCEOF && getNextOpcode()==IFEQ && getClassConstantOperand().equals(SCALA_LIST) ){
			if(stack.getStackItem(0).getSignature().equals(srcObjSig) && getPC()<cmpNilFailPC+5){
				bugReporter.reportBug(new BugInstance("NIL_LIST_IN_MATCH",Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
				        .addSourceLine(this, getPC()));
			}
		}
		if(seen==IFEQ && getPC()<cmpNilPC+5){
			cmpNilFailPC = getBranchTarget();
		}
	}
	
	private void reset(){
		
	}


}
