
package cn.edu.sjtu.se.zhengWang;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.ba.ClassContext;


/**
 * Looks for methods that store the return result in a local variable, and
 * then immediately returns that local variable.
 */
public class StoreBeforeReturnDetector extends BytecodeScanningDetector
{
	private BugReporter bugReporter;
	private Method method;
	private OpcodeStack stack;
	
	private int LastSecond;
	private int LastThird;
	private int Step;
	private int tempReg;

	
	public StoreBeforeReturnDetector(BugReporter bugReporter)
	{
		this.bugReporter = bugReporter;
	}
	
	
	@Override
	public void visitClassContext(ClassContext classContext)
	{
		try
		{
			stack = new OpcodeStack();
			super.visitClassContext(classContext);
		}
		finally
		{
			stack = null;
		}
	}

	
	@Override
    public void visitMethod(Method method)
	{
	    this.method = method;
	    super.visitMethod(method);
	}
	/*
	 * 
	 * @param the code which is visited must have a return value;
	 */	
	@Override
	public void visitCode(Code obj)
	{		
		if (!method.getReturnType().equals(Type.VOID))
		{
			stack.resetForMethodEntry(this);
			LastSecond = -1;
			LastThird = -1;
			Step = 0;//make sure the store and load operate is the last 3rd and 2nd
			tempReg = -1;//record the left register just visited in binary operate
			super.visitCode(obj);
		}	
	}
	/*
	 * 
	 * @param the code which is visited must have a return value;
	 */	
	@Override
	public void sawOpcode(int seen) 
	{
		if ((seen >= ISTORE_0) && (seen <= ASTORE_3))//when see store
		{
			if(stack.getStackDepth()>=1)
			{
				
				Integer StackUsedReg=(Integer)stack.getStackItem(0).getUserValue();
				if (StackUsedReg==null
						|| StackUsedReg != GetStoreReg(seen))
				{
					LastThird=LastSecond;
					LastSecond=seen;
					Step=0;
				}
			}
		}
		Step++;
		if ((seen >= ILOAD_0) && (seen <= ALOAD_3))//when see load
		{
			if(LastSecond-seen == 33){
				LastThird = LastSecond;
				LastSecond = seen;
			}
		}
		
		if ((seen >= IRETURN) && (seen <= ARETURN))//when see return
		{
			if (LastSecond >= ILOAD_0 && LastSecond<=ALOAD_3
			    && LastThird-LastSecond == 33 && Step==3)
			{
				bugReporter.reportBug(new BugInstance(this, "STORE_BEFORE_RETURN", NORMAL_PRIORITY)
				.addClass(this)
				.addMethod(this)
				.addSourceLine(this));
			}
		}
			
		tempReg = getBinaryOpReg(seen);
		
		stack.sawOpcode(this,seen);
		
		if (tempReg > -1)
		{
			OpcodeStack.Item item0 = stack.getStackItem(0);
			item0.setUserValue(tempReg);
		}
	}//end of sawOpcode
	
	/*
	 * 
	 * @param get the register number of the seen
	 */	
	public int GetStoreReg(int store)
	{
		return (store - 59) % 4;
	}
	
	/*
	 * 
	 * @param get the register of Binary Operand() in the left side
	 */	
	public int getBinaryOpReg(int seen)
	{
		if ((seen >= IADD)&&(seen <= DREM)
			&&stack.getStackDepth() >= 2)
		{
			return stack.getStackItem(1).getRegisterNumber();	
		}
		return -1;
	}
	
}