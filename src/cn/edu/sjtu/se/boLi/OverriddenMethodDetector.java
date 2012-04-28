package cn.edu.sjtu.se.boLi;

import java.util.HashMap;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import org.apache.bcel.util.BCELifier;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

import edu.umd.cs.findbugs.classfile.analysis.ClassInfo;
import edu.umd.cs.findbugs.ba.ClassContext;

public class OverriddenMethodDetector extends BytecodeScanningDetector
{
	private BugReporter bugReporter;
	private ClassContext classContext;   
  private JavaClass javaClass;          
  private Method[] methods;             
  private int methodIndex;              
  private String methodName;   
	
	public OverriddenMethodDetector(BugReporter bugReporter) 	
	{
      this.bugReporter = bugReporter;

      //trackers = new HashMap<JavaClass, ConstructorInvocationTracker>();
  }
  
  @Override
    public void visitClassContext(ClassContext classContext) 
    {
        this.classContext = classContext;
        javaClass = classContext.getJavaClass();
        methods = javaClass.getMethods();
        methodIndex = 0;
        javaClass.accept(this);
    }
  
  @Override
    public void visit(Code code) 
    {
        try
        {
	        	JavaClass theSuperClass = javaClass.getSuperClass();       	        	        		
	          Method m = methods[methodIndex];
	         	methodIndex = methodIndex + 1;
	         	for(int j = 0; j < theSuperClass.getMethods().length; j++)
	         	{
	          		Method m1 = theSuperClass.getMethods()[j];
	           		if(m.getName().equals(m1.getName()) && m.getSignature().equals(m1.getSignature()))
	           		{
	           			if(m.getName().equals("<init>"))
	           				continue;
	           			else
	           			{
		           			//System.out.println("hashcode of method " + m.getName() + " from derived class: " + m.getCode().toString());
		           			//System.out.println("hashcode of method " + m1.getName() + " from super class: " + m1.getCode().toString());
		           			int numOfSame = 0;
		           			byte[] b1 = m.getCode().getCode();
		           			byte[] b2 = m1.getCode().getCode();
		           			
		           			if (hashByteArray(b1) == hashByteArray(b2))
		           			{
		           				System.out.println("in");
		           				bugReporter.reportBug(new BugInstance("RE-IMPLEMENT_OVERRIDDEN",NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this));	           				           				
		           				break;
		           			}
	           			}
	           		}
	          }	        
      	}
      	catch(ClassNotFoundException exception)
      	{
      		exception.printStackTrace();
      	}
        super.visit(code);
    }
    
    private int hashByteArray(byte[] byteArray)
    {
    	int hash = byteArray[0];    	
    	for (byte b:byteArray)
    	{
    		hash &= b;
    	}
    	
    	return hash;
    }


}