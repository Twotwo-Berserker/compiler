package org.qogir.compiler.grammar.regularGrammar.scanner;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.Regex;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;
import org.qogir.compiler.grammar.regularGrammar.TNFA;
import org.qogir.simulation.logger.ScanWithDFALogger;
import org.qogir.simulation.logger.StateMinLogger;
import org.qogir.simulation.logger.SubsetConsLogger;
import org.qogir.simulation.logger.ThompsonLogger;
import org.qogir.simulation.scanner.Scanner;
import org.qogir.simulation.util.FileUtils;
import org.qogir.simulation.util.StateMini;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ScannerTest {
    @Test
    public void testConstructDFA() {
        String[] regexes = new String[]{"regex1 := c(a|b)*"};
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);
        System.out.println(rg);

        //test building a grammar for the grammar
        Scanner scanner = new Scanner(rg);
        TNFA tnfa = scanner.constructNFA();
        System.out.println(tnfa);
        System.out.println("Show the DFA:");
        //test constructing the DFA
        System.out.println(scanner.constructDFA(tnfa).toString());
    }

    public static void main(String[] args) throws IOException {
        String[] regexes = FileUtils.readRegexFromTxt("test.txt");

        RegularGrammar rg = new RegularGrammar(regexes);
        System.out.println(rg);

        System.out.println("Show the RegexTree:");
        Scanner scanner = new Scanner(rg);
        System.out.println(scanner.constructRegexTrees().toString());

        System.out.println("Show the NFA:");
        TNFA tnfa = scanner.constructNFA();
        System.out.println(tnfa.toString());

        System.out.println("Show the DFA:");
        RDFA rdfa = scanner.constructDFA(tnfa);
        System.out.println(rdfa.toString());

        System.out.println("Show the minimizeDFA:");
        System.out.println(scanner.minimizeDFA(rdfa).toString());
    }

    @Test
    public void testConstructMinDFA() {
        String[] regexes = new String[]{"regex1 := (a|b)c*"};
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);
        System.out.println(rg);

        //test building a grammar for the grammar
        Scanner scanner = new Scanner(rg);
        System.out.println("Show the NFA:");
        TNFA tnfa = scanner.constructNFA();

        System.out.println("Show the minimizeDFA:");
        //test constructing the minimizeDFA
        System.out.println(scanner.minimizeDFA(scanner.constructDFA(tnfa)).toString());
    }

    @Test
    public void testConstructMultipleDFA() {
        ThompsonLogger.reset();
        StateMinLogger.reset();
        ScanWithDFALogger.reset();
        SubsetConsLogger.reset();

        String[] regexes = new String[]{"regex0 := a|ε", "regex1 := c(a|b)*", "regex2 := (c(a|b)*)*(d|ε)f*c"};
        //String[] regexes = new String[]{"regex0 := a|ε", "regex1 := c(a|b)*","regex2 := (c(a|b)*)*(d|ε)f*c"};
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);
        //System.out.println(rg);

        //test building a grammar for the grammar
        Scanner scanner = new Scanner(rg);
        HashMap<Regex, RDFA> regexRDFAHashMap = scanner.constructAllDFA();
/*        System.out.println("Show each DFA:");
        for (RDFA rdfa : regexRDFAHashMap.values()) {
            System.out.println(rdfa.toString());
        }*/
        System.out.println("#############ThompsonLogger#############");
        System.out.println(ThompsonLogger.constructionLogger.returnStepQueues());
        System.out.println();

        System.out.println("#############StateMinLogger#############");
        System.out.println(JSON.toJSONString(StateMini.turnToStateMiniDTO(StateMinLogger.constructionLogger, regexRDFAHashMap, rg), SerializerFeature.DisableCircularReferenceDetect));
        System.out.println();

        System.out.println("#############SubsetConsLogger#############");
        System.out.println(SubsetConsLogger.constructionLogger.returnStepQueues());
        System.out.println();
    }
}