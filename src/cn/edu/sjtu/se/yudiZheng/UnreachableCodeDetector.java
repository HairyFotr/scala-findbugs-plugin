/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package cn.edu.sjtu.se.yudiZheng;

import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import cn.edu.sjtu.se.yudiZheng.cfg.BasicBlock;
import cn.edu.sjtu.se.yudiZheng.cfg.CtrlFlowGraph;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.visitclass.BetterVisitor;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
 * @author Yugi
 */
public class UnreachableCodeDetector extends PreorderVisitor implements Detector {

    private BugReporter reporter;

    public UnreachableCodeDetector(BugReporter bugReporter) {
        reporter = bugReporter;
    }

    public void visitClassContext(ClassContext classContext) {

        ClassReader cr = new ClassReader(classContext.getJavaClass().getBytes());
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        for (MethodNode method : (List<MethodNode>) cn.methods) {

            CtrlFlowGraph cfg = CtrlFlowGraph.build(method);
            List<BasicBlock> connected = new LinkedList<BasicBlock>(cfg.getNodes());

            connected.remove(cfg.getBB(method.instructions.getFirst()));

            for (TryCatchBlockNode tcb : (List<TryCatchBlockNode>) method.tryCatchBlocks) {
                connected.remove(cfg.getBB(tcb.handler));
            }

            for (BasicBlock bb : connected) {

                if (bb.getPredecessors().size() == 0) {
                    reporter.reportBug(new BugInstance(this, "UNREACHABLE", NORMAL_PRIORITY).addClass(cn).addMethod(cn.name,
                            method.name, method.desc, (method.access & Opcodes.ACC_STATIC) != 0));
                }
            }
        }
    }

    public void report() {
    }

}
