package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;
import org.qogir.simulation.logger.StateMinLogger;

import java.util.*;

public class StateMinimization {

    /**
     * 区分DFA中的等价状态（核心算法：划分求精）
     * 核心逻辑：
     * 1. 初始划分：接受态 和 非接受态 分为两组
     * 2. 迭代求精：按“输入字符转移后的组ID”划分，直到无法再划分
     * @param dfa 输入的DFA
     * @return 划分后的等价状态组（Key：组ID，Value：组内状态集合）
     */
    public HashMap<Integer, HashMap<Integer, State>> distinguishEquivalentState(RDFA dfa) {
        // 存储状态组：Key=组ID，Value=组内状态（Key=状态ID，Value=状态对象）
        HashMap<Integer,HashMap<Integer, State>> groupSet = new HashMap<>();

        // 步骤1：初始划分——分离接受态和非接受态
        HashMap<Integer, State> acceptStates = new HashMap<>(); // 接受态集合
        HashMap<Integer, State> nonAcceptStates = new HashMap<>(); // 非接受态集合

        // 遍历DFA所有状态，按类型分组
        for (State s : dfa.getTransitTable().vertexSet()) {
            if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                acceptStates.put(s.getId(), s);
            } else {
                nonAcceptStates.put(s.getId(), s);
            }
        }

        // 将初始分组加入groupSet
        int groupId = 0;
        if (!acceptStates.isEmpty()) {
            groupSet.put(groupId++, acceptStates);
        }
        if (!nonAcceptStates.isEmpty()) {
            groupSet.put(groupId++, nonAcceptStates);
        }

        // 日志记录初始划分
        StateMinLogger logger = new StateMinLogger();
        logger.setFirstTwoSteps(groupSet);

        System.out.println("GroupSet.size:" + groupSet.size());
        printGroupSet(0, groupSet, "initial split"); // 打印初始划分

        // 步骤2：迭代求精划分（直到无法再划分）
        boolean changed = true; // 标记划分是否变化
        int step = 1; // 迭代步骤计数
        ArrayList<Character> alphabet = dfa.getAlphabet(); // DFA输入字母表

        // 最多迭代100次（防止死循环），且划分有变化时继续
        while (changed && step < 100) {
            changed = false;

            // 遍历字母表中的每个字符，尝试按该字符的转移划分
            for (Character ch : alphabet) {
                HashMap<Integer, HashMap<Integer, State>> newGroupSet = new HashMap<>(); // 新的划分结果
                int newGroupId = 0;

                // 遍历当前所有状态组
                for (Integer gId : groupSet.keySet()) {
                    HashMap<Integer, State> group = groupSet.get(gId); // 当前处理的状态组

                    // 映射：状态ID → 该状态按ch转移后的目标组ID
                    HashMap<Integer, Integer> stateToTargetGroup = new HashMap<>();
                    for (Integer stateId : group.keySet()) {
                        State s = group.get(stateId);
                        // 找到状态s按ch转移后的目标状态所属的组ID
                        Integer targetGroupId = findTargetGroup(dfa, s, ch, groupSet);
                        stateToTargetGroup.put(stateId, targetGroupId);
                    }

                    // 按“转移后的组ID”划分当前组为子组
                    HashMap<Integer, HashMap<Integer, State>> subGroups = new HashMap<>();
                    for (Integer stateId : group.keySet()) {
                        Integer targetGroupId = stateToTargetGroup.get(stateId);
                        // 按目标组ID分组
                        if (!subGroups.containsKey(targetGroupId)) {
                            subGroups.put(targetGroupId, new HashMap<>());
                        }
                        subGroups.get(targetGroupId).put(stateId, group.get(stateId));
                    }

                    // 将子组加入新的划分结果
                    for (HashMap<Integer, State> subGroup : subGroups.values()) {
                        newGroupSet.put(newGroupId++, subGroup);
                    }

                    // 若当前组被划分成多个子组，标记划分有变化
                    if (subGroups.size() > 1) {
                        changed = true;
                    }
                }

                // 若划分有变化，更新groupSet并记录日志
                if (changed) {
                    groupSet = newGroupSet;
                    printGroupSet(step, groupSet, "end of_" + ch); // 打印本轮划分结果
                    logger.addStep(groupSet, ch.toString()); // 记录步骤
                    step++;
                    break; // 重新从字母表开头开始迭代（新划分需重新检查所有字符）
                }
            }
        }

        return groupSet;
    }

    /**
     * 查找状态s按字符ch转移后的目标状态所属的组ID
     * @param dfa 输入DFA
     * @param s 源状态
     * @param ch 输入字符
     * @param groupSet 当前的状态组划分
     * @return 目标状态的组ID（无转移返回-1）
     */
    private Integer findTargetGroup(RDFA dfa, State s, Character ch, HashMap<Integer, HashMap<Integer, State>> groupSet) {
        // 遍历所有边，找到s按ch转移的目标状态
        for (LabelEdge edge : dfa.getTransitTable().edgeSet()) {
            if (edge.getSource().equals(s) && edge.getLabel() == ch) {
                State target = (State) edge.getTarget();
                // 遍历所有组，找到包含目标状态的组ID
                for (Integer gId : groupSet.keySet()) {
                    if (groupSet.get(gId).containsKey(target.getId())) {
                        return gId;
                    }
                }
            }
        }
        // 无转移：返回-1作为特殊组ID
        return -1;
    }

    /**
     * 打印当前的状态组划分（调试用）
     * @param step 迭代步骤
     * @param groupSet 状态组划分
     * @param memo 备注（如“initial split”、“end of_a”）
     */
    private void printGroupSet(int step, HashMap<Integer, HashMap<Integer, State>> groupSet, String memo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Step").append(step).append(":  ");

        // 组ID排序，保证输出有序
        List<Integer> sortedKeys = new ArrayList<>(groupSet.keySet());
        Collections.sort(sortedKeys);

        for (Integer gId : sortedKeys) {
            HashMap<Integer, State> group = groupSet.get(gId);
            sb.append(gId).append(":").append(formatGroup(group)).append(" ");
        }
        sb.append(" :").append(memo);
        System.out.println(sb.toString());
    }

    /**
     * 格式化单个状态组为字符串（调试打印用）
     * @param group 状态组
     * @return 形如 {0:ACCEPT,1:MIDDLE} 的字符串
     */
    private String formatGroup(HashMap<Integer, State> group) {
        List<String> stateStrs = new ArrayList<>();
        for (State s : group.values()) {
            stateStrs.add(s.getId() + ":" + s.getType());
        }
        Collections.sort(stateStrs); // 排序保证输出有序
        return "{" + String.join(",", stateStrs) + "}";
    }

    /**
     * DFA最小化主流程：基于等价状态划分，构建最小DFA
     * @param dfa 输入的原始DFA
     * @return 最小化后的DFA
     */
    public RDFA minimize(RDFA dfa) {
        // 步骤1：获取等价状态组划分
        HashMap<Integer, HashMap<Integer, State>> groupSet = distinguishEquivalentState(dfa);

        // 步骤2：构建最小DFA
        RDFA minDfa = new RDFA();
        minDfa.setAlphabet(dfa.getAlphabet()); // 复用原DFA的字母表

        // 映射：组ID → 最小DFA中的代表状态
        HashMap<Integer, State> groupToMinState = new HashMap<>();
        // 映射：原DFA状态ID → 所属组ID
        HashMap<Integer, Integer> stateToGroup = new HashMap<>();

        // 初始化状态映射
        for (Integer gId : groupSet.keySet()) {
            for (Integer stateId : groupSet.get(gId).keySet()) {
                stateToGroup.put(stateId, gId);
            }
        }

        // 步骤3：为每个等价组创建最小DFA的状态
        int minStateId = 0;
        for (Integer gId : groupSet.keySet()) {
            HashMap<Integer, State> group = groupSet.get(gId);

            // 确定最小DFA状态的类型：组内有接受态则为接受态，否则为中间态
            int type = State.MIDDLE;
            for (State s : group.values()) {
                if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                    type = State.ACCEPT;
                    break;
                }
            }

            // 创建最小DFA状态并加入转移表
            State minState = new State();
            minState.setType(type);
            minDfa.getTransitTable().addVertex(minState);
            groupToMinState.put(gId, minState);

            // 标记最小DFA的起始状态：包含原DFA起始状态的组为新起始态
            for (Integer stateId : group.keySet()) {
                if (stateId == dfa.getStartState().getId()) {
                    minDfa.setStartState(minState);
                }
            }
        }

        // 容错：若未找到起始状态（理论上不会发生），取第一个状态作为起始态
        if (minDfa.getStartState() == null && !groupToMinState.isEmpty()) {
            minDfa.setStartState(groupToMinState.values().iterator().next());
        }

        // 步骤4：为最小DFA添加转移边
        for (LabelEdge edge : dfa.getTransitTable().edgeSet()) {
            State source = (State) edge.getSource(); // 原DFA源状态
            State target = (State) edge.getTarget(); // 原DFA目标状态
            Character label = edge.getLabel(); // 转移字符

            // 查找原状态所属的组ID
            Integer sourceGroup = stateToGroup.get(source.getId());
            Integer targetGroup = stateToGroup.get(target.getId());

            // 为最小DFA添加转移：组→组 映射为 状态→状态
            if (sourceGroup != null && targetGroup != null) {
                State minSource = groupToMinState.get(sourceGroup);
                State minTarget = groupToMinState.get(targetGroup);

                if (minSource != null && minTarget != null) {
                    minDfa.getTransitTable().addEdge(minSource, minTarget, label);
                }
            }
        }

        // 重新编号最小DFA的状态ID（保证ID从0开始连续）
        minDfa.renumberSID();
        // 修正起始状态类型（标记为START）
        minDfa.getStartState().setType(State.START);
        return minDfa;
    }

    /**
     * 将状态组集合转换为字符串（备用调试方法）
     */
    private String GroupSetToString(HashMap<Integer,HashMap<Integer, State>> GroupSet){
        StringBuilder str = new StringBuilder();
        for( Integer g: GroupSet.keySet()){
            String tmp = GroupToString(GroupSet.get(g));
            str.append(g).append(":").append(tmp).append("\t");
        }
        return str.toString();
    }

    /**
     * 将单个状态组转换为字符串（备用调试方法）
     */
    private String GroupToString(HashMap<Integer, State> group){
        StringBuilder str = new StringBuilder();
        for(Integer k : group.keySet()){
            str.append(group.get(k).getId()).append(":").append(group.get(k).getType()).append(",");
        }
        if(!str.isEmpty()) str = new StringBuilder(str.substring(0, str.length() - 1)); // 移除最后一个逗号
        str = new StringBuilder("{" + str + "}");
        return str.toString();
    }
}