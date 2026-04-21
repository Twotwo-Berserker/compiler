package org.qogir.simulation.logger;

import org.qogir.compiler.grammar.regularGrammar.RegexTreeNode;
import org.qogir.compiler.grammar.regularGrammar.TNFA;
import org.qogir.simulation.logger.dto.ThompsonDTO;
import org.qogir.simulation.util.StateDiagram;
import org.qogir.simulation.util.TraverseTree;

import java.util.ArrayDeque;

public class ThompsonLogger {
    public static ConstructionLogger<ThompsonDTO> constructionLogger = new ConstructionLogger<>();

    public static void resetParam() {
        if (!constructionLogger.getStepQueue().isEmpty()) {
            constructionLogger.getStepQueues().add(constructionLogger.getStepQueue());
        }
        constructionLogger.setStepQueue(new ArrayDeque<>());
    }

    public static void reset() {
        constructionLogger = new ConstructionLogger<>();
    }

    public void getThompsonLogger(TNFA tnfa, RegexTreeNode root, RegexTreeNode node) {
        Integer colorNodeId = node.getId();
        addStep(tnfa, root, colorNodeId);
    }

    public void addStep(TNFA tnfa, RegexTreeNode root, Integer colorNodeId) {
        ThompsonDTO thompsonDTO = new ThompsonDTO();
        thompsonDTO.setStateInfos(StateDiagram.getStateDiagramFromNFA(tnfa));
        thompsonDTO.setTreeInfos(TraverseTree.traverse(root, colorNodeId));
        constructionLogger.addStep(thompsonDTO);
    }


}
