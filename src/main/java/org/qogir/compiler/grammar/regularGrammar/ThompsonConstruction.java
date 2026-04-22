package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;

import java.util.ArrayDeque;
import java.util.Set;

import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.tree.DefaultTreeNode;

import com.alibaba.fastjson.asm.Label;

public class ThompsonConstruction {

    public TNFA translate(RegexTreeNode node, RegexTreeNode root) {
        if (node == null) return null;

        TNFA tnfa=new TNFA();
        //Add your implementation
        tnfa=build(node);

        return tnfa;
    }
    private TNFA build(RegexTreeNode node) {
        switch (node.getType()) {
            case 0: // 基本字符
                return buildBasic(node);
            case 1: // 连接（多个子表达式）
                return buildConcat(node);
            case 2: // 并集（多个分支）
                return buildUnion(node);
            case 3: // 闭包
                return buildStar(node);
            default:
                throw new IllegalArgumentException("Unsupported node type: " + node.getType());
        }
    }

    // 基本字符：a 或 ε
    private TNFA buildBasic(RegexTreeNode node) {
        char ch = node.getValue();
        State start = new State();          // 新建起始状态
        State accept = new State();          // 新建接受状态
        accept.setType(State.ACCEPT);
        start.setType(State.START);

        TNFA tnfa = new TNFA(accept);        // 使用外部接受状态构造 TNFA
        tnfa.setStartState(start);
        tnfa.getTransitTable().addVertex(start);

        // 添加转移边：起始 -> 接受
        tnfa.getTransitTable().addEdge(start, accept, ch);
        return tnfa;
    }

    // 连接：依次串联所有子节点（如 abc）
    private TNFA buildConcat(RegexTreeNode node) {
        RegexTreeNode child =  (RegexTreeNode)node.getFirstChild();
        if (child == null) return null;

        TNFA first = build(child);           // 第一个子表达式的 NFA
        child = (RegexTreeNode)child.getNextSibling();

        while (child != null) {
            TNFA next = build(child);
            // 将 first 的接受状态与 next 的起始状态用 ε 边连接
            first.getTransitTable().addEdge(first.getAcceptingState(),
                                            next.getStartState(),
                                            'ε');
            // 合并 next 的所有状态和边到 first 中
            mergeTNFA(first, next);
            // 更新 first 的接受状态为 next 的接受状态
            first.getAcceptingState().setType(State.MIDDLE);
            first.setAcceptingState(next.getAcceptingState());
            child = (RegexTreeNode)child.getNextSibling();
        }
        return first;
    }

    // 并集：并联所有分支（如 a|b|c）
    private TNFA buildUnion(RegexTreeNode node) {
        State newStart = new State();
        State newAccept = new State();
        newAccept.setType(State.ACCEPT);
        newStart.setType(State.START);
        
        TNFA result = new TNFA(newAccept);
        result.setStartState(newStart);
        result.getTransitTable().addVertex(newStart);
        result.getTransitTable().addVertex(newAccept);

        RegexTreeNode child = (RegexTreeNode)node.getFirstChild();
        while (child != null) {
            TNFA branch = build(child);
            // 从新起始状态 ε 到分支起始
            result.getTransitTable().addEdge(newStart, branch.getStartState(), 'ε');
            // 从分支接受状态 ε 到新接受状态
            result.getTransitTable().addEdge(branch.getAcceptingState(), newAccept, 'ε');
            // 合并分支的状态和边
            mergeTNFA(result, branch);
            branch.getAcceptingState().setType(State.MIDDLE);
            child = (RegexTreeNode)child.getNextSibling();
        }
        return result;
    }

    // 闭包：Kleene 星号（*）
    private TNFA buildStar(RegexTreeNode node) {
        RegexTreeNode innerNode = (RegexTreeNode)node.getFirstChild();
        if (innerNode == null) return null;

        TNFA inner = build(innerNode);
        State newStart = new State();
        State newAccept = new State();
        newAccept.setType(State.ACCEPT);
        
        newStart.setType(State.START);

        TNFA result = new TNFA(newAccept);
        result.setStartState(newStart);
        result.getTransitTable().addVertex(newStart);
        result.getTransitTable().addVertex(newAccept);

        // 1) 新起始 -> 内部起始
        result.getTransitTable().addEdge(newStart, inner.getStartState(), 'ε');
        // 2) 内部接受 -> 内部起始（循环）
        result.getTransitTable().addEdge(inner.getAcceptingState(), inner.getStartState(), 'ε');
        // 3) 内部接受 -> 新接受
        result.getTransitTable().addEdge(inner.getAcceptingState(), newAccept, 'ε');
        // 4) 新起始 -> 新接受（零次重复）
        result.getTransitTable().addEdge(newStart, newAccept, 'ε');

        // 合并内部 NFA 的状态和边
        mergeTNFA(result, inner);
        inner.getAcceptingState().setType(State.MIDDLE);
        return result;
    }

    // 将 src NFA 的所有状态和边合并到 dest NFA 中
    private void mergeTNFA(TNFA dest, TNFA src) {
        LabeledDirectedGraph<State> destGraph = dest.getTransitTable();
        LabeledDirectedGraph<State> srcGraph = src.getTransitTable();

        // 合并状态
        for (State s:srcGraph.vertexSet()) {
            if (!destGraph.containsVertex(s)) {
                destGraph.addVertex(s);
            }
            if(s.getType()==State.START)s.setType(State.MIDDLE);
        }
        // 合并边
        for (LabelEdge from : srcGraph.edgeSet()) {
            destGraph.addEdge((State)from.getSource(), (State)from.getTarget(), from.getLabel());
        }
    }
}
