/**
 * This a free software developed by Willard Wang from SJTU.
 * You can freely use or modify anything of this software.
 * Contact the author at deathspeeder@gmail.com
 * 
 * 
 * File name: VarNotReassigned.java
 * Author: deathspeeder
 * Create date: Dec 12, 2011
 * @formatter:off
 */
/**
 * @formatter:on
 */
package cn.edu.sjtu.se.junWang;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

/**
 * @author deathspeeder
 * 
 */
public class VarNotReassignedDetector extends BytecodeScanningDetector {

    private BugReporter bugReporter;
    private Map<String, Boolean> checkMap;

    public VarNotReassignedDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Method obj) {
        super.visit(obj);
        if (obj.getName().endsWith("_$eq")) {
            Boolean entry = checkMap.get(obj.getName());
            if (null != entry && !entry) {
                bugReporter
                        .reportBug(new BugInstance("VAR_NOT_REASSIGNED",
                                NORMAL_PRIORITY).addClassAndMethod(
                                getThisClass(), obj));
            }
        }
    }

    @Override
    public void visit(JavaClass obj) {
        super.visit(obj);
        checkMap = new HashMap<String, Boolean>();
        Field[] fields = obj.getFields();
        for (Field f : fields) {
            checkMap.put(f.getName() + "_$eq", false);
        }

        Method[] methods = obj.getMethods();
        for (Method m : methods) {
            String code = m.getCode().toString();
            Set<Map.Entry<String, Boolean>> sets = checkMap.entrySet();
            for (Map.Entry<String, Boolean> entry : sets) {
                if (code.contains(this.getClassName().replace('/', '.') + "."
                        + entry.getKey())) {
                    entry.setValue(true);
                }
            }
        }
    }

}
