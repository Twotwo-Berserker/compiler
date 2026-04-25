package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;
import org.qogir.simulation.logger.SubsetConsLogger;

import java.util.*;

/**
 * The subset construction Algorithm for converting an NFA to a DFA.
 * The subset construction Algorithm takes an NFA N as input and output a DFA D accepting the same language as N.
 * The main mission is to eliminate ε-transitions and multi-transitions in NFA and construct a transition table for D.
 * The algorithm can be referred to {@see }
 */
public class SubsetConstruction {

    /**
     * Eliminate all ε-transitions reachable from a single state in NFA through the epsilon closure operation.
     * @param s a single state of NFA
     * @param tb the transition table of NFA
     * @return a set of state reachable from the state s on ε-transition
     * @author xuyang
     */
    private HashMap<Integer, State> epsilonClosures(State s, LabeledDirectedGraph<State> tb){
        if (!tb.vertexSet().contains(s)) { //if vertex s not in the transition table
            return null;
        }

        HashMap<Integer,State> nfaStates = new HashMap<>();
        
        // Use BFS to find all states reachable via ε-transitions
        Queue<State> queue = new LinkedList<>();
        Set<State> visited = new HashSet<>();
        
        queue.offer(s);
        visited.add(s);
        nfaStates.put(s.getId(), s);
        
        while (!queue.isEmpty()) {
            State current = queue.poll();
            
            // Find all ε-transitions from current state
            for (LabelEdge edge : tb.edgeSet()) {
                if (edge.getSource().equals(current) && edge.getLabel() == 'ε') {
                    State target = (State) edge.getTarget();
                    if (!visited.contains(target)) {
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
     * Eliminate all ε-transitions reachable from a  state set in NFA through the epsilon closure operation
     * @param ss a state set of NFA
     * @param tb the transition table of NFA
     * @return a set of state reachable from the state set on ε-transition
     * @author xuyang
     */

    public HashMap<Integer, State> epsilonClosure(HashMap<Integer, State> ss, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        for(State s : ss.values()){
            nfaStates.putAll(epsilonClosures(s,tb));
        }
        return nfaStates;
    }

    /**
     *
     * @param s
     * @param ch
     * @param tb
     * @return
     */
    private HashMap<Integer,State> moves(State s, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();

        // Find all states reachable from state s on input character ch
        for (LabelEdge edge : tb.edgeSet()) {
            if (edge.getSource().equals(s) && edge.getLabel() == ch) {
                State target = (State) edge.getTarget();
                nfaStates.put(target.getId(), target);
            }
        }

        return nfaStates;
    }

    public HashMap<Integer,State> move(HashMap<Integer, State> ss, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        for(State s : ss.values()){
            nfaStates.putAll(moves(s,ch,tb));
        }
        return nfaStates;
    }

    public HashMap<Integer,State> epsilonClosureWithMove(HashMap<Integer, State> sSet, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> states = new HashMap<>();
        states.putAll(epsilonClosure(move(sSet, ch, tb),tb));
        return states;
    }
    public RDFA subSetConstruct(TNFA tnfa){
        RDFA dfa = new RDFA();
        LabeledDirectedGraph<State> nfaTable = tnfa.getTransitTable();
        ArrayList<Character> alphabet = tnfa.getAlphabet();
        
        // Map to track processed DFA states and their corresponding NFA state sets
        HashMap<HashMap<Integer, State>, State> nfaSetToDfaState = new HashMap<>();
        Queue<HashMap<Integer, State>> worklist = new LinkedList<>();
        
        // Step 1: Compute the epsilon closure of the NFA start state
        HashMap<Integer, State> startNfaSet = epsilonClosures(tnfa.getStartState(), nfaTable);
        
        // Create the DFA start state
        State dfaStart = new State();
        dfaStart.setType(State.START);
        dfa.setStartState(dfaStart);
        dfa.getTransitTable().addVertex(dfaStart);
        dfa.setStateMappingBetweenDFAAndNFA(dfaStart, startNfaSet);
        nfaSetToDfaState.put(startNfaSet, dfaStart);
        worklist.offer(startNfaSet);
        
        // Print initial DFA state
        if (startNfaSet != null) {
            System.out.println("DFA State:" + dfaStart.getSid() + ":" + dfaStart.getType() + " NFA State set: " + formatStateSet(startNfaSet));
        }

        int dfaStateCounter = 1;
        
        // Step 2: Process the worklist
        while (!worklist.isEmpty()) {
            HashMap<Integer, State> currentNfaSet = worklist.poll();
            State currentDfaState = nfaSetToDfaState.get(currentNfaSet);
            
            // For each input symbol in the alphabet
            for (Character ch : alphabet) {
                // Compute epsilon-closure(move(currentNfaSet, ch))
                HashMap<Integer, State> nextNfaSet = epsilonClosureWithMove(currentNfaSet, ch, nfaTable);
                
                if (nextNfaSet.isEmpty()) {
                    continue;
                }
                
                // Check if this NFA set already has a corresponding DFA state
                State nextDfaState;
                if (!nfaSetToDfaState.containsKey(nextNfaSet)) {
                    // Create a new DFA state
                    nextDfaState = new State();
                    int type = containsAcceptingState(nextNfaSet) ? State.ACCEPT : State.MIDDLE;
                    nextDfaState.setType(type);
                    dfa.getTransitTable().addVertex(nextDfaState);
                    dfa.setStateMappingBetweenDFAAndNFA(nextDfaState, nextNfaSet);
                    nfaSetToDfaState.put(nextNfaSet, nextDfaState);
                    worklist.offer(nextNfaSet);
                    
                    // Print new DFA state
                    System.out.println("DFA State:" + nextDfaState.getSid() + ":" + nextDfaState.getType() + " NFA State set: " + formatStateSet(nextNfaSet));
                } else {
                    nextDfaState = nfaSetToDfaState.get(nextNfaSet);
                }
                
                // Add transition from currentDfaState to nextDfaState on symbol ch
                dfa.getTransitTable().addEdge(currentDfaState, nextDfaState, ch);
                
                // Log the transition
                SubsetConsLogger.getSubsetConsLogger(dfa, ch, currentDfaState, nextNfaSet);
            }
        }
        
        // Renumber DFA state SIDs to be sequential starting from 0
        dfa.renumberSID();
        
        return dfa;
    }
    
    /**
     * Check if a set of NFA states contains an accepting state
     */
    private boolean containsAcceptingState(HashMap<Integer, State> nfaSet) {
        for (State s : nfaSet.values()) {
            if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Format a state set for display
     */
    private String formatStateSet(HashMap<Integer, State> nfaSet) {
        if (nfaSet.isEmpty()) {
            return "{}";
        }
        List<Integer> ids = new ArrayList<>(nfaSet.keySet());
        Collections.sort(ids);
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i));
            if (i < ids.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

}
