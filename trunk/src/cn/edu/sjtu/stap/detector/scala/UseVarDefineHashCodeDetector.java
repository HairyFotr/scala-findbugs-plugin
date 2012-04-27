package cn.edu.sjtu.stap.detector.scala;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.Priorities;

public class UseVarDefineHashCodeDetector extends BytecodeScanningDetector{
	public static String HASHCODE = "hashCode";
	
	private BugReporter bugReporter;
	
	private Map<String,Field> fields = new HashMap<String,Field>();
	private Set<String> fieldsUsedInHash = new HashSet<String>();
	private String currentMethod = "";
	
	public UseVarDefineHashCodeDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void visit(JavaClass obj) {
		fields.clear();
	}
	
	public void visitAfter(JavaClass obj){
//		if(fieldsUsedInHash.size()>0)
//			bugReporter.reportBug(new BugInstance("USE_VAR_DEFINE_HASHCODE",Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
//			        .addSourceLine(this, getPC()));
		fieldsUsedInHash.clear();
	}
	
	@Override
	public void visit(Method obj) {
		currentMethod = obj.getName();
	}
	
	public void visit(Field field){
		fields.put(field.getName(), field);
	}
	
	public void sawOpcode(int seen) {
		if(!currentMethod.equals(HASHCODE))
			return;
		if(seen==INVOKEVIRTUAL){
			String var = this.getNameConstantOperand();
			Field field = fields.get(var);
			if(field!=null && !field.isFinal()){
				fieldsUsedInHash.add(field.getName());
				bugReporter.reportBug(new BugInstance("USE_VAR_DEFINE_HASHCODE",Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
		        .addSourceLine(this, getPC()));
			}
		}
	}	
}
