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

package cn.edu.sjtu.se.mengpingLi;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * @author WUTONG
 */
public class MissExceptionHandlerDetector extends OpcodeStackDetector{
	public static final String ACTOR = "scala.actors.Actor";
	public static final String REACT = "$$anonfun$act$";
	public static final String EXCEPTION = "Ljava/lang/Exception;";
	public static final int F_ACTOR = 1;
	public static final int F_REACT = 2;
	public static final int F_INIT = 0;
	
	static Map<String, Boolean> actors = new HashMap<String, Boolean>();
	static Map<String, Boolean> reacts = new HashMap<String, Boolean>();
	
	private BugReporter bugReporter;
	private int flag = F_INIT;
	private String aName = "";
	
	public MissExceptionHandlerDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	@Override
	public void visit(Code code) {
		super.visit(code);
	}

    @Override
    public void sawOpcode(int seen) {
    	if (flag == F_REACT){
	    	if (seen == org.apache.bcel.Constants.INVOKESPECIAL) {
	    		int i = stack.getStackDepth();
	    		for (; i > 0; i--){
	    			if(EXCEPTION.equals(stack.getStackItem(i-1).getSignature())){
	    				reacts.put(aName, true);
	    			}
	    		}
	    	}
    	}
    }
    
    @Override
	public void visitMethod(Method obj){
    	if (flag == F_ACTOR && obj.getName().equals("exceptionHandler") && obj.getCode().getLength() != 29){
    		actors.put(aName, true);
    	}
    		
    }
    
    @Override
    public void visitJavaClass(JavaClass obj){    	
    	String name = obj.getClassName();
    	if (name.contains(REACT)){
    		flag = F_REACT;
    		aName = name.substring(0,name.indexOf("$$"));
    		super.visitJavaClass(obj);
    		return;
    	}
    	
    	String[] interfaces = obj.getInterfaceNames();
    	for (String i : interfaces){
    		if (ACTOR.equals(i)){
    			flag = F_ACTOR;
    			aName = name;
    			actors.put(aName, false);
    			super.visitJavaClass(obj);
    			break;
    		}
    	}
        
        flag = F_INIT;
    }
    
	@Override
	public void visitAfter(JavaClass obj){
		if (flag != F_INIT){
			
			Boolean ab = actors.get(aName), rb = reacts.get(aName);
			
			if (ab != null &&  rb!= null && ab.equals(false) && rb.equals(true)){
//				System.out.println("" + ab + rb);
				bugReporter.reportBug(new BugInstance("MISS_EXCEPITON_HANDLER", NORMAL_PRIORITY).addClass(this));
			}
		}
	}
}
