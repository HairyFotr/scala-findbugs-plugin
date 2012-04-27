package cn.edu.sjtu.se.yudiZheng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Visitor;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Location;
import edu.umd.cs.findbugs.visitclass.BetterVisitor;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
http://www.scala-lang.org/node/258 107 120
http://stackoverflow.com/questions/4653424/what-are-the-disadvantages-to-declaring-scala-case-classes
http://stackoverflow.com/questions/7166823/scala-case-class-equals-not-working-as-expected
*/
	
public class CaseClassInheritanceDetector extends PreorderVisitor implements Detector {

	public final static String CLASS_PRODUCT = "scala.Product";

	public final static String METHOD_EQUALS = "equals";

	public final static String METHOD_HASHCODE = "hashCode";

	public final static String METHOD_TOSTRING = "toString";

	public final static String CALLSITE_HASHCODE = "scala.runtime.ScalaRunTime$._hashCode (Lscala/Product;)I";

	public final static String CALLSITE_TOSTRING = "scala.runtime.ScalaRunTime$._toString (Lscala/Product;)Ljava/lang/String;";

	public static Set<String> caseClasses = new HashSet<String>();

	private BugReporter bugReporter;

	private ClassContext context;

	public CaseClassInheritanceDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	public void visitClassContext(ClassContext classContext) {

		this.context = classContext;
		classContext.getJavaClass().accept(this);
	}

	@Override
	public void visitJavaClass(JavaClass clazz) {

		if (isCaseClass(clazz)) {

			try {

				JavaClass parent = clazz.getSuperClass();

				if (isCaseClass(parent)) {
					bugReporter.reportBug(new BugInstance(this, "CASE_INHERIT", NORMAL_PRIORITY).addClass(clazz));
				}

			} catch (ClassNotFoundException e) {
				bugReporter.logError("Error", e);
			}
		}
	}

	public boolean isCaseClass(JavaClass clazz) {

		if (caseClasses.contains(clazz.getClassName())) {
			return true;
		}

		if (!Arrays.asList(clazz.getInterfaceNames()).contains(CLASS_PRODUCT)) {
			return false;
		}

		Method method_hashcode = getMethod(clazz, METHOD_HASHCODE);

		if (method_hashcode == null) {
			return false;
		}

		Method method_tostring = getMethod(clazz, METHOD_TOSTRING);

		if (method_tostring == null) {
			return false;
		}

		Method method_equals = getMethod(clazz, METHOD_EQUALS);

		if (method_equals == null) {
			return false;
		}

		try {

			if (!hasCallSite(method_hashcode, CALLSITE_HASHCODE)) {
				return false;
			}

			if (!hasCallSite(method_tostring, CALLSITE_TOSTRING)) {
				return false;
			}
		} catch (CFGBuilderException e) {
			bugReporter.logError("Error", e);
			return false;
		}

		caseClasses.add(clazz.getClassName());
		return true;
	}

	private Method getMethod(JavaClass clazz, String methodName) {

		for (Method method : clazz.getMethods()) {

			if (method.getName().equals(methodName)) {
				return method;
			}
		}

		return null;
	}

	private boolean hasCallSite(Method method, String callsite) throws CFGBuilderException {

		CFG cfg = context.getCFG(method);
		ConstantPool cp = context.getConstantPoolGen().getConstantPool();

		for (Iterator<Location> i = cfg.locationIterator(); i.hasNext();) {

			Location location = i.next();
			Instruction ins = location.getHandle().getInstruction();

			if (ins instanceof InvokeInstruction) {

				if (cp.constantToString(cp.getConstant(((InvokeInstruction) ins).getIndex())).equals(callsite)) {
					return true;
				}
			}
		}

		return false;
	}

	public void report() {
	}
}
