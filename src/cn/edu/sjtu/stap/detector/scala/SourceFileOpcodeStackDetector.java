package cn.edu.sjtu.stap.detector.scala;


import cn.edu.sjtu.stap.detect.util.FindbugUtils;

import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;


public abstract class SourceFileOpcodeStackDetector extends OpcodeStackDetector {
	public abstract Project getProject();
	
	public String getSourceFileContent(){
		return FindbugUtils.getSourceFileContent(getProject(), getPackageName(), getSourceFile());
	}
	
}
