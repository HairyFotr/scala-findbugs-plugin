/*
 * PartialInitializedObjectDetector.java
 *   A bug pattern detector aimed at issueing unordered initialization warning 
 * Consider the following inherent hierachy:
 *   public class Base{
 *       public Base(){ foo(); }
 *       public void foo(){};
 *   }
 *
 *   public class Sub extends Base{
 *       public Sub(){};
 *       public void foo(){};
 *   }
 *
 *   we now have a test code: Sub s = new Sub();
 *   Which foo will be invoked first while the other later?
 *
 *   Experiments show that Sub.foo() will be invoked prior than Base.foo() when
 * invoking constructor of Base when constructing Sub. However, at this time,
 * s is partially constructed, this may lead to unexpected results.
 *
 * author: qiushuang
 */
package cn.edu.sjtu.se.shuangQiu;

import java.util.HashMap;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

import edu.umd.cs.findbugs.classfile.analysis.ClassInfo;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PartialInitializedObjectDetector extends BytecodeScanningDetector{
    /* constructor REQUIRED
     * as we can see, a bug detector is only initialized once,
     * and we also know that, when classloader loading class, its base class
     * is loaded first. Thus we can make sure the moment verifying sub-class
     * information of its base class already exists.
     */
    public PartialInitializedObjectDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;

        trackers = new HashMap<JavaClass, ConstructorInvocationTracker>();
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        /* record class context */
        this.classContext = classContext;
        /* reserve a reference to current class */
        javaClass = classContext.getJavaClass();
        /* extract all methods defined in current class */
        methods = javaClass.getMethods();
        /* initialize method index */
        methodIndex = 0;

        javaClass.accept(this);

    }

    @Override
    public void visit(Code code) {
        methodName = methods[methodIndex].getName();
        String signature = methods[methodIndex].getSignature();
        methodIndex = methodIndex + 1;    /* update method index */

        JavaClass bases[] = null;
        try {
            bases = javaClass.getSuperClasses();

            for (JavaClass base : bases) {
                if (overrideBase(base, methodName, signature)
                    && invokedInBaseConstructor(base, methodName, signature)) {
                    bugReporter.reportBug(new BugInstance("PartialInitialized",
                        HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this));

                    break;         /* we've get what we want, stop ASAP */
                }
            }
        } catch(ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        super.visit(code);                /* invoke visit on base class */
    }

    /* in this detector, we only prcess those virtual call within constructor */
    @Override
    public void sawOpcode(int seen) {
        if (TARGET_METHOD.equals(methodName) && (seen == INVOKEVIRTUAL)) {
            String clsName = getClassConstantOperand();
            String methodName = getNameConstantOperand();
            String signature = getSigConstantOperand();

            if (clsName.equals(javaClass.getClassName())) {
                ConstructorInvocationTracker tracker = trackers.get(javaClass);
                /* current class is not recorded  */
                if (tracker == null){
                    tracker = new ConstructorInvocationTracker(javaClass);
                }

                tracker.methodInvoke(
                    composeMethodHeader(methodName, signature));
                /* re-put current tracker into hash table */
                trackers.put(javaClass, tracker);
            }
        }
    }

    /* given method name and method signature, compose a method header */
    private String composeMethodHeader(String methodName, String signature) {
        return methodName + " " + signature;
    }

    /* check weather specific method override base */
    private boolean overrideBase(JavaClass base, String method, String signature) {
        /* flag for single exit point, set when method override its base */
        boolean overrided = false;

        Method[] baseMethods = base.getMethods();
        for (Method baseMtd: baseMethods) {
            if (method.equals(baseMtd.getName()) &&
                signature.equals(baseMtd.getSignature())) {
                /* override method matched */
                overrided = true;

                break;      /* we've get what wanted, stop ASAP */
            }
        }

        return overrided;
    }

    /* check weather specific method is invoked in constructor of base class */
    private boolean invokedInBaseConstructor(JavaClass base, String method, String signature) {
        /* flag for single exit point, set when invokation in base found */
        boolean invoked = false;

        ConstructorInvocationTracker tracker = trackers.get(base);
        /* indeed some methods invoked in base class */
        if (tracker != null) {
            if (tracker.doesMethodInvoked(
                composeMethodHeader(method, signature))) {
                invoked = true;
            }
        }

        return invoked;
    }

    private BugReporter bugReporter;      /* report a bug */
    private ClassContext classContext;    /* class now parsing */
    private JavaClass javaClass;          /* parsing class information */
    private Method[] methods;             /* all methods implemented in class */
    private int methodIndex;              /* index to method now processing */
    private String methodName;            /* name of method now processing */

    /* only process constructors eliminating all its signatures */
    private static final String TARGET_METHOD = "<init>";
    /* class and its invoked methods */
    private HashMap<JavaClass, ConstructorInvocationTracker> trackers;
}