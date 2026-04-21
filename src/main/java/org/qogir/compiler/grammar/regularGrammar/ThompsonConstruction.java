package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.FiniteAutomaton;
import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;

public class ThompsonConstruction {
    // ε 空转移常量
    public static final char EPSILON = 'ε';

    /**
     * 对外入口：正则语法树 -> TNFA
     */
    public TNFA regexToNFA(RegexTreeNode rootNode) {
        TNFA fullNfa = buildFromTree(rootNode);
        // 重新编排状态ID，保证输出美观、顺序统一
        // fullNfa.renumberSID();
        return fullNfa;
    }

    /**
     * 递归遍历语法树，按Thompson规则构造NFA片段
     */
    private TNFA buildFromTree(RegexTreeNode node) {
        if (node == null) return new TNFA();

        return switch (node.getType()) {
            // 节点0：基础字符
            case 0 -> buildSingleChar(node.getValue());
            // 节点1：隐式连接 Concat
            case 1 -> buildConcat(
                    buildFromTree((RegexTreeNode)node.getFirstChild()),
                    buildFromTree((RegexTreeNode)node.getLastChild())
            );
            // 节点2：或运算 |
            case 2 -> buildUnion(
                    buildFromTree((RegexTreeNode)node.getFirstChild()),
                    buildFromTree((RegexTreeNode)node.getLastChild())
            );
            // 节点3：克林闭包 *
            case 3 -> buildKleeneStar(buildFromTree((RegexTreeNode)node.getFirstChild()));
            default -> throw new RuntimeException("不支持的语法树节点类型: " + node.getType());
        };
    }

    // 1. 单个基础字符的最小NFA
    private TNFA buildSingleChar(char c) {
        TNFA nfa = new TNFA();
        State start = nfa.getStartState();
        State end = nfa.getAcceptingState();

        nfa.getTransitTable().addEdge(start, end, c);
        // System.out.println("addEdge:" + start + "-" + c + " -> " + end);
        return nfa;
    }

    // 2. 连接运算 AB
    private TNFA buildConcat(TNFA nfaA, TNFA nfaB) {
        TNFA res = new TNFA(); // 不自动新建首尾状态
        mergeAll(res, nfaA);
        mergeAll(res, nfaB);

        // A 的终点 ε 连接 B 的起点
        State AEnd = nfaA.getAcceptingState();
        AEnd.setType(State.MIDDLE); // 连接后不再是终点
        State BStart = nfaB.getStartState();
        BStart.setType(State.MIDDLE); // 连接后不再是起点
        res.getTransitTable().addEdge(
                AEnd,
                BStart,
                EPSILON
        );

        res.setStartState(nfaA.getStartState());
        res.setAcceptingState(nfaB.getAcceptingState());
        return res;
    }

    // 3. 或运算 A|B
    private TNFA buildUnion(TNFA nfaA, TNFA nfaB) {
        TNFA res = new TNFA();
        State newStart = res.getStartState();
        State newEnd = res.getAcceptingState();

        mergeAll(res, nfaA);
        mergeAll(res, nfaB);

        // 新起点 ε 分别指向两个子NFA起点
        State AStart = nfaA.getStartState();
        AStart.setType(State.MIDDLE);
        State BStart = nfaB.getStartState();
        BStart.setType(State.MIDDLE);
        res.getTransitTable().addEdge(newStart, AStart, EPSILON);
        res.getTransitTable().addEdge(newStart, BStart, EPSILON);

        // 两个子NFA终点 ε 统一指向新终点
        State AEnd = nfaA.getAcceptingState();
        AEnd.setType(State.MIDDLE);
        State BEnd = nfaB.getAcceptingState();
        BEnd.setType(State.MIDDLE);
        res.getTransitTable().addEdge(AEnd, newEnd, EPSILON);
        res.getTransitTable().addEdge(BEnd, newEnd, EPSILON);

        return res;
    }

    // 4. 克林闭包 A*
    private TNFA buildKleeneStar(TNFA nfaA) {
        TNFA res = new TNFA();
        State newStart = res.getStartState();
        State newEnd = res.getAcceptingState();

        mergeAll(res, nfaA);

        State AStart = nfaA.getStartState();
        AStart.setType(State.MIDDLE);
        State AEnd = nfaA.getAcceptingState();
        AEnd.setType(State.MIDDLE);
        // Thompson闭包标准4条ε边
        res.getTransitTable().addEdge(newStart, AStart, EPSILON);
        res.getTransitTable().addEdge(newStart, newEnd, EPSILON);
        res.getTransitTable().addEdge(AEnd, AStart, EPSILON);
        res.getTransitTable().addEdge(AEnd, newEnd, EPSILON);

        return res;
    }

    /**
     * 工具：把源自动机的所有状态、转移边完整合并到目标自动机
     */
    private void mergeAll(TNFA target, FiniteAutomaton source) {
        var targetTable = target.getTransitTable();
        var sourceTable = source.getTransitTable();

        // 复制所有状态顶点
        for (State state : sourceTable.vertexSet()) {
            targetTable.addVertex(state);
        }

        // 复制全部转移边
        for (LabelEdge e : sourceTable.edgeSet()) {
            State s = (State) e.getSource();
            State t = (State) e.getTarget();
            Character label = e.getLabel();
            targetTable.addEdge(s, t, label);
        }
    }

    // =============================================
    // ✅ 打印方法：完全和课件截图格式一模一样
    // =============================================
    public void printNfaFormat(TNFA nfa) {
        // 1. 打印开始状态
        System.out.println("Start State:" + nfa.getStartState().getId());
        // 2. 打印表头
        System.out.println("the transitTable is:");

        // 3. 遍历所有边，严格输出 (源ID:源类型->目标ID:目标类型 @ 字符)
        var table = nfa.getTransitTable();
        for (LabelEdge edge : table.edgeSet()) {
            State src = (State) edge.getSource();
            State dst = (State) edge.getTarget();
            char label = edge.getLabel();

            String sym = (label == EPSILON) ? "ε" : String.valueOf(label);

            // 格式：(源ID:源类型->目标ID:目标类型 @ 符号)
            System.out.printf(
                    "(%d:%d->%d:%d @ %s)\n",
                    src.getId(),
                    src.getType(),
                    dst.getId(),
                    dst.getType(),
                    sym
            );
        }
    }
}