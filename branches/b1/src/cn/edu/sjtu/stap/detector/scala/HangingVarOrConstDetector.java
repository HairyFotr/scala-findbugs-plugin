package cn.edu.sjtu.stap.detector.scala;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.gui2.MainFrame;


public class HangingVarOrConstDetector  extends SourceFileOpcodeStackDetector{

	private BugReporter bugReporter;
	private int lastStackSize;
	private int lastOpcode;
	private Method currentMethod;
	
	public HangingVarOrConstDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	

	private Project project;
	public void setProject(Project p){
		this.project = p;
	}
	
	public void visit(Method m){
		currentMethod = m;
	}

	@Override
	public Project getProject() {
		return MainFrame.getInstance().getProject();
	}
	
	@Override
	public void sawOpcode(int seen) {
		if(seen==POP || seen==POP2){
			if(stack.getStackDepth() > lastStackSize && getPrevOpcode(1)==lastOpcode){
				try{
					check();
				}catch(Exception e){}
			}
		}
	}
	
	@Override
	public void afterOpcode(int seen) {
		lastStackSize = stack.getStackDepth();
		super.afterOpcode(seen);
		lastOpcode = seen;
	}
	
	public void check(){
		if(getNextOpcode()==RETURN && getMethod().getReturnType().equals(Type.VOID)){
			String methodName = getMethod().getName();
			LineNumberTable lnTable = getMethod().getLineNumberTable();
			if(lnTable!=null){
				int firstLine = lnTable.getSourceLine(0);
				String source = getSourceFileContent();
				String[] lines  = source.split("\n");
				String part = "";
				int defIndex = -1;
				int funcIndex = -1;
				for(int i=firstLine-1;i>=0;i--){
					String line = lines[i];
					defIndex = line.lastIndexOf("def");
					funcIndex = line.lastIndexOf(methodName);
					part=line+part;
					if(defIndex>=0 && funcIndex>=0 && funcIndex>defIndex)
						break;
				}
				if(!part.contains("=")){
					bugReporter.reportBug(new BugInstance("NO_RETURN_TYPE_FOR_FUNC",Priorities.NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this,getNextPC()));
					return;
				}
			}
		}else{
			bugReporter.reportBug(new BugInstance("HANING_VAR_OR_CONST",Priorities.NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this,getPC()));
			
		}
		
	}
}




//public class HandingVarOrConstDetector  extends OpcodeStackDetector{
//
//	private BugReporter bugReporter;
//	private int lastStackSize;
//	
//	public HandingVarOrConstDetector(BugReporter bugReporter) {
//		this.bugReporter = bugReporter;
//	}
//	
//	public void visit(Method method){
//		
//	}
//	
//	@Override
//	public void sawOpcode(int seen) {
//		lastStackSize = stack.getStackDepth();
//	}
//	
//	@Override
//	public void afterOpcode(int seen) {
//		super.afterOpcode(seen);
//		if(stack.getStackDepth() > lastStackSize){
//			if(getNextOpcode()==POP || getNextOpcode()==POP2){
//				bugReporter.reportBug(new BugInstance("1",1).addClass(this));
//			}
//		}
//	}
//}

