/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * @author liuzhenbang
 */
/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package cn.edu.sjtu.se.zhenbangLiu;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Constant;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.visitclass.DismantleBytecode;

/**
 * @author liuzhenbang
 */
public class CheckRepeatedCondition extends OpcodeStackDetector {

	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.bcel.OpcodeStackDetector#sawOpcode(int)
	 */
	/*public class match
	{
		String str1;
		String str2;
		int jumpoffset;
		int keyopcode;
	}*/
	//match pattern[]=new match[1000];
	BugAccumulator bugAccumulator;
	public String pre;
	String ref1,ref2;
	BugReporter bugReporter;
	public CheckRepeatedCondition(BugReporter bugReporter) {
	this.bugAccumulator = new BugAccumulator(bugReporter);
	this.pre = null;

	 
	}

	@Override
	public void visit(Code obj){
	super.visit(obj);
	bugAccumulator.reportAccumulatedBugs();

	}
	String str[]=new String[1000];
	int jumpoffset[]=new int[1000];
	int keyopcode[]=new int[1000];
	String str1;
	int i,j;
	@Override
	
	//TODO
	/**
	 * This method has an error in the if condition.
	 * It was this.getDottedSigConstantOperand().equals("()I")
	 * at first, but it was reported as an error since the
	 * method getDottedSigConstantOperand() is not defined.
	 * So I just replaced it with a relatively similar method of
	 * this class getDottedClassConstantOperand().
	 * Find the correct one!!
	 * 
	 */
	public void sawOpcode(int seen) {
		if(seen==INVOKEVIRTUAL&&this.getClassConstantOperand().equals("testApp$")
			&&this.getDottedClassConstantOperand().equals("()I"))
		{
				/*pattern[i].str1=this.getNameConstantOperand();
				//str[i][0]=this.getNameConstantOperand();
				pattern[i].str2=this.getNameConstantOperand();
				//str[i][1]=this.getNameConstantOperand();*/
				str[j]=this.getNameConstantOperand();
				j++;
				str1=this.getDottedClassConstantOperand();
			
		}
		if((seen==IF_ACMPEQ||seen==IF_ACMPNE||seen==IF_ICMPNE||seen==IF_ICMPEQ||
        seen==IF_ICMPGT||seen==IF_ICMPLE||seen==IF_ICMPLT||seen==IF_ICMPGE)
        )
		{
				
				jumpoffset[i]=this.getBranchOffset()+this.getPC();
				int flag=i-1;
				i++;
				int flag1=j;
				while(flag>0)
				{
					while(flag1>0)
					{
					
					if(
							(str[j]==(str[flag1])
							&&str[j-1]==(str[flag1-1]))
							||(str[j]==(str[flag1-1])
							&&str[j-1]==(str[flag1]))
							&&jumpoffset[flag]==this.getBranchOffset()
							&&keyopcode[flag]==this.getOpcode()
							)
					{
						bugAccumulator.accumulateBug(new BugInstance(this, "RpC_REPEATED_CONDITIONAL_TEST1", NORMAL_PRIORITY)
						.addClassAndMethod(this).addSourceLine(this, this.getPC()).addInt(this.getPC()), this);
						//bugReporter.reportBug(new BugInstance("RpC_REPEATED_CONDITIONAL_TEST1", NORMAL_PRIORITY).addClassAndMethod(this)
						 //       .addSourceLine(this, getPC()).addInt(getPC()));
					}
					flag1--;
					}
					flag--;
			
				}
		}
			
		}
	}



