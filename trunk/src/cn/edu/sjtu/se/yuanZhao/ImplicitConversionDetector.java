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

package cn.edu.sjtu.se.yuanZhao;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.StatelessDetector;


public class ImplicitConversionDetector extends BytecodeScanningDetector implements StatelessDetector{
	
	private String clsName;
	private BugReporter bugReporter;
	
	boolean element;
	boolean tuple;
	boolean typebug;

	int seenClauseAt_1;
	int seenClauseAt_2;
	

	public ImplicitConversionDetector(BugReporter bugreporter){
		System.out.println("construct a Implicit Conversion");
		
		this.bugReporter = bugreporter;
		
		element = false;
		tuple = false;
		typebug = false;
		seenClauseAt_1 = Integer.MIN_VALUE;
		seenClauseAt_2 = Integer.MIN_VALUE;
	}
	
	@Override
    public void visit(JavaClass obj) {
		clsName = obj.getClassName();
		System.out.println("Enter into a class:"+clsName);
	}
	
	@Override
    public void visit(Code code){
		System.out.println("Enter into a code:"+code.toString());
		System.out.println();
		
		super.visit(code); 
		
		seenClauseAt_1 = Integer.MIN_VALUE;
		seenClauseAt_2 = Integer.MIN_VALUE;
		element = false;
		tuple = false;
		typebug = false;
	}
	
	 @Override
    public void sawOpcode(int seen) { 
		 
		 if(seen == ALOAD_1){
			 seenClauseAt_1 = getPC();
		 }
		 
		 if(getPC() == seenClauseAt_1 + 1){			 
			 if(seen == INVOKEVIRTUAL && getNameConstantOperand().substring(0, 1).equals("_")
					 && getClassConstantOperand().equals("scala/Tuple3")){
				 element = true;
			 }else{
				 tuple = true;
			 }
		 }
		  
		 
		 if(seen == ALOAD_2){
			 seenClauseAt_2 = getPC();
		 }
		 
		 if(getPC() == seenClauseAt_2 + 1)
		 {
			 if(seen == INVOKEVIRTUAL && getNameConstantOperand().substring(0, 1).equals("_")
					 && getClassConstantOperand().equals("scala/Tuple3")){
				 if(element){
					 typebug = false;
					 System.out.println("both is elements,no bug");
					 return;
				 }else{
					 typebug = true;
					 System.out.println("----------------------judge a possible bug");
					 System.out.println();
				 }
			 }
			 else if(tuple){
				 typebug = false;
				 System.out.println("both is tuples,no bug");				 
				 return;
			 }else{
				 typebug = true;
				 System.out.println("----------------------judge a possible bug");
				 System.out.println();
			 }
		 }
		
		 /*if(seen == INVOKEVIRTUAL && getClassConstantOperand() == "java/lang/Object"
				 && getNameConstantOperand() == "equals" && (getPC() <= seenClauseAt_2 + 12)
				 && typebug){
			 
				 bugReporter.reportBug( 
		                 new BugInstance("Implicit_Conversion", HIGH_PRIORITY) 
		                 .addClassAndMethod(this).addSourceLine(this)); 
		 }*/
		 
		 if((getPC() <= seenClauseAt_2 + 20) && (getPC() > seenClauseAt_2)){
			 if(seen == IFEQ || seen == IF_ICMPNE){
				 if(typebug){
					 bugReporter.reportBug( 
							 new BugInstance("My_Implicit_Conversion", HIGH_PRIORITY) 
							 .addClassAndMethod(this).addSourceLine(this));
				 }
			 }
		 }
		
		 
		 
		 
	 }
}