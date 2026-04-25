package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;
import org.qogir.simulation.logger.SubsetConsLogger;

import java.util.*;

/**
 * 子集构造算法：将NFA转换为等价的DFA
 * 核心目标：消除NFA的ε-转移（空转移）和多转移特性，生成满足DFA定义的转移表
 */
public class SubsetConstruction {

    /**
     * 单状态的ε-闭包：求从单个NFA状态出发，仅通过ε-转移能到达的所有状态
     * @param s NFA的单个状态
     * @param tb NFA的转移表（有向带标签图）
     * @return 该状态的ε-闭包状态集合（Key：状态ID，Value：状态对象）
     */
    private HashMap<Integer, State> epsilonClosures(State s, LabeledDirectedGraph<State> tb){
        if (!tb.vertexSet().contains(s)) { // 若状态不在转移表中，返回null
            return null;
        }

        HashMap<Integer,State> nfaStates = new HashMap<>();
        // 广度优先搜索（BFS）遍历所有ε-可达状态
        Queue<State> queue = new LinkedList<>();
        Set<State> visited = new HashSet<>();

        queue.offer(s); // 初始状态入队
        visited.add(s);
        nfaStates.put(s.getId(), s);

        while (!queue.isEmpty()) {
            State current = queue.poll();
            // 遍历当前状态的所有出边，筛选ε-转移边
            for (LabelEdge edge : tb.edgeSet()) {
                if (edge.getSource().equals(current) && edge.getLabel() == 'ε') {
                    State target = (State) edge.getTarget();
                    if (!visited.contains(target)) { // 避免重复访问
                        visited.add(target);
                        queue.offer(target);
                        nfaStates.put(target.getId(), target);
                    }
                }
            }
        }
        return nfaStates;
    }

    /**
     * 状态集合的ε-闭包：求从一组NFA状态出发，仅通过ε-转移能到达的所有状态
     * @param ss NFA的状态集合
     * @param tb NFA的转移表
     * @return 该状态集合的ε-闭包
     */
    public HashMap<Integer, State> epsilonClosure(HashMap<Integer, State> ss, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        for(State s : ss.values()){
            // 合并每个单状态的ε-闭包
            nfaStates.putAll(epsilonClosures(s,tb));
        }
        return nfaStates;
    }

    /**
     * 单状态的move操作：求从单个NFA状态出发，通过输入字符ch转移能到达的所有状态（无ε-转移）
     * @param s NFA单个状态
     * @param ch 输入字符
     * @param tb NFA转移表
     * @return move操作结果集合
     */
    private HashMap<Integer,State> moves(State s, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        // 遍历所有边，筛选“当前状态+指定字符”的转移边
        for (LabelEdge edge : tb.edgeSet()) {
            if (edge.getSource().equals(s) && edge.getLabel() == ch) {
                State target = (State) edge.getTarget();
                nfaStates.put(target.getId(), target);
            }
        }
        return nfaStates;
    }

    /**
     * 状态集合的move操作：求从一组NFA状态出发，通过输入字符ch转移能到达的所有状态（无ε-转移）
     * @param ss NFA状态集合
     * @param ch 输入字符
     * @param tb NFA转移表
     * @return move操作结果集合
     */
    public HashMap<Integer,State> move(HashMap<Integer, State> ss, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        for(State s : ss.values()){
            // 合并每个单状态的move结果
            nfaStates.putAll(moves(s,ch,tb));
        }
        return nfaStates;
    }

    /**
     * 组合操作：先执行move，再执行ε-闭包（子集构造的核心操作）
     * @param sSet NFA状态集合
     * @param ch 输入字符
     * @param tb NFA转移表
     * @return move+ε-闭包的最终状态集合
     */
    public HashMap<Integer,State> epsilonClosureWithMove(HashMap<Integer, State> sSet, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> states = new HashMap<>();
        // 先move：按字符转移；再ε-闭包：补全所有ε-可达状态
        states.putAll(epsilonClosure(move(sSet, ch, tb),tb));
        return states;
    }

    /**
     * 子集构造主流程：将TNFA（带ε的NFA）转换为RDFA（简化DFA）
     * @param tnfa 输入的NFA（含ε-转移）
     * @return 等价的DFA
     */
    public RDFA subSetConstruct(TNFA tnfa){
        RDFA dfa = new RDFA();
        LabeledDirectedGraph<State> nfaTable = tnfa.getTransitTable(); // NFA转移表
        ArrayList<Character> alphabet = tnfa.getAlphabet(); // NFA的输入字母表

        // 映射：NFA状态集合 → 对应的DFA状态（核心映射，解决“DFA状态对应NFA状态子集”的问题）
        HashMap<HashMap<Integer, State>, State> nfaSetToDfaState = new HashMap<>();
        // 工作队列：待处理的NFA状态集合（BFS遍历所有可能的DFA状态）
        Queue<HashMap<Integer, State>> worklist = new LinkedList<>();

        // 步骤1：初始化DFA起始状态（NFA起始状态的ε-闭包）
        HashMap<Integer, State> startNfaSet = epsilonClosures(tnfa.getStartState(), nfaTable);
        State dfaStart = new State();
        dfaStart.setType(State.START); // 标记为DFA起始状态
        dfa.setStartState(dfaStart);
        dfa.getTransitTable().addVertex(dfaStart); // 将起始状态加入DFA转移表
        dfa.setStateMappingBetweenDFAAndNFA(dfaStart, startNfaSet); // 记录DFA状态与NFA子集的映射
        nfaSetToDfaState.put(startNfaSet, dfaStart);
        worklist.offer(startNfaSet); // 初始状态集合入队

        int dfaStateCounter = 1; // DFA状态ID计数器

        // 步骤2：处理工作队列，生成所有DFA状态和转移
        while (!worklist.isEmpty()) {
            HashMap<Integer, State> currentNfaSet = worklist.poll(); // 当前处理的NFA子集
            State currentDfaState = nfaSetToDfaState.get(currentNfaSet); // 对应的DFA状态

            // 遍历字母表中的每个字符，生成DFA转移
            for (Character ch : alphabet) {
                // 计算：ε-closure(move(当前NFA子集, ch)) → 下一个NFA子集
                HashMap<Integer, State> nextNfaSet = epsilonClosureWithMove(currentNfaSet, ch, nfaTable);

                if (nextNfaSet.isEmpty()) { // 无转移，跳过
                    continue;
                }

                // 检查该NFA子集是否已对应DFA状态
                State nextDfaState;
                if (!nfaSetToDfaState.containsKey(nextNfaSet)) {
                    // 未存在：创建新的DFA状态
                    nextDfaState = new State();
                    // 标记状态类型：若NFA子集含接受态，则DFA状态为接受态
                    int type = containsAcceptingState(nextNfaSet) ? State.ACCEPT : State.MIDDLE;
                    nextDfaState.setType(type);
                    dfa.getTransitTable().addVertex(nextDfaState); // 加入DFA转移表
                    dfa.setStateMappingBetweenDFAAndNFA(nextDfaState, nextNfaSet); // 记录映射
                    nfaSetToDfaState.put(nextNfaSet, nextDfaState);
                    worklist.offer(nextNfaSet); // 新状态子集入队待处理
                } else {
                    // 已存在：复用已有DFA状态
                    nextDfaState = nfaSetToDfaState.get(nextNfaSet);
                }

                // 为DFA添加转移边：当前DFA状态 → 下一个DFA状态，输入字符ch
                dfa.getTransitTable().addEdge(currentDfaState, nextDfaState, ch);

                // 记录转移日志（调试/可视化用）
                SubsetConsLogger.getSubsetConsLogger(dfa, ch, currentDfaState, nextNfaSet);
            }
        }

        // 重新编号DFA状态ID（保证ID从0开始连续）
        dfa.renumberSID();
        System.out.println(dfa.StateMappingBetweenDFAAndNFAToString());
        return dfa;
    }

    /**
     * 检查NFA状态集合是否包含接受态
     * @param nfaSet NFA状态集合
     * @return 包含接受态返回true，否则false
     */
    private boolean containsAcceptingState(HashMap<Integer, State> nfaSet) {
        for (State s : nfaSet.values()) {
            if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                return true;
            }
        }
        return false;
    }

}
