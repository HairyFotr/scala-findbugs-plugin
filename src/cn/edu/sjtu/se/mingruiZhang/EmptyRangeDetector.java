package cn.edu.sjtu.se.mingruiZhang;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;


public class EmptyRangeDetector extends OpcodeStackDetector {

    private static final String WRAPPER_CLASS = "scala/Predef$";
    private static final String WRAPPER_NAME = "intWrapper";
    private static final String WRAPPER_SIG = "(I)Lscala/runtime/RichInt;";
    
    private static final String TO_CLASS = "scala/runtime/RichInt";
    private static final String TO_NAME = "to";
    private static final String TO_SIG = "(I)Lscala/collection/immutable/Range$Inclusive;";
    
    private static final String BY_CLASS = "scala/collection/immutable/Range$Inclusive";
    private static final String BY_NAME = "by";
    private static final String BY_SIG = "(I)Lscala/collection/immutable/Range;";
    
    private enum State {
        init, wrapped, pushed, waitBy, pushed2
    }
    private State state = State.init;
    private int lastWrappedIntValue;
    private int bugPC;

    BugReporter bugReporter;

    public EmptyRangeDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {
        boolean ivirtual = (seen == INVOKEVIRTUAL);
        String opClass = "";
        String opName = "";
        String opSig = "";
        if(ivirtual){
            opClass = getClassConstantOperand();
            opName = getNameConstantOperand();
            opSig = getSigConstantOperand();
            //System.out.println("invokevirtual("+opClass+opName+opSig+")");//test
        }
        if (state==State.init && ivirtual && opClass.equals(WRAPPER_CLASS) && opName.equals(WRAPPER_NAME) && opSig.equals(WRAPPER_SIG)) {
            //System.out.println("1");//test
            OpcodeStack.Item item = stack.getStackItem(0);
            lastWrappedIntValue = ((Integer)(item.getConstant())).intValue();
            state = State.wrapped;
        } else if (state==State.pushed && ivirtual && opClass.equals(TO_CLASS) && opName.equals(TO_NAME) && opSig.equals(TO_SIG)) {
            OpcodeStack.Item item = stack.getStackItem(0);
            //System.out.println("2");//test
            int value1 = lastWrappedIntValue;
            int value2 = ((Integer)(item.getConstant())).intValue();
            if (value1>value2) {
                bugPC = getPC();
                state = State.waitBy;
            } else {
                state = State.init;
            }
        } else if (state==State.pushed2) {
            //System.out.println("3");//test
            if (!(ivirtual && opClass.equals(BY_CLASS) && opName.equals(BY_NAME) && opSig.equals(BY_SIG)))
                bugReporter.reportBug(new BugInstance("EMPTY_RANGE", HIGH_PRIORITY).addClassAndMethod(this).addSourceLine(this, bugPC).addInt(bugPC));
            if(ivirtual && opClass.equals(WRAPPER_CLASS) && opName.equals(WRAPPER_NAME) && opSig.equals(WRAPPER_SIG))   //situation where "pushed2" should be "pushed"
                state = State.wrapped;
            else    //other situation
                state = State.init;
        } else {
            //System.out.println("4");//test
            switch(state){
                case wrapped:
                    state = State.pushed;
                    break;
                case waitBy:
                    state = State.pushed2;
                    break;
                default:
                    state = State.init;
                    break;
            }
        }
    }
}
