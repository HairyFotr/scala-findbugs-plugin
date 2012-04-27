package cn.edu.sjtu.stap.detector.scala;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class TypeEraseDetector extends OpcodeStackDetector{

	private BugReporter bugReporter;

	
	public TypeEraseDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void sawOpcode(int seen) {
		if(seen==INSTANCEOF && isIntrestedType(getDottedClassConstantOperand())){
			bugReporter.reportBug(new BugInstance("TYPE_ERASE_IN_MATCH", Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
			        .addSourceLine(this, getPC()));
		}
	}
	
	
	//TODO:添加所有支持泛型的类，这里只列了比较常用的几个，可以再添加一些
	private boolean isIntrestedType(String dottedClass){
		if(dottedClass.equals("scala.collection.immutable.List"))
			return true;
		else if(dottedClass.equals("scala.collection.immutable.Map"))
			return true;
		else if(dottedClass.equals("scala.collection.immutable.Set"))
			return true;
		else if(dottedClass.equals("scala.collection.mutable.List"))
			return true;
		else if(dottedClass.equals("scala.collection.mutable.Map"))
			return true;
		else if(dottedClass.equals("scala.collection.mutable.Set"))
			return true;
		return false;
	}
}
