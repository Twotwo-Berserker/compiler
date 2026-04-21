package org.qogir.simulation.scanner;

import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.Regex;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * lexical analysis
 */
public abstract class Scan {
    protected final RegularGrammar rg;
    public final String input;

    public Scan(RegularGrammar rg, String input){
        this.rg = rg;
        this.input = input;
    }

    /**
     * @param tokenQueue an empty token sequence used for holding output tokens.
     * @return
     */
    public abstract int scan(ArrayDeque<Token> tokenQueue, HashMap<Regex, RDFA> regexRDFAHashMap);
}
