/**
 * Category: Dodgy
 * Description: ѭ��������Circular Dependencies��
 * Details: 
 *   ��������ģ�飨�ࡢ���Ӧ�ó���ȣ�ֱ�ӵػ��ӵ��໥��������Aֱ�ӵػ��ӵ�����B��ͬʱBҲֱ�ӵػ��ӵ�����A��
 */

package cn.edu.sjtu.se.zilongWang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class CircularDependenciesDetector extends BytecodeScanningDetector 
{
    private HashMap<String, Set<String>> dependencyGraph = null;

    private BugReporter bugReporter;

    private String className;

    public CircularDependenciesDetector(BugReporter bugReporter) 
	{
        this.bugReporter = bugReporter;
        this.dependencyGraph = new HashMap<String, Set<String>>();
    }

    @Override
    public void visit(JavaClass obj) 
	{
		//��ȡ����
        className = obj.getClassName();
		//System.out.println(className);
    }

    @Override
    public void sawOpcode(int seen) 
	{
        if ((seen == INVOKESPECIAL) || (seen == INVOKESTATIC) || (seen == INVOKEVIRTUAL)) 
		{
			//��ȡ��������
            String refclassName = getClassConstantOperand();
            refclassName = refclassName.replace('/', '.');
            if (refclassName.startsWith("java"))
                return;
				
			if (refclassName.startsWith("scala"))
                return;
				
            if (className.equals(refclassName))
                return;

            //��ȡ��ǰclassName������ͼ�е�������
			Set<String> dependencies = dependencyGraph.get(className);
            
			//�������ͼ��û�е�ǰclassName
			if (dependencies == null) 
			{
				//��������������ͼ��
                dependencies = new HashSet<String>();
                dependencyGraph.put(className, dependencies);
            }

            //����������
			dependencies.add(refclassName);
        }
    }

    @Override
    public void report() 
	{
        //�������ͼ��Ҷ����
		removeDependencyLeaves(dependencyGraph);

        LoopFinder lf = new LoopFinder();

        while (dependencyGraph.size() > 0) 
		{
            String className = dependencyGraph.keySet().iterator().next();
			
			//Ѱ�һ�·
            Set<String> loop = lf.findLoop(dependencyGraph, className);
            boolean pruneLeaves;
			
			//�����ڻ�·
            if (loop != null) 
			{
				//����bug
                BugInstance bug = new BugInstance(this, "CIRCULAR_DEPENDENCY", NORMAL_PRIORITY);
                for (String loopCls : loop) 
				{
                    bug.addClass(loopCls);
                }
                bugReporter.reportBug(bug);
				
				//�Ƴ�·
                pruneLeaves = removeLoopLinks(dependencyGraph, loop);
            } 
			else 
			{
                dependencyGraph.remove(className);
                pruneLeaves = true;
            }
			
			//�����������ͼ��Ҷ����
            if (pruneLeaves)
                removeDependencyLeaves(dependencyGraph);
        }

        //�������ͼ���ͷ���Դ
		dependencyGraph.clear();
    }

	//�������ͼ��Ҷ����
    private void removeDependencyLeaves(Map<String, Set<String>> dependencyGraph) 
	{
        boolean changed = true;
        while (changed) 
		{
            changed = false;
            Iterator<Set<String>> it = dependencyGraph.values().iterator();
            while (it.hasNext()) 
			{
                Set<String> dependencies = it.next();

                boolean foundClass = false;
                Iterator<String> dit = dependencies.iterator();
                while (dit.hasNext()) 
				{
                    foundClass = dependencyGraph.containsKey(dit.next());
                    if (!foundClass) 
					{
                        dit.remove();
                        changed = true;
                    }
                }
                if (dependencies.size() == 0) 
				{
                    it.remove();
                    changed = true;
                }
            }
        }
    }

	//������ͼ���Ƴ�·
    private boolean removeLoopLinks(Map<String, Set<String>> dependencyGraph, Set<String> loop) 
	{
        Set<String> dependencies = null;
        for (String className : loop) 
		{
            if (dependencies != null)
                dependencies.remove(className);
            dependencies = dependencyGraph.get(className);
        }
        if (dependencies != null)
            dependencies.remove(loop.iterator().next());

        boolean removedClass = false;
        Iterator<String> cIt = loop.iterator();
        while (cIt.hasNext()) 
		{
            String className = cIt.next();
            dependencies = dependencyGraph.get(className);
            if (dependencies.size() == 0) 
			{
                cIt.remove();
                removedClass = true;
            }
        }
        return removedClass;
    }

    static class LoopFinder 
	{
        private Map<String, Set<String>> dGraph = null;

        private String startClass = null;

        private Set<String> visited = null;

        private Set<String> loop = null;

        public Set<String> findLoop(Map<String, Set<String>> dependencyGraph, String startCls) 
		{
            dGraph = dependencyGraph;
            startClass = startCls;
            visited = new HashSet<String>();
            loop = new LinkedHashSet<String>();
			//��startClass��ʼ���һ�·
            if (findLoop(startClass))
                return loop;
            return null;
        }

        private boolean findLoop(String curClass) 
		{
            Set<String> dependencies = dGraph.get(curClass);
            if (dependencies == null)
                return false;

            visited.add(curClass);
            loop.add(curClass);
            for (String depClass : dependencies) 
			{
				//�ҵ���·
                if (depClass.equals(startClass))
                    return true;

                if (visited.contains(depClass))
                    continue;
				
				//�ݹ����
                if (findLoop(depClass))
                    return true;
            }
            loop.remove(curClass);
            return false;
        }
    }
}
