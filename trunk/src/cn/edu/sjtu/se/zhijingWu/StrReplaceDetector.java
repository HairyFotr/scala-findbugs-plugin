package cn.edu.sjtu.se.zhijingWu;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class StrReplaceDetector extends BytecodeScanningDetector{

	private BugReporter bugReporter;
	boolean ready;

	
	public  StrReplaceDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		ready=false;
	}
	@Override
	public void sawOpcode(int seen) {
	//System.out.println(seen);
	if(ready)
			{
			//System.out.println("here");
			if(seen==87)
			bugReporter.reportBug(new BugInstance("STR_REPLACE_MIS", Priorities.NORMAL_PRIORITY).addClassAndMethod(this)
			        .addSourceLine(this, getPC()));
					ready=false;
	}
else if (seen == INVOKEVIRTUAL && 
			getNameConstantOperand().equals("replace")
			&& getClassConstantOperand().equals("java/lang/String"))
			ready=true;
			
	}
	}