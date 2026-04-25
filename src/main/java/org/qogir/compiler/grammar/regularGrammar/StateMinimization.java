package org.qogir.compiler.grammar.regularGrammar;


import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;
import org.qogir.simulation.logger.StateMinLogger;

import java.util.*;

public class StateMinimization {

    /**
     * Distinguish non-equivalent states in the given DFA.
     *
     * @param dfa the original dfa.
     * @return distinguished equivalent state groups
     */
    public HashMap<Integer, HashMap<Integer, State>> distinguishEquivalentState(RDFA dfa) {
        HashMap<Integer,HashMap<Integer, State>> groupSet = new HashMap<>();
        
        // Step 1: Initial split - separate accepting and non-accepting states
        HashMap<Integer, State> acceptStates = new HashMap<>();
        HashMap<Integer, State> nonAcceptStates = new HashMap<>();
        
        for (State s : dfa.getTransitTable().vertexSet()) {
            if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                acceptStates.put(s.getId(), s);
            } else {
                nonAcceptStates.put(s.getId(), s);
            }
        }
        
        int groupId = 0;
        if (!acceptStates.isEmpty()) {
            groupSet.put(groupId++, acceptStates);
        }
        if (!nonAcceptStates.isEmpty()) {
            groupSet.put(groupId++, nonAcceptStates);
        }
        
        // Log initial step
        StateMinLogger logger = new StateMinLogger();
        logger.setFirstTwoSteps(groupSet);
        
        System.out.println("GroupSet.size:" + groupSet.size());
        printGroupSet(0, groupSet, "initial split");
        
        // Step 2: Iteratively refine the partition
        boolean changed = true;
        int step = 1;
        ArrayList<Character> alphabet = dfa.getAlphabet();
        
        while (changed && step < 100) {
            changed = false;
            
            // Try splitting by each symbol in the alphabet
            for (Character ch : alphabet) {
                HashMap<Integer, HashMap<Integer, State>> newGroupSet = new HashMap<>();
                int newGroupId = 0;
                
                for (Integer gId : groupSet.keySet()) {
                    HashMap<Integer, State> group = groupSet.get(gId);
                    
                    // Group states by their transition on symbol ch
                    HashMap<Integer, Integer> stateToTargetGroup = new HashMap<>();
                    for (Integer stateId : group.keySet()) {
                        State s = group.get(stateId);
                        Integer targetGroupId = findTargetGroup(dfa, s, ch, groupSet);
                        stateToTargetGroup.put(stateId, targetGroupId);
                    }
                    
                    // Split group based on target groups
                    HashMap<Integer, HashMap<Integer, State>> subGroups = new HashMap<>();
                    for (Integer stateId : group.keySet()) {
                        Integer targetGroupId = stateToTargetGroup.get(stateId);
                        if (!subGroups.containsKey(targetGroupId)) {
                            subGroups.put(targetGroupId, new HashMap<>());
                        }
                        subGroups.get(targetGroupId).put(stateId, group.get(stateId));
                    }
                    
                    // Add subgroups to new group set
                    for (HashMap<Integer, State> subGroup : subGroups.values()) {
                        newGroupSet.put(newGroupId++, subGroup);
                    }
                    
                    if (subGroups.size() > 1) {
                        changed = true;
                    }
                }
                
                if (changed) {
                    groupSet = newGroupSet;
                    printGroupSet(step, groupSet, "end of_" + ch);
                    logger.addStep(groupSet, ch.toString());
                    step++;
                    break; // Restart with new partition
                }
            }
        }
        
        return groupSet;
    }
    
    /**
     * Find which group a state transitions to on a given symbol
     */
    private Integer findTargetGroup(RDFA dfa, State s, Character ch, HashMap<Integer, HashMap<Integer, State>> groupSet) {
        // Find the target state
        for (LabelEdge edge : dfa.getTransitTable().edgeSet()) {
            if (edge.getSource().equals(s) && edge.getLabel() == ch) {
                State target = (State) edge.getTarget();
                // Find which group contains the target state
                for (Integer gId : groupSet.keySet()) {
                    if (groupSet.get(gId).containsKey(target.getId())) {
                        return gId;
                    }
                }
            }
        }
        // No transition on this symbol, return -1 as a special group
        return -1;
    }
    
    /**
     * Print the current group set
     */
    private void printGroupSet(int step, HashMap<Integer, HashMap<Integer, State>> groupSet, String memo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Step").append(step).append(":  ");
        
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
     * Format a group for display
     */
    private String formatGroup(HashMap<Integer, State> group) {
        List<String> stateStrs = new ArrayList<>();
        for (State s : group.values()) {
            stateStrs.add(s.getId() + ":" + s.getType());
        }
        Collections.sort(stateStrs);
        return "{" + String.join(",", stateStrs) + "}";
    }

    public RDFA minimize(RDFA dfa) {
        // Get the distinguished state groups
        HashMap<Integer, HashMap<Integer, State>> groupSet = distinguishEquivalentState(dfa);
        
        // Create the minimized DFA
        RDFA minDfa = new RDFA();
        minDfa.setAlphabet(dfa.getAlphabet());
        
        // Map from group ID to the representative DFA state in the minimized DFA
        HashMap<Integer, State> groupToMinState = new HashMap<>();
        
        // Map from original DFA state ID to group ID
        HashMap<Integer, Integer> stateToGroup = new HashMap<>();
        for (Integer gId : groupSet.keySet()) {
            for (Integer stateId : groupSet.get(gId).keySet()) {
                stateToGroup.put(stateId, gId);
            }
        }
        
        // Create states for the minimized DFA
        int minStateId = 0;
        for (Integer gId : groupSet.keySet()) {
            HashMap<Integer, State> group = groupSet.get(gId);
            
            // Determine the type of the new state
            int type = State.MIDDLE;
            for (State s : group.values()) {
                if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                    type = State.ACCEPT;
                    break;
                }
            }
            
            State minState = new State();
            minState.setType(type);
            minDfa.getTransitTable().addVertex(minState);
            groupToMinState.put(gId, minState);
            
            // Check if this group contains the original start state
            for (Integer stateId : group.keySet()) {
                if (stateId == dfa.getStartState().getId()) {
                    minDfa.setStartState(minState);
                }
            }
        }
        
        // If no start state was set (shouldn't happen), use the first state
        if (minDfa.getStartState() == null && !groupToMinState.isEmpty()) {
            minDfa.setStartState(groupToMinState.values().iterator().next());
        }
        
        // Add transitions
        for (LabelEdge edge : dfa.getTransitTable().edgeSet()) {
            State source = (State) edge.getSource();
            State target = (State) edge.getTarget();
            Character label = edge.getLabel();
            
            Integer sourceGroup = stateToGroup.get(source.getId());
            Integer targetGroup = stateToGroup.get(target.getId());
            
            if (sourceGroup != null && targetGroup != null) {
                State minSource = groupToMinState.get(sourceGroup);
                State minTarget = groupToMinState.get(targetGroup);
                
                if (minSource != null && minTarget != null) {
                    minDfa.getTransitTable().addEdge(minSource, minTarget, label);
                }
            }
        }
        
        // Renumber minimized DFA state SIDs to be sequential starting from 0
        minDfa.renumberSID();
        minDfa.getStartState().setType(State.START);
        return minDfa;
    }

    private String GroupSetToString(HashMap<Integer,HashMap<Integer, State>> GroupSet){
        StringBuilder str = new StringBuilder();
        for( Integer g: GroupSet.keySet()){
            String tmp = GroupToString(GroupSet.get(g));
            str.append(g).append(":").append(tmp).append("\t");
        }
        return str.toString();
    }

    private String GroupToString(HashMap<Integer, State> group){
        StringBuilder str = new StringBuilder();
        for(Integer k : group.keySet()){
            str.append(group.get(k).getId()).append(":").append(group.get(k).getType()).append(",");
        }
        if(!str.isEmpty()) str = new StringBuilder(str.substring(0, str.length() - 1));
        str = new StringBuilder("{" + str + "}");
        return str.toString();
    }
}
