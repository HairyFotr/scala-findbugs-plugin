/**
 * This a free software developed by Willard Wang from SJTU.
 * You can freely use or modify anything of this software.
 * Contact the author at deathspeeder@gmail.com
 * 
 * 
 * File name: OptionMisuseDetector.java
 * Author: deathspeeder
 * Create date: Nov 26, 2011
 * @formatter:off
 */
/**
 * @formatter:on
 */
package cn.edu.sjtu.se.junWang;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * @author deathspeeder
 * 
 */
public class ClassesInPackageObjectDetector extends BytecodeScanningDetector {

    private BugReporter bugReporter;

    public ClassesInPackageObjectDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        super.visitClassContext(classContext);
        String className = classContext.getJavaClass().getClassName();
        String packageName = classContext.getJavaClass().getPackageName() + ".package$";
        if (!className.equals(packageName) && className.startsWith(packageName)) {
            bugReporter
                    .reportBug(new BugInstance("PACKAGE_OBJECT_MISUSE", NORMAL_PRIORITY)
                            .addClass(classContext.getJavaClass()));
        }
        classContext.getJavaClass().accept(this);
    }

}