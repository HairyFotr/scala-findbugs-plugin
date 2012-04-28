package cn.edu.sjtu.se.xiJia;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import cn.edu.sjtu.se.xiJia.bean.MethodOperate;
import cn.edu.sjtu.se.xiJia.bean.Operate;
import cn.edu.sjtu.se.xiJia.bean.SimpleOperate;
import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class CheckImplicitDetector extends OpcodeStackDetector {
	BugAccumulator bugAccumulator;
	private Set<String> operators;
	private Stack<Operate> hist;
	private Set<Integer> codes;

	public CheckImplicitDetector(BugReporter bugReporter) {
		this.bugAccumulator = new BugAccumulator(bugReporter);
		this.operators = new HashSet<String>();
		this.operators.add("$plus$eq");
		// TODO others

		this.hist = new Stack<Operate>();
		this.codes = new HashSet<Integer>();
	}

	public void visit(Code obj) {
		super.visit(obj);
		bugAccumulator.reportAccumulatedBugs();
	}

	@Override
	public void visitMethod(Method obj) {
		hist.clear();
		System.out.println(obj.getName());
	}

	@Override
	public void report() {
		super.report();
		for (Integer i : codes) {
			System.out.println(i);
		}
	}

	private void printHistory() {
		for (Operate o : hist) {
			System.out.println(o);
		}
		System.out.println();
		System.out.println();
	}

	@Override
	public void sawOpcode(int arg0) {
		if (arg0 == INVOKEVIRTUAL
				&& (operators.contains(this.getNameConstantOperand()))) {
			System.out.println("The operator " + this.getNameConstantOperand()
					+ " has been invoked.");

			int count = getArgumentCount(this.getMethodDescriptorOperand()
					.getSignature()) + 1;

			System.out.println("************** " + count + "*****************");
			Operate o;
			while (count != 1) {
				o = hist.peek();
				hist.pop();
				count -= o.getStackOp();
				System.out.println("************** " + o.getOpCode() + " "
						+ count + " " + o.getStackOp() + " *****************");
			}

			o = hist.peek();
			if (MethodOperate.class.isInstance(o)) {
				MethodOperate mo = (MethodOperate) o;
				if (mo.getArgumentNum() == 1 && !mo.getReturnType().equals("V")) {
					JavaClass jc;
					try {
						jc = mo.getStackTop().getJavaClass();
						boolean match = false;
						if (jc != null) {
							System.out
									.println("************** Class is not null *****************");
							Method[] methods = jc.getMethods();
							for (Method m : methods) {
								if (m.getSignature().equals(
										this.getMethodDescriptorOperand()
												.getSignature())
										&& m.getName()
												.equals(this
														.getMethodDescriptorOperand()
														.getName())) {
									match = true;
									System.out
											.println("************** Find It *****************");
									break;
								}
							}
						} else {
							System.out
									.println("************** Class is null *****************");
						}
						if (jc == null || !match) {
							bugAccumulator.accumulateBug(
									new BugInstance(this, "IMPLICIT_MISUSE",
											NORMAL_PRIORITY)
											.addClassAndMethod(this)
											.addSourceLine(this, getPC())
											.addInt(getPC()), this);
							System.out
									.println("************** Got It *****************");
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				} else {
					System.out
							.println("**************Class Wrong*****************");
				}
			}

			printHistory();
		} else {
			if (((arg0 >> 5) == 5 || (arg0 >> 5) == 6) && (arg0 & 15) >= 6
					&& (arg0 & 15) <= 10) {
				try {
					System.out.println(getMethodDescriptorOperand()
							.getSignature());
				} catch (Exception e) {
				}
				hist.push(new MethodOperate(arg0, (this
						.getMethodDescriptorOperand().getSignature()
						.endsWith("V") ? 0 : 1)
						- getArgumentCount(this.getMethodDescriptorOperand()
								.getSignature())
						- ((arg0 == 184 || arg0 == 217) ? 0 : 1), this
						.getMethodDescriptorOperand().getName(), this
						.getDottedClassConstantOperand(), this
						.getMethodDescriptorOperand().getSignature(),
						getArgumentCount(this.getMethodDescriptorOperand()
								.getSignature()), this.getStack()
								.getStackDepth() == 0 ? null : this.getStack()
								.getStackItem(0)));
			} else if (arg0 == POP || arg0 == POP2) {
				hist.clear();
			} else if ((arg0 <= 53 && arg0 > 1) || arg0 == BIPUSH
					|| arg0 == SIPUSH || arg0 == GETSTATIC) {
				hist.add(new SimpleOperate(arg0, 1));
			}
		}

	}

	private int getArgumentCount(String sig) {
		String arg = sig.substring(sig.indexOf('(') + 1, sig.indexOf(')'));
		if (arg.equals("") || arg == null) {
			return 0;
		} else {
			return arg.split(",").length;
		}
	}
}
