package cn.edu.sjtu.se.yudiZheng.cfg;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;

public class BasicBlock {
	// index of the basic block, count according to the order in a method
	private int index;

	private AbstractInsnNode entrance;
	private AbstractInsnNode exit;

	private Set<BasicBlock> predecessors;
	private Set<BasicBlock> successors;
	
	// joins refer to the join point of a new cfg to an existing cfg in the 
	// same method. NOTE that an exception handler is regarded as a new cfg
	// but not included in the normal execution cfg
	private Set<BasicBlock> joins;

	private Set<BasicBlock> dominators;

	// whether this basic block starts a loop or not  
	private boolean loop;

	public BasicBlock(int index, AbstractInsnNode entrance,
			AbstractInsnNode exit) {
		this.index = index;
		this.entrance = entrance;
		this.exit = exit;

		successors = new HashSet<BasicBlock>();
		predecessors = new HashSet<BasicBlock>();
		joins = new HashSet<BasicBlock>();

		dominators = new HashSet<BasicBlock>();

		loop = false;
	}

	public int getIndex() {
		return index;
	}

	public AbstractInsnNode getEntrance() {
		return entrance;
	}

	public void setExit(AbstractInsnNode exit) {
		this.exit = exit;
	}

	public AbstractInsnNode getExit() {
		return exit;
	}

	public Set<BasicBlock> getPredecessors() {
		return predecessors;
	}

	public Set<BasicBlock> getSuccessors() {
		return successors;
	}

	public Set<BasicBlock> getJoins() {
		return joins;
	}

	public Set<BasicBlock> getDominators() {
		return dominators;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public boolean isLoop() {
		return loop;
	}

}
