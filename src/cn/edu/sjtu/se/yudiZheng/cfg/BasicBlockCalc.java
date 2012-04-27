package cn.edu.sjtu.se.yudiZheng.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class BasicBlockCalc {

	// Make sure an instruction has a valid next-instruction.
	// NOTE that in asm, label might be an AbstractInsnNode. If an instruction
	// is followed with a label which is the end of an instruction list, then
	// it has no next instruction.
	public static boolean hasNextNonVirtInstr(InsnList instrLst, int i) {

		int nextInstrIndex = i + 1;

		// not valid next index
		if (nextInstrIndex >= instrLst.size()) {
			return false;
		}

		AbstractInsnNode nextInstruction = instrLst.get(nextInstrIndex);

		// is non-virtual instruction later in a list ?
		while (nextInstruction != null) {

			if (!AsmHelper.isVirtualInstr(nextInstruction)) {
				return true;
			}

			nextInstruction = nextInstruction.getNext();
		}

		return false;
	}

	// Get basic blocks of the given method node.
	public static List<AbstractInsnNode> getAll(InsnList instructions, List<TryCatchBlockNode> tryCatchBlocks) {

		// add method automatically skips all the labels
		Set<AbstractInsnNode> bbStarts = new HashSet<AbstractInsnNode>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean add(AbstractInsnNode e) {
				return super.add(AsmHelper.skipVirualInsns(e, true));
			}
		};

		bbStarts.add(instructions.getFirst());

		for (int i = 0; i < instructions.size(); i++) {

			AbstractInsnNode instruction = instructions.get(i);
			int opcode = instruction.getOpcode();

			if (opcode == Opcodes.ATHROW) {
				bbStarts.add(instruction.getNext());
				continue;
			}

			switch (instruction.getType()) {
			case AbstractInsnNode.JUMP_INSN: {
				// Covers IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
				// IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,
				// IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL, and IFNONNULL.
				bbStarts.add(((JumpInsnNode) instruction).label);

				// goto never returns
				if (opcode != Opcodes.GOTO && hasNextNonVirtInstr(instructions, i)) {
					bbStarts.add(instruction.getNext());
				}

				break;
			}

			case AbstractInsnNode.LOOKUPSWITCH_INSN: {
				// Covers LOOKUPSWITCH
				LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) instruction;

				for (Object label : lsin.labels) {
					bbStarts.add((LabelNode) label);
				}

				bbStarts.add(lsin.dflt);
				break;
			}

			case AbstractInsnNode.TABLESWITCH_INSN: {
				// Covers TABLESWITCH
				TableSwitchInsnNode tsin = (TableSwitchInsnNode) instruction;

				for (Object label : tsin.labels) {
					bbStarts.add((LabelNode) label);
				}

				bbStarts.add(tsin.dflt);
				break;
			}

			default:
				break;
			}
		}

		// add also starts of the handlers
		for (TryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
			bbStarts.add(tryCatchBlock.handler);
		}

		// sort starting instructions
		List<AbstractInsnNode> bbSortedList = new ArrayList<AbstractInsnNode>();

		for (AbstractInsnNode instruction : instructions.toArray()) {

			if (bbStarts.contains(instruction)) {
				bbSortedList.add(instruction);
			}
		}

		return bbSortedList;
	}
}
