package org.qogir.compiler.grammar.regularGrammar.scanner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import javassist.NotFoundException;
import org.junit.Test;
import org.qogir.compiler.FA.State;
import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.Regex;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;
import org.qogir.compiler.grammar.regularGrammar.TNFA;
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

public class ConstructionLoggerTest {
    //Test Java Agent
    public static void main(String[] args) throws NotFoundException {
        while (true) {
            ThompsonLogger.reset();
            ScanWithDFALogger.reset();
            StateMinLogger.reset();
            SubsetConsLogger.reset();
            State.STATE_ID = 0;

            java.util.Scanner reader = new java.util.Scanner(System.in);
            int number = reader.nextInt();
            if (number == 1) {
                String[] regexes = new String[]{"regex0:=1234"};
                String input = "12344";
                //test defining a regular grammar
                RegularGrammar rg = new RegularGrammar(regexes);
                //test building a grammar for the grammar
                Scanner scanner = new Scanner(rg);
                HashMap<Regex, RDFA> regexRDFAHashMap = scanner.constructAllDFA();

                ScanWithDFA scanWithDFA = new ScanWithDFA(rg, input);
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
    public void testConstructMinDFALogger() {
        String[] regexes = new String[]{"regex1 := c(a|b)*"};
        //test defining a regular grammar
        RegularGrammar rg = new RegularGrammar(regexes);

        //test building a grammar for the grammar
        Scanner scanner = new Scanner(rg);
        TNFA tnfa = scanner.constructNFA();

        //test constructing the minimizeDFA
        scanner.minimizeDFA(scanner.constructDFA(tnfa));
        //System.out.println(scanner.minimizeDFA(scanner.constructDFA(tnfa)).toString());
    }

}
