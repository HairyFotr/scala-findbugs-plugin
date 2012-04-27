package cn.edu.sjtu.stap.detector.scala;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;

import cn.edu.sjtu.stap.detect.util.FindbugUtils;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.gui2.MainFrame;

public class DeadCodeReturnDetector extends SourceFileOpcodeStackDetector{

	private BugReporter bugReporter;
	private boolean hasBranch = false;
	

	
	public DeadCodeReturnDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	private Project project;
	public void setProject(Project p){
		this.project = p;
	}

	
	
	public void visit(Method method){
		super.visit(method);
		hasBranch = false;
		
	}
	
	public Project getProject(){
		return MainFrame.getInstance().getProject();
	}
	
	@Override
	public void sawOpcode(int seen) {
		if(isBranch(seen))
			hasBranch = true;
		if(isReturn(seen) && !hasBranch){
			String methodName = getMethod().getName();
			if(!methodName.startsWith("<") && !methodName.endsWith(">")){
				LineNumberTable lnTable = getMethod().getLineNumberTable();
				if(lnTable!=null){
					String source = getSourceFileContent();
					if(source!=null){
						analyzeInSourceFile(source,methodName,lnTable);
					}
				}

			}
		}
	}
	
	private void analyzeInSourceFile(String source,String methodName,LineNumberTable lnTable){
		String[] lines = source.split("\n");
		int lastLine = lnTable.getSourceLine(lnTable.getLength()-1);
		if(lines.length>=lastLine){
			String line = lines[lastLine-1];
			String str;
			int returnIndex = line.indexOf("return");
			if(returnIndex<0)
				return;
			int index = line.indexOf(";");
			if(returnIndex>0 && index>0){
				str = line.substring(index+1); 
				if(!str.trim().equals(""))
					bugReporter.reportBug(new BugInstance("RELUCANT_RETURN",Priorities.NORMAL_PRIORITY).addClass(this).addSourceLine(new SourceLineAnnotation(getDottedClassName(),getSourceFile(),lastLine,lastLine,1,1)));
			}
			
			for(int i=lastLine;i<lines.length;i++){
				line = lines[i];
				int rightBracketIndex = line.indexOf("}");
				if(rightBracketIndex>=0)
					break;
				if(returnIndex>0 && !line.trim().equals(""))
					bugReporter.reportBug(new BugInstance("RELUCANT_RETURN",Priorities.NORMAL_PRIORITY).addClass(this).addSourceLine(new SourceLineAnnotation(getDottedClassName(),getSourceFile(),i+1,i+1,1,1)));
				}

			}
		}
}
