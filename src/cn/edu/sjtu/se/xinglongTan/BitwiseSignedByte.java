package cn.edu.sjtu.se.xinglongTan;

import org.apache.bcel.classfile.Code;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class BitwiseSignedByte extends OpcodeStackDetector {
	BugReporter bugReporter;

	public BitwiseSignedByte(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	@Override
	public void visit(Code obj) {
		super.visit(obj);
	}

	@Override
	public void sawOpcode(int seen) {

		//bug always comes with IOR 
		if (seen == IOR) {
			System.out.println(stack);
			
			//get the top 2 stack item
			OpcodeStack.Item first = stack.getStackItem(0);
			OpcodeStack.Item second = stack.getStackItem(1);
			
			/* if one of the 2 stack item has these attributes then cause a bug:
			 * the value type is "B" -- Byte
			 * the Special Kind is 1  -- means it is signed_byte
			 * if the value can be access, it should be less than 0
			 */
			if ((first.getSignature().equals("B")
					&& (first.getSpecialKind() == 1) && ((first.getConstant() == null) || Integer
					.parseInt(first.getConstant().toString()) < 0))
					|| (second.getSignature().equals("B")
							&& (second.getSpecialKind() == 1)
							&& ((second.getConstant() == null) || Integer
							.parseInt(second.getConstant().toString()) < 0))) {

				BugInstance bug = new BugInstance(this, "BIT_IOR_SIGNED_BYTE",1)
								.addClassAndMethod(this)
								.addSourceLine(this, getPC());
				bugReporter.reportBug(bug);
			}
		}
	}
}