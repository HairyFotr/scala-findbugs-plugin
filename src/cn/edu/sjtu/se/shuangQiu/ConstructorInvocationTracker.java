/*
 * ConstructorInvocationTracker.java
 *   record all methods invoked in this class, we use a set
 * recording all overloaded constructor and their invokation sequence
 * internally.
 *
 * author: qiushuang
 */
package cn.edu.sjtu.se.shuangQiu;

import java.util.HashSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

class ConstructorInvocationTracker {
    /* constructor */
    public ConstructorInvocationTracker(JavaClass javaClass) {
        this.javaClass = javaClass;
        tracker = new HashSet<String>();
    }

    /* query interface -- get method object from method signature */
    public boolean doesMethodInvoked(String method) {
        return tracker.contains(method);
    }

    /* record invoked method */
    public void methodInvoke(String method) {
        tracker.add(method);
    }

    private JavaClass javaClass;              /* class to record */
    private HashSet<String> tracker;          /* invoked instance methods */
}