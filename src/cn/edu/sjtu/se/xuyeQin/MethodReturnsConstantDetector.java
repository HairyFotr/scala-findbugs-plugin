package cn.edu.sjtu.se.xuyeQin;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.ba.ClassContext;

public class MethodReturnsConstantDetector extends BytecodeScanningDetector {
	private final BugReporter bugReporter;
	private OpcodeStack stack;
	private Object returnConstant;
	private boolean methodSuspect;
	private int returnPC;
	
	public MethodReturnsConstantDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		try {
			stack = new OpcodeStack();
			super.visitClassContext(classContext);
		} finally {
			stack = null;
		}
	}
	
	/**
	 * implements the visitor to reset the stack and proceed for private methods
	 *
	 * @param obj the context object of the currently parsed code block
	 */
	@Override
	public void visitCode(Code obj) {
		Method m = getMethod();
		int aFlags = m.getAccessFlags();
		if ((((aFlags & Constants.ACC_PRIVATE) != 0))
		&&  ((aFlags & Constants.ACC_SYNTHETIC) == 0)
		&&  (!m.getSignature().endsWith(")Z"))) {
			stack.resetForMethodEntry(this);
			returnConstant = null;
			methodSuspect = true;
			returnPC = -1;
			super.visitCode(obj);
			if (methodSuspect && (returnConstant != null)) {
				BugInstance bi = new BugInstance(this, "MRC_METHOD_RETURNS_CONSTANT", NORMAL_PRIORITY)
									.addClass(this)
									.addMethod(this);
				if (returnPC >= 0) {
					bi.addSourceLine(this, returnPC);
				}

				bi.addString(returnConstant.toString());
				bugReporter.reportBug(bi);
			}
		}
	}

	/**
	 * implements the visitor to look for methods that return a constant
	 *
	 * @param seen the opcode of the currently parsed instruction
	 */
	@Override
	public void sawOpcode(int seen) {
		try {
			if (!methodSuspect) {
				return;
			}

			if ((seen >= IRETURN) && (seen <= ARETURN)) {
				if (stack.getStackDepth() > 0) {
					OpcodeStack.Item item = stack.getStackItem(0);

					Object constant = item.getConstant();
					if (constant == null) {
						methodSuspect = false;
						return;
					}
					if ((item.getUserValue() != null) && ("".equals(constant))) {
						methodSuspect = false;
						return;
					}
					if ((returnConstant != null) && (!returnConstant.equals(constant))) {
						methodSuspect = false;
						return;
					}

					returnConstant = constant;
				}
			} else if ((seen == GOTO) || (seen == GOTO_W)) {
				if (stack.getStackDepth() > 0) {
					methodSuspect = false;
				}
			}
		} finally {
			stack.sawOpcode(this, seen);
		}
	}
}
