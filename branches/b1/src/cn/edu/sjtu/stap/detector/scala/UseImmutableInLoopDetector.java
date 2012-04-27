package cn.edu.sjtu.stap.detector.scala;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class UseImmutableInLoopDetector extends OpcodeStackDetector{

	private static final String	USE_IMMUT_COLLECTION_IN_LOOP="USE_IMMUT_COLLECTION_IN_LOOP";
	
	private BugReporter bugReporter;
	
	private List<Loop> loops = new ArrayList<Loop>();
	private Loop currentLoop = new Loop(0,0);
	private int invokePC = 0;
	private int checkCastPC = 0;
	
	public UseImmutableInLoopDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	public void visit(Method method){
		loops.clear();
		currentLoop.start=Integer.MAX_VALUE;
		currentLoop.end = 0;
		invokePC = 0;
		checkCastPC = 0;
	}
	
	@Override
	public void sawOpcode(int seen) {
		if(isJumpCmp(seen)){
			currentLoop.start = getPC();
			if(getBranchTarget()>currentLoop.start)
				currentLoop.end = getBranchTarget();
			loops.add(new Loop(currentLoop.start,currentLoop.end));
		}
		if(getPC()>currentLoop.start && getPC()<currentLoop.end ){
			if (seen == INVOKEINTERFACE&& getDottedClassConstantOperand().equals("scala.collection.SetLike")) {
				String calledFunc = getNameConstantOperand();
				if (calledFunc.equals("$plus") || calledFunc.equals("$minus")) {
					Item item = stack.getStackItem(1);
					if(item.getSignature().equals("Lscala/collection/immutable/Set;"))
						invokePC = getPC();
				}
			}
			if(seen==CHECKCAST && getDottedClassConstantOperand().equals("scala.collection.immutable.Set")){
				checkCastPC = getPC();
				if((getPrevOpcode(1)==INVOKEINTERFACE || invokePC+5==checkCastPC) && isAstore(getNextOpcode()))
					bugReporter.reportBug(new BugInstance(USE_IMMUT_COLLECTION_IN_LOOP,Priorities.NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC()));
			}
		}
		
		checkLoops(getPC());
	}
	
	private void checkLoops(int pc){
		for(int i=loops.size()-1;i>=0;i--){
			if(pc>loops.get(i).end)
				loops.remove(i);
		}
	}
	
	public static boolean isJumpCmp(int seen){
		return seen>=IF_ICMPEQ && seen<= IF_ACMPNE;
	}
	
	public static boolean isAload(int seen){
		return seen==ALOAD || (seen>=ALOAD_0 && seen<=ALOAD_3);
	}
	
	public static boolean isAstore(int seen){
		return seen==ASTORE || (seen>=ASTORE_0 && seen<=ASTORE_3);
	}
	
	class Loop{
		int start;
		int end;
		
		public Loop(int s,int e){
			start = s;
			end = e;
		}
	}
}
