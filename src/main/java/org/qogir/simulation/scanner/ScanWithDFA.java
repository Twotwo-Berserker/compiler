package org.qogir.simulation.scanner;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.Regex;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;

import java.util.*;

public class ScanWithDFA extends Scan {

    public ScanWithDFA(RegularGrammar rg, String input) {
        super(rg, input);
    }

    @Override
    public int scan(ArrayDeque<Token> tokenQueue, HashMap<Regex, RDFA> regexRDFAHashMap) {
        HashMap<RDFA, Regex> RDFARegexHashMap = new HashMap<>();

        HashMap<RDFA, State> startState = new HashMap<>();
        HashMap<RDFA, State> currentState;
        HashMap<RDFA, State> nextState;

        //record last accepting state during DFAs running.
        HashMap<Integer, State> lastAcceptingStates = new HashMap<>();
        HashMap<Integer, State> currentAcceptingStates = new HashMap<>();

        for (Regex rg : regexRDFAHashMap.keySet()) {
            RDFARegexHashMap.put(regexRDFAHashMap.get(rg), rg);
        }

        for (RDFA rdfa : regexRDFAHashMap.values()) {
            startState.put(rdfa, rdfa.getStartState());
        }
        currentState = new HashMap<>(startState);
        nextState = new HashMap<>();


        String inputList = this.input + "$";
        char[] input = inputList.toCharArray();


        Integer result = runDFAsForInput(input, currentState, nextState, lastAcceptingStates, currentAcceptingStates,
                regexRDFAHashMap, RDFARegexHashMap, tokenQueue, startState);

        return result;


    }

    public Integer runDFAsForInput(char[] input, HashMap<RDFA, State> currentState, HashMap<RDFA, State> nextState,
                                   HashMap<Integer, State> lastAcceptingStates, HashMap<Integer, State> currentAcceptingStates,
                                   HashMap<Regex, RDFA> regexRDFAHashMap, HashMap<RDFA, Regex> RDFARegexHashMap,
                                   ArrayDeque<Token> tokenQueue, HashMap<RDFA, State> startState) {
        //State lastAcceptingStates;
        int lastStartCharAt = 0;
        int lastAcceptedCharAt = 0;

        for (int i = 0; i < input.length; i++) {

/*            if (input[i] != '$' && !rg.symbols.contains(input[i])) {
                return ScanMessage.NOT_MATCH;
            }*/

            goNextState(currentState, nextState, input[i], RDFARegexHashMap, i);


            //put the accepting states in next states into current accepting states
            currentAcceptingStates.clear();
            for (State s : nextState.values()) {
                if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART)
                    currentAcceptingStates.put(s.getId(), s);
            }


            if (!currentAcceptingStates.isEmpty()) {
                lastAcceptedCharAt = i;
                lastAcceptingStates.clear();
                lastAcceptingStates.putAll(currentAcceptingStates);
            }

            if (nextState.isEmpty()) { //stuck, can output
                if (lastAcceptingStates.isEmpty()) {
                    meaningless();
                    return ScanMessage.NOT_MATCH;
                } else {
                    String lexeme = this.input.substring(lastStartCharAt, lastAcceptedCharAt + 1);

                    ArrayList<Integer> priority = new ArrayList<>();
                    priority.add(-1);

                    for (RDFA rdfa : regexRDFAHashMap.values()) {
                        if (IfStateInRDFA(lastAcceptingStates, rdfa)) {//Multiple DFA matches situation
                            Regex r = RDFARegexHashMap.get(rdfa);
                            if (r.getPriority() > priority.get(0)) {
                                getToken(priority, tokenQueue, r, lexeme);
                            } else if (r.getPriority() == priority.get(0)) {
                                return ScanMessage.AMBIGUITY_GRAMMAR;
                            }
                        }
                    }
                    if (input[i] != '$') {
                        i = lastAcceptedCharAt;
                        lastStartCharAt = lastAcceptedCharAt + 1;
                        currentState.clear();
                        lastAcceptingStates.clear();
                        currentState.putAll(startState);//restart NFA
                    }
                }
            } else { // hold current situation and going on
                currentState.clear();
                currentState.putAll(nextState);
            }
        }
        if (lastAcceptedCharAt != input.length - 2) return ScanMessage.NOT_MATCH;
        else return ScanMessage.SUCCESS_MATCH;
    }

    public ArrayDeque<Token> getToken(ArrayList<Integer> priority, ArrayDeque<Token> tokenQueue, Regex r, String lexeme) {
        if (priority.get(0) != -1) tokenQueue.pop();
        priority.clear();
        priority.add(r.getPriority());

        String regexName = r.getName();
        tokenQueue.add(new Token(regexName, lexeme));

        /*new ScanWithDFALogger().addTokens(tokenQueue);*/
        return tokenQueue;
    }

    public HashMap<RDFA, State> goNextState(HashMap<RDFA, State> currentState, HashMap<RDFA, State> nextState, char pos,
                                            HashMap<RDFA, Regex> RDFARegexHashMap, int posNum) {

        /*new ScanWithDFALogger().getScanStartStepLogger(currentState, nextState, RDFARegexHashMap);*/

        if (!nextState.isEmpty()) {
            nextState.clear();
        }
        //each RDFA's currentState
        for (RDFA rdfa : currentState.keySet()) {
            State state = currentState.get(rdfa);
            if (state != null) {
                LabeledDirectedGraph<State> tb = rdfa.getTransitTable();
                for (LabelEdge e : tb.getEdgeSet()) {
                    if (tb.getEdgeSource(e) == state && e.getLabel().equals(pos)) {
                        nextState.put(rdfa, tb.getEdgeTarget(e));
                    }
                }
            }
        }


        /*new ScanWithDFALogger().getScanStepLogger(nextState, RDFARegexHashMap, posNum);*/

        return nextState;
    }

    public boolean IfStateInRDFA(HashMap<Integer, State> lastAcceptingStates, RDFA rdfa) {
        Set<State> vertexSet = rdfa.getTransitTable().getVertexSet();
        List<Integer> collect = vertexSet.stream().map(State::getId).toList();
        Collection<State> values = lastAcceptingStates.values();
        for (State s : values) {
            if (collect.contains(s.getId())) return true;
        }
        return false;
    }

    void meaningless() {
        //这个函数仅仅为了修改字节码文件时用于定位函数位置
    }
}
