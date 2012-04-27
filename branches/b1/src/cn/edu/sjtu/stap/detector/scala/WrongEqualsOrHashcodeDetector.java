package cn.edu.sjtu.stap.detector.scala;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import cn.edu.sjtu.stap.scala.util.ScalaType;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.PreorderDetector;

public class WrongEqualsOrHashcodeDetector extends PreorderDetector{

	public static String EQUALS = "equals";
	
	public static String EQ = "$eq$eq";
	
	public static String HASHCODE = "hashCode";
	
	private BugReporter bugReporter;
	
	private Set<String> set =  new HashSet<String>();
	
	public WrongEqualsOrHashcodeDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void visit(JavaClass obj) {
		set.clear();
	}
	
	@Override
	public void visitAfter(JavaClass obj) {
		if(set.contains(EQUALS) && !set.contains(HASHCODE)){
			bugReporter.reportBug(new BugInstance("DEFINE_EQUALS_NO_HASHCODE",Priorities.NORMAL_PRIORITY).addClass(obj));
		}
		
		if(set.contains(HASHCODE) && !set.contains(EQUALS)){
			bugReporter.reportBug(new BugInstance("DEFINE_HASHCODE_NO_EQUALS",Priorities.NORMAL_PRIORITY).addClass(obj));
		}
	}
	
	@Override
	public void visit(Method obj) {
		String methodName = obj.getName();
		if(methodName.equalsIgnoreCase(EQUALS)){
			if(checkEqualsSignature(obj))
				set.add(EQUALS);
			else
				bugReporter.reportBug(new BugInstance("WRONG_EQUALS_SIG",Priorities.NORMAL_PRIORITY).addClassAndMethod(this));
		}else if(methodName.equalsIgnoreCase(HASHCODE)){
			if(checkHashCodeSignature(obj))
				set.add(HASHCODE);
			else
				bugReporter.reportBug(new BugInstance("WRONG_HASHCODE_SIG",Priorities.NORMAL_PRIORITY).addClassAndMethod(this));
		}else if(methodName.equalsIgnoreCase(EQ)){
			checkEq(obj);
		}
	}
	
	private static boolean checkEq(Method m){
		return true;
	}
	
	private static boolean checkEqualsSignature(Method m){
		Type[] args  = m.getArgumentTypes();
		if(m.getName().equals(EQUALS) && m.isPublic() && m.getReturnType().equals(Type.BOOLEAN) && args.length==1 && args[0].equals(ScalaType.ANY))
			return true;
		return false;
	}
	
	private static boolean checkHashCodeSignature(Method m){
		if(m.getName().equals(HASHCODE) && m.isPublic() && m.getReturnType().equals(Type.INT) && m.getArgumentTypes().length==0)
			return true;
		return false;
	}
}
