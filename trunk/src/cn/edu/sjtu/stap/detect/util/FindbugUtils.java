package cn.edu.sjtu.stap.detect.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.ba.SourceFinder;
import edu.umd.cs.findbugs.util.Util;

public class FindbugUtils {
	public static String getSourceFileContent(Project p,String packageName,String fileName){
		SourceFinder sf = new SourceFinder(p);
		BufferedReader rd;
		try {
			rd = new BufferedReader(Util.getReader(sf.findSourceFile(packageName,fileName).getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line=rd.readLine())!=null)
				sb.append(line).append("\n");
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
