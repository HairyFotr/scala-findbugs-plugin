package cn.edu.sjtu.se.weichengYang.detector.scala;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.gui2.MainFrame;

public class FirstLetterLowerInMatchDetector extends SourceFileOpcodeStackDetector {
	
	private static Pattern targetPattern = Pattern.compile("case\\s*(.*?)\\s*=>");
	
	private BugReporter bugReporter;
	
	private Map<String,JavaClass> classes = new HashMap<String,JavaClass>();
	private Set<String> analyzedSourceFiles = new HashSet<String>();
	private Map<String,Integer> potentialBugs = new HashMap<String,Integer>();
	
	private String source;
	private String[] sourceLines;
	
	public FirstLetterLowerInMatchDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}
	
	private Project project;
	public void setProject(Project p){
		this.project = p;
	}
	
	@Override
	public Project getProject() {
		return MainFrame.getInstance().getProject();
	}
	
		
	@Override
	public void visit(JavaClass obj) {
		classes.put(obj.getClassName(), obj);
		
		source = getSourceFileContent();
		if(source!=null){
			sourceLines = source.split("\n");
			if(!analyzedSourceFiles.contains(obj.getPackageName()+"."+obj.getSourceFileName())){
				analyzeInSource();
				analyzedSourceFiles.add(obj.getPackageName()+"."+obj.getSourceFileName());
			}

		}

		if(potentialBugs.size()>0);
			doCheck();
	}
	
	
	@Override
	public void visit(Method obj) {
	
	}


	@Override
	public void sawOpcode(int seen) {
		
	}
	
	private void analyzeInSource(){
		Matcher m = targetPattern.matcher(source);
		while(m.find()){
			String matchName = m.group(1).trim();
			if(matchName.equals("\"_\"") || matchName.equals("'_'")){
				int line = locateLineNumber(m.group())+1;
				bugReporter.reportBug(new BugInstance("STR_UNDERLINE_IN_MATCH",Priorities.NORMAL_PRIORITY).addClass(this).addSourceLine(new SourceLineAnnotation(getDottedClassName(),getSourceFile(),line,line,1,1)));
			}
			if(!matchName.contains(":") && Character.isLowerCase(matchName.charAt(0))){
				int line = locateLineNumber(m.group());
				potentialBugs.put(matchName, line);
				doCheck();
			}	
		}
	}
	
	
	//TODO:usr better ways to locate
	private int locateLineNumber(String str){
		int index = source.indexOf(str);
		int lineNumber = 0;
		int size = 0;
		for(String line:sourceLines){
			size+=line.length();
			if(index<size)
				break;
			lineNumber++;
		}
		return lineNumber;
	}
	
	//TODO:use more accurate method to recognize class names
	private void doCheck(){
		for(String var : potentialBugs.keySet()){
			for(String className:classes.keySet()){
				
				if(className.endsWith("$"+var+"$")){	
					int line = potentialBugs.get(var)+1;
					bugReporter.reportBug(new BugInstance("FIRST_LETTER_LOWER_IN_MATCH",Priorities.NORMAL_PRIORITY).addClass(this).addSourceLine(new SourceLineAnnotation(getDottedClassName(),getSourceFile(),line,line,1,1)));
					potentialBugs.remove(var);
				}
			}
		}
	}
	

}
