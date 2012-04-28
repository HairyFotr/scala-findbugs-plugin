
package cn.edu.sjtu.se.taoLiu;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Location;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;




/**
 * @author JDS
 * 
 */

//public class VarInHashCodeDetector extends BytecodeScanningDetector implements
//        Detector {
//
//    private BugReporter bugReporter;
//
//    public VarInHashCodeDetector(BugReporter bugReporter) {
//        this.bugReporter = bugReporter;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * edu.umd.cs.findbugs.Detector#visitClassContext(edu.umd.cs.findbugs.ba
//     * .ClassContext)
//     */
//    @Override
//    public void visitClassContext(ClassContext classContext) {
//        JavaClass theClass = classContext.getJavaClass();
//        Set<String> fieldNames = new HashSet<String>();
//        JavaClass tempClass = theClass;
//       
//        while(theClass!=null){
//			for (int i = 0; i < theClass.getFields().length; i++) {
//
//				Field f = theClass.getFields()[i];
//				if (!fieldNames.contains(f.getName())&&!f.isFinal()) {					
//					fieldNames.add(f.getName());					
//				}
//			}
//			try {
//				theClass=theClass.getSuperClass();
//			} catch (ClassNotFoundException e) {
//				theClass=null;
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
//        theClass=tempClass;
//        System.out.println("SIZE1 "+fieldNames.size());
//        for(String fieldName : fieldNames)System.out.println("1 "+fieldName);
//        
//        Method hashCode = null;
//        for (int i = 0; i < theClass.getMethods().length; i++) {
//            Method m = theClass.getMethods()[i];
//            if (m.getName().equals("hashCode")) {
//                hashCode = m;
//                break;
//            }
//        }
//
//        //String prefix = theClass.getClassName() + ".";
//        
//        if (null != hashCode) {
//            String code = hashCode.getCode().toString();
//            System.out.println("Code:");
//            System.out.println(code);
//
//            for (String fieldName : fieldNames) {
//               
//                if (code.contains(fieldName)) {
//                    bugReporter.reportBug(new BugInstance("VAR_IN_HASHCODE",NORMAL_PRIORITY).addClass(theClass).addField(theClass.getClassName(), fieldName, "", false));
//                    
//                    //System.out.println(fieldName+" line:"+hashCode.getLineNumberTable().);
//                }
//            }
//        }
//
//        theClass.accept(this);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see edu.umd.cs.findbugs.Detector#report()
//     */
//    @Override
//    public void report() {
//        // TODO Auto-generated method stub
//
//    }
//
//}


public class VarInHashCodeDetector extends OpcodeStackDetector
{
	private BugReporter bugReporter;
	public VarInHashCodeDetector(BugReporter bugReporter) {
       this.bugReporter = bugReporter;
    }
	public void visit(Code obj) {  
	   super.visit(obj);  
    } 
	public void sawOpcode(int seen)
	{
		if(seen==INVOKEVIRTUAL && this.getMethodName().equals("hashCode"))
		{
			
			JavaClass theClass = this.getClassContext().getJavaClass();
			Set<String> fieldNames = new HashSet<String>();
			
			while(theClass!=null){
			   for (int i = 0; i < theClass.getFields().length; i++) {
	
					Field f = theClass.getFields()[i];
					if (!fieldNames.contains(f.getName())&&!f.isFinal()) {					
						fieldNames.add(f.getName());					
					}
				}
				try {
					theClass=theClass.getSuperClass();
				} catch (ClassNotFoundException e) {
					theClass=null;
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
			
			
			for (String fieldName : fieldNames)
			{
			    if(getNameConstantOperand().equals(fieldName))
			    {			
			    	bugReporter.reportBug(new BugInstance("VAR_IN_HASHCODE",NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this, getPC()).addInt(getPC()));
			    }
			}
			
		}
	}
	
}
