package cn.edu.sjtu.se.changLiu;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class EquivalentJudgmentBugDetector extends BytecodeScanningDetector {
	BugReporter bugReporter;

	public EquivalentJudgmentBugDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	@Override
	public void visit(JavaClass c) {
		String className = c.getClassName();
		System.out.println("Find class: " + className);
		Map<String, Method> methods = new HashMap<String, Method>();

		for (Method obj : c.getMethods()) {
			String methodName = obj.getName() + "_" + obj.getSignature();
			System.out.println("Find method: " + methodName);

			methods.put(methodName, obj);
		}

		System.out.println("Check class: " + className);

		if (methods.keySet().contains("equals_(Ljava/lang/Object;)Z")
				&& !methods.keySet().contains("hashCode_()I")) {
			// TODO Report missing hashCode function
			System.out.println("Missing hashCode in : " + className);

			bugReporter.reportBug(new BugInstance("MISS_HASHCODE_FUNCTION",
					NORMAL_PRIORITY).addClassAndMethod(c,
					methods.get("equals_(Ljava/lang/Object;)Z")));
		} else if (methods.keySet().contains(
				"$eq$eq_(L" + className.replace('.', '/') + ";)Z")
				&& !methods.keySet().contains(
						"$bang$eq_(L" + className.replace('.', '/') + ";)Z")) {
			// TODO Report missing not equal function
			System.out.println("Missing not equal in : " + className);

			bugReporter.reportBug(new BugInstance("MISS_NOT_EQUAL_FUNCTION",
					NORMAL_PRIORITY).addClassAndMethod(
					c,
					methods.get("$eq$eq_(L" + className.replace('.', '/')
							+ ";)Z")));
		}

		super.visit(c);
	}

	@Override
	public void visit(Method obj) {
		String methodName = obj.getName() + "_" + obj.getSignature();
		if (obj.getName().equals("equals")
				&& !obj.getSignature().equals("(Ljava/lang/Object;)Z")) {
			System.out.println("Find illegal equals function : " + methodName);
			// TODO raise bug
			bugReporter.reportBug(new BugInstance("INVALID_EQUAL_FUNCTION",
					NORMAL_PRIORITY).addClassAndMethod(getThisClass(), obj));
		}

		super.visit(obj);
	}
}
