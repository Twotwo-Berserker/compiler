package org.qogir.simulation.util;

import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.Regex;
import org.qogir.compiler.grammar.regularGrammar.RegularGrammar;
import org.qogir.simulation.logger.ConstructionLogger;
import org.qogir.simulation.logger.dto.StateMinTableDTO;
import org.qogir.simulation.logger.dto.StateMiniDTO;
import org.qogir.simulation.logger.dto.StateMiniTableRowDTO;
import org.qogir.simulation.logger.dto.StateSet;
import org.qogir.simulation.logger.dto.diagramsInfos.NodeInfosDTO;
import org.qogir.simulation.logger.dto.diagramsInfos.StateInfosDTO;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StateMini {
    public static ArrayList<ArrayList<StateMiniDTO>> turnToStateMiniDTO(ConstructionLogger<StateMiniTableRowDTO> constructionLogger, HashMap<Regex, RDFA> regexRDFAHashMap, RegularGrammar rg) {

        //多个正则表达式的分步骤数据
        ArrayList<ArrayList<StateMiniDTO>> regexData = new ArrayList<>();

        ArrayDeque<ArrayDeque<StateMiniTableRowDTO>> stepQueues = constructionLogger.getStepQueues();
        int count = 0;//计算当前执行到哪个表达式
        for (ArrayDeque<StateMiniTableRowDTO> stateMiniTableRowDTOS : stepQueues) {
            //StateMiniDTO用于记录一张状态图和一个表，一个表达式记录多个步骤故有多张表和图
            ArrayList<StateMiniDTO> stateMiniDTOS = new ArrayList<>();

            ArrayList<StateMinTableDTO> stateMinTableDTOS = new ArrayList<>();
            for (StateMiniTableRowDTO t : stateMiniTableRowDTOS) {
                //stateSets字段数据
                ArrayList<String> stateSets = new ArrayList<>();
                for (StateSet s : t.getStateSets()) {
                    stateSets.add(s.getCurStateSet().toString());
                }
                //每次添加一个步骤
                stateMinTableDTOS.add(new StateMinTableDTO(t.getStep(), t.getSplitBy(), regularStateSets(stateSets.toString())));
                ArrayList<StateMinTableDTO> temp = new ArrayList<>(stateMinTableDTOS);


                //每个步骤的图信息
                ArrayList<NodeInfosDTO> nodeInfos = new ArrayList<>();
                for (int i = 0; i < t.getStateSets().size(); i++) {
                    String states = t.getStateSets().get(i).getCurStateSet().toString().replaceAll("\\[", "{").replaceAll("]", "}");
                    nodeInfos.add(new NodeInfosDTO(i, states, t.getStateSets().get(i).getType() == 1 ? "accept" : "normal"));
                }

                //添加表和图数据
                stateMiniDTOS.add(new StateMiniDTO(temp, new StateInfosDTO(nodeInfos, new ArrayList<>())));
            }


            //将最终表结果再次添加
            ArrayList<StateMinTableDTO> temp = new ArrayList<>(stateMiniDTOS.get(stateMiniDTOS.size() - 1).getStateMinTableDTOS());
            StateInfosDTO stateInfosDTO = StateDiagram.getStateDiagramFromDFA(regexRDFAHashMap.get(rg.getPatterns().get(count)));
            ArrayList<NodeInfosDTO> oldNodeInfos = stateInfosDTO.getNodeInfos();
            for (NodeInfosDTO n : oldNodeInfos) {
                for (NodeInfosDTO n2 : stateMiniDTOS.get(stateMiniDTOS.size() - 1).getStateInfos().getNodeInfos()) {
                    if (stringToArray(n2.getLabel()).contains(n.getLabel())) {
                        n.setLabel(n2.getLabel().replaceAll("\\[", "{").replaceAll("]", "}"));
                    }
                }
            }
            stateInfosDTO.setNodeInfos(oldNodeInfos);
            stateMiniDTOS.add(new StateMiniDTO(temp, stateInfosDTO));


            //将最终表结果再次添加,将节点数组简化为数组中的单个值
            temp = new ArrayList<>(stateMiniDTOS.get(stateMiniDTOS.size() - 1).getStateMinTableDTOS());
            stateMiniDTOS.add(new StateMiniDTO(temp, StateDiagram.getStateDiagramFromDFA(regexRDFAHashMap.get(rg.getPatterns().get(count)))));


            regexData.add(stateMiniDTOS);
            count++;
        }
        return regexData;
    }

    public static ArrayList<String> stringToArray(String s) {
        s = s.substring(1, s.length() - 1);
        return new ArrayList<>(Arrays.asList(s.split(",")));
    }

    public static String regularStateSets(String s) {
        s = s.replaceAll("\\[", "{").replaceAll("]", "}");
        s = s.substring(1, s.length() - 1);
        s = "[" + s + "]";

        return s;
    }
}


