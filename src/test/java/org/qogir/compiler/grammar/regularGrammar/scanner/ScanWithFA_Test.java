package org.qogir.compiler.grammar.regularGrammar.scanner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Test;
import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.Regex;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;
import org.qogir.simulation.logger.ScanWithDFALogger;
import org.qogir.simulation.logger.StateMinLogger;
import org.qogir.simulation.logger.SubsetConsLogger;
import org.qogir.simulation.logger.ThompsonLogger;
import org.qogir.simulation.scanner.ScanWithDFA;
import org.qogir.simulation.scanner.Scanner;
import org.qogir.simulation.scanner.Token;
import org.qogir.simulation.util.StateMini;

import java.util.ArrayDeque;
import java.util.HashMap;

public class ScanWithFA_Test {
    //Test Java Agent
    public static void main(String[] args) {
        String[] regexes = new String[]{"regex0:=a|b", "regex1 := c(a|b)*", "regex2 := (c(a|b)*)*(d|ε)f*c"};//"regex1 := c(a|b)*","regex2 := d(f|ea*(g|h))b","c(a|b)*","a|b", "ab*", "d(f|e)","d(f|ea*(g|h))b","c(a|b)*"
        String input = "abbcab";//aabb
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);
        while (true) {
            ThompsonLogger.reset();
            ScanWithDFALogger.reset();
            StateMinLogger.reset();
            SubsetConsLogger.reset();

            java.util.Scanner reader = new java.util.Scanner(System.in);
            int number = reader.nextInt();
            if (number == 1) {
                ScanWithDFA scanWithDFA = new ScanWithDFA(rg, input);
                Scanner scanner = new Scanner(rg);
                HashMap<Regex, RDFA> regexRDFAHashMap = scanner.constructAllDFA();
                ArrayDeque<Token> tokens = new ArrayDeque<>();
                int result = scanWithDFA.scan(tokens, regexRDFAHashMap);

                System.out.println("#############ThompsonLogger#############");
                System.out.println(ThompsonLogger.constructionLogger.returnStepQueues());
                System.out.println();


                System.out.println("#############StateMinLogger#############");
                System.out.println(JSON.toJSONString(StateMini.turnToStateMiniDTO(StateMinLogger.constructionLogger, regexRDFAHashMap, rg), SerializerFeature.DisableCircularReferenceDetect));
                System.out.println();

                System.out.println("#############SubsetConsLogger#############");
                System.out.println(SubsetConsLogger.constructionLogger.returnStepQueues());
                System.out.println();


                System.out.println("#############ScanWithDFALogger#############");
                System.out.println(ScanWithDFALogger.constructionLogger.returnStepQueue());
                System.out.println();
            }
        }
    }

    @Test
    public void testScanWithDFA() {
        String[] regexes = new String[]{"regex0:=a|b", "regex1:=c(a|b)*", "regex2:=(c(a|b)*)*(d|ε)f*c"};//"regex1 := c(a|b)*","regex2 := d(f|ea*(g|h))b","c(a|b)*","a|b", "ab*", "d(f|e)","d(f|ea*(g|h))b","c(a|b)*"

        String input = "abbcab";
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);
        ScanWithDFA scanWithDFA = new ScanWithDFA(rg, input);
        ArrayDeque<Token> tokens = new ArrayDeque<>();
        Scanner scanner = new Scanner(rg);
        HashMap<Regex, RDFA> regexRDFAHashMap = scanner.constructAllDFA();
        int result = scanWithDFA.scan(tokens, regexRDFAHashMap);

        StringBuilder str = new StringBuilder();
        str.append(result).append("\n");
        while (!tokens.isEmpty()) {
            Token token = tokens.poll();
            str.append(token.toString()).append("\n");
        }

        System.out.println(str);

    }

  /*  @Test
    public void testScanWithNFA() {
        String[] regexes = new String[]{"regex0 := a|b", "regex1 := c(a|b)*", "regex2 := c"};//"regex1 := c(a|b)*","regex2 := d(f|ea*(g|h))b","c(a|b)*","a|b", "ab*", "d(f|e)","d(f|ea*(g|h))b","c(a|b)*"

        String input = "abbcab";//aabb
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);
        ScanWithNFA scanWithNFA = new ScanWithNFA(rg, input);
        ArrayDeque<Token> tokens = new ArrayDeque<>();
        int result = scanWithNFA.scan(tokens);

        StringBuilder str = new StringBuilder();
        str.append(result).append("\n");
        while (!tokens.isEmpty()) {
            Token token = tokens.poll();
            str.append(token.toString()).append("\n");
        }

        System.out.println(str);

    }*/
}
