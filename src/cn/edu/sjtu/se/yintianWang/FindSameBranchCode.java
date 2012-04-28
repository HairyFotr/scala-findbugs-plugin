/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2005 Dave Brosius <dbrosius@users.sourceforge.net>
 * Copyright (C) 2005-2006 University of Maryland
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
package cn.edu.sjtu.se.yintianWang;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.TABLESWITCH;
import org.apache.bcel.util.ByteSequence;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.SystemProperties;
import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Edge;
import edu.umd.cs.findbugs.ba.EdgeTypes;
import edu.umd.cs.findbugs.ba.MethodUnprofitableException;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
 * @author davidwyt
 */
public class FindSameBranchCode extends PreorderVisitor implements Detector {
    private ClassContext classContext;

    private BugReporter bugReporter;

    private Collection<BugInstance> pendingBugs = new LinkedList<BugInstance>();

    public FindSameBranchCode(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    public void visitClassContext(ClassContext classContext) {
        this.classContext = classContext;
        classContext.getJavaClass().accept(this);
    }

    @Override
    public void visitMethod(Method method) {
        try {
            if (method.getCode() == null)
                return;

            CFG cfg = classContext.getCFG(method);

            Iterator<BasicBlock> bbi = cfg.blockIterator();
            while (bbi.hasNext()) {
                BasicBlock bb = bbi.next();

                int numOutgoing = cfg.getNumOutgoingEdges(bb);
                if (numOutgoing == 2)
                    findIfBranchSameCode(cfg, method, bb);
                else if (numOutgoing > 2)
                    findMatchBranchSameCode(cfg, method, bb);
            }
        } catch (Exception e) {
            bugReporter.logError("Failure examining basic blocks in same branch code detector", e);
        }
        if (pendingBugs.size() <= 2)
            for (BugInstance b : pendingBugs)
                bugReporter.reportBug(b);
        pendingBugs.clear();

    }

    private void findIfBranchSameCode(CFG cfg, Method method, BasicBlock bb) {
        BasicBlock thenBB = null, elseBB = null;

        Iterator<Edge> iei = cfg.outgoingEdgeIterator(bb);
        while (iei.hasNext()) {
            Edge e = iei.next();
            if (e.getType() == EdgeTypes.IFCMP_EDGE) {
                elseBB = e.getTarget();
            } else if (e.getType() == EdgeTypes.FALL_THROUGH_EDGE) {
                thenBB = e.getTarget();
            }
        }

        if ((thenBB == null) || (elseBB == null))
            return;

        InstructionHandle thenStartHandle = getFirstInstruction(cfg, thenBB);
        InstructionHandle elseStartHandle = getFirstInstruction(cfg, elseBB);
        if ((thenStartHandle == null) || (elseStartHandle == null))
            return;

        int thenStartPos = thenStartHandle.getPosition();
        int elseStartPos = elseStartHandle.getPosition();

        InstructionHandle thenFinishIns = findThenFinish(cfg, thenBB, elseStartPos);
        int thenFinishPos = thenFinishIns.getPosition();

        if (!(thenFinishIns.getInstruction() instanceof GotoInstruction))
            return;

        InstructionHandle elseFinishHandle = ((GotoInstruction) thenFinishIns.getInstruction()).getTarget();
        int elseFinishPos = elseFinishHandle.getPosition();

        if (thenFinishPos >= elseStartPos)
            return;

        if ((thenFinishPos - thenStartPos) != (elseFinishPos - elseStartPos))
            return;

        if (thenFinishPos <= thenStartPos)
            return;

        byte[] thenBytes = getCodeBytes(method, thenStartPos, thenFinishPos);
        byte[] elseBytes = getCodeBytes(method, elseStartPos, elseFinishPos);

        if (!Arrays.equals(thenBytes, elseBytes))
            return;

        // adjust elseFinishPos to be inclusive (for source line attribution)
        InstructionHandle elseLastIns = elseFinishHandle.getPrev();
        if (elseLastIns != null)
            elseFinishPos = elseLastIns.getPosition();

        pendingBugs.add(new BugInstance(this, "SCALA_SBC_SAME_IFBRANCH_CODE", NORMAL_PRIORITY)
                .addClassAndMethod(classContext.getJavaClass(), method)
                .addSourceLineRange(classContext, this, thenStartPos, thenFinishPos)
                .addSourceLineRange(classContext, this, elseStartPos, elseFinishPos));
    }

    /**
     * Like bb.getFirstInstruction() except that if null is returned it will
     * follow the FALL_THROUGH_EDGE (if any)
     */
    private static InstructionHandle getFirstInstruction(CFG cfg, BasicBlock bb) {
        InstructionHandle ih = bb.getFirstInstruction();
        if (ih != null)
            return ih;
        Iterator<Edge> iei = cfg.outgoingEdgeIterator(bb);
        while (iei.hasNext()) {
            Edge e = iei.next();
            if (EdgeTypes.FALL_THROUGH_EDGE == e.getType())
                return getFirstInstruction(cfg, e.getTarget());
        }
        return null;
    }

    private void findMatchBranchSameCode(CFG cfg, Method method, BasicBlock bb) {

        int[] matchPos = new int[cfg.getNumOutgoingEdges(bb) + 1];
        HashMap<Integer, InstructionHandle> prevHandle = new HashMap<Integer, InstructionHandle>();

        Iterator<Edge> iei = cfg.outgoingEdgeIterator(bb);
        int idx = 0;

        while (iei.hasNext()) {
            Edge e = iei.next();
            int eType = e.getType();
            if (eType == EdgeTypes.SWITCH_EDGE || eType == EdgeTypes.SWITCH_DEFAULT_EDGE) {
                BasicBlock target = e.getTarget();
                InstructionHandle firstIns = getFirstInstruction(cfg, target);
                if (firstIns == null)
                    continue; // give up on this edge
                int firstInsPosition = firstIns.getPosition();
                matchPos[idx++] = firstInsPosition;
                InstructionHandle prevIns = firstIns.getPrev();

                if (prevIns != null)
                    prevHandle.put(firstInsPosition, prevIns);
            } else {
                // this must not be a match statement, so give up
                return;
            }
        }

        if (idx < 2) // need at least two edges
            return;

        Arrays.sort(matchPos, 0, idx); // sort the 'idx' match positions

        // compute end position of final case (ok if set to 0 or <=
        // matchPos[idx-1])
        matchPos[idx] = getFinalTarget(cfg, matchPos[idx - 1], prevHandle.values());

        HashMap<BigInteger, Collection<Integer>> map = new HashMap<BigInteger, Collection<Integer>>();
        for (int i = 0; i < idx; i++) {

            int endPos = matchPos[i + 1];
            InstructionHandle last = prevHandle.get(matchPos[i + 1]);
            if (last == null) {
                // should be default case -- leave endPos as is
            } else if (last.getInstruction() instanceof GotoInstruction) {
                endPos = last.getPosition(); // don't store the goto
            } else if (last.getInstruction() instanceof ReturnInstruction) {
                // leave endPos as is (store the return instruction)
                // } else if (last.getInstruction() instanceof ATHROW) {
                // // leave endPos as is (store the throw instruction)
                // Don't do this since many cases may throw "not implemented".
            } else {
                if (i + 2 < idx)
                    continue; // falls through to next case, so don't store it
                              // at all
                if (i + 1 < idx && matchPos[idx] != matchPos[idx - 1])
                    continue; // also falls through unless match has no default
                              // case
            }

            BigInteger clauseAsInt = getCodeBytesAsBigInt(method, matchPos, i, endPos);
            updateMap(map, i, clauseAsInt);

        }
        for (Collection<Integer> clauses : map.values()) {
            if (clauses.size() > 1) {
                BugInstance bug = new BugInstance(this, "SCALA_SBC_SAME_MATCHBRANCH_CODE", NORMAL_PRIORITY).addClassAndMethod(
                        classContext.getJavaClass(), method);
                for (int i : clauses)
                    bug.addSourceLineRange(this.classContext, this, matchPos[i], matchPos[i + 1] - 1); // not
                                                                                                         // endPos,
                pendingBugs.add(bug);
            }
        }
    }

    private void updateMap(HashMap<BigInteger, Collection<Integer>> map, int i, BigInteger clauseAsInt) {
        Collection<Integer> values = map.get(clauseAsInt);

        if (values == null) {
            values = new LinkedList<Integer>();
            map.put(clauseAsInt, values);
        }
        values.add(i); // index into the sorted array
    }

    private BigInteger getCodeBytesAsBigInt(Method method, int[] matchPos, int i, int endPos) {
        byte[] clause = getCodeBytes(method, matchPos[i], endPos);

        BigInteger clauseAsInt;
        if (clause.length == 0)
            clauseAsInt = BigInteger.ZERO;
        else
            clauseAsInt = new BigInteger(clause);
        return clauseAsInt;
    }

    /**
     * determine the end position (exclusive) of the final case by looking at
     * the gotos at the ends of the other cases
     */
    private static int getFinalTarget(CFG cfg, int myPos, Collection<InstructionHandle> prevs) {
        int maxGoto = 0;
        BasicBlock myBB = null;
        // note: InstructionHandle doesn't override equals(), so use
        // prevs.contains() with caution.
        Iterator<BasicBlock> bbi = cfg.blockIterator();
        while (bbi.hasNext()) {
            BasicBlock bb = bbi.next();
            InstructionHandle last = bb.getLastInstruction(); // may be null
            if (prevs.contains(last)) { // danger will robinson
                Iterator<Edge> iei = cfg.outgoingEdgeIterator(bb);
                while (iei.hasNext()) {
                    Edge e = iei.next();
                    int eType = e.getType();
                    if (eType == EdgeTypes.GOTO_EDGE) {
                        BasicBlock target = e.getTarget();
                        InstructionHandle targetFirst = getFirstInstruction(cfg, target);
                        if (targetFirst != null) {
                            int targetPos = targetFirst.getPosition();
                            if (targetPos > maxGoto)
                                maxGoto = targetPos;
                        }
                    }
                }
            } else if (last != null && myPos == bb.getFirstInstruction().getPosition()) {
                // note: getFirstInstruction() may return null, but won't if
                // last!=null.
                myBB = bb; // used in case (c) below
            }
        }
        /*
         * ok, there are three cases: a) maxGoto == myPos: There is no default
         * case within the match. b) maxGoto > myPos: maxGoto is the end
         * (exclusive) of the default case c) maxGoto < myPos: all the cases do
         * something like return or throw, so who knows if there is a default
         * case (and it's length)?
         */
        if (maxGoto < myPos && myBB != null) {
            /*
             * We're in case (c), so let's guess that the end of the basic block
             * is the end of the default case (if it exists). This may give
             * false negatives (if the default case has branches, for example)
             * but shouldn't give false negatives (because if it matches one of
             * the cases, then it has also matched that case's return/throw).
             */
            InstructionHandle last = myBB.getLastInstruction();
            if (last != null) {
                // note: last.getNext() may return null, so do it this way
                return last.getPosition() + last.getInstruction().getLength();
            }
        }
        return maxGoto;
    }

    private byte[] getCodeBytes(Method m, int start, int end) {
        byte[] code = m.getCode().getCode();
        byte[] bytes = new byte[end - start];
        System.arraycopy(code, start, bytes, 0, end - start);

        try {
            ByteSequence sequence = new ByteSequence(code);
            while ((sequence.available() > 0) && (sequence.getIndex() < start)) {
                Instruction.readInstruction(sequence);
            }

            int pos;
            while (sequence.available() > 0 && ((pos = sequence.getIndex()) < end)) {
                Instruction ins = Instruction.readInstruction(sequence);
                if ((ins instanceof BranchInstruction) && !(ins instanceof TABLESWITCH) && !(ins instanceof LOOKUPSWITCH)) {
                    BranchInstruction bi = (BranchInstruction) ins;
                    int offset = bi.getIndex();
                    int target = offset + pos;
                    if (target >= end) { // or target < start ??
                        byte hiByte = (byte) ((target >> 8) & 0x000000FF);
                        byte loByte = (byte) (target & 0x000000FF);
                        bytes[pos + bi.getLength() - 2 - start] = hiByte;
                        bytes[pos + bi.getLength() - 1 - start] = loByte;
                    }
                }
            }
        } catch (IOException ioe) {
        }

        return bytes;
    }

    private InstructionHandle findThenFinish(CFG cfg, BasicBlock thenBB, int elsePos) {
        InstructionHandle inst = thenBB.getFirstInstruction();
        while (inst == null) {
            Iterator<Edge> ie = cfg.outgoingEdgeIterator(thenBB);
            while (ie.hasNext()) {
                Edge e = ie.next();
                if (e.getType() == EdgeTypes.FALL_THROUGH_EDGE) {
                    thenBB = e.getTarget();
                    break;
                }
            }
            inst = thenBB.getFirstInstruction();
        }

        InstructionHandle lastIns = inst;
        while (inst.getPosition() < elsePos) {
            lastIns = inst;
            inst = inst.getNext();
        }

        return lastIns;
    }

    public void report() {
        // TODO Auto-generated method stub

    }
}
