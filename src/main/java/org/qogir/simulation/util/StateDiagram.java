package org.qogir.simulation.util;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.grammar.regularGrammar.RDFA;
import org.qogir.compiler.grammar.regularGrammar.TNFA;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.simulation.logger.dto.diagramsInfos.EdgesDTO;
import org.qogir.simulation.logger.dto.diagramsInfos.NodeInfosDTO;
import org.qogir.simulation.logger.dto.diagramsInfos.StateInfosDTO;

import java.util.ArrayList;
import java.util.Set;

public class StateDiagram {
    public static StateInfosDTO getStateDiagramFromNFA(TNFA tnfa) {
        tnfa.renumberSID();
        StateInfosDTO stateInfosDTO = new StateInfosDTO();
        ArrayList<NodeInfosDTO> nodeInfos = new ArrayList<>();
        ArrayList<EdgesDTO> edges = new ArrayList<>();

        Set<State> vertexSet = tnfa.getTransitTable().vertexSet();
        Set<LabelEdge> edgeSet = tnfa.getTransitTable().edgeSet();
        int firstId = tnfa.getStartState().getId();
        nodeInfos.add(new NodeInfosDTO(-1, "start", "text"));
        for (State s : vertexSet) {
            boolean mark = false;
            for (LabelEdge e : edgeSet) {
                State source = (State) e.getSource();
                State target = (State) e.getTarget();
                if (source.getId() == s.getId() || target.getId() == s.getId()) {
                    mark = true;
                    break;
                }
            }
            //insure vertex in edge
            if (!mark) continue;

            NodeInfosDTO nodeInfosDTO = new NodeInfosDTO();
            nodeInfosDTO.setId(s.getId());
            nodeInfosDTO.setLabel(String.valueOf(s.getSid()));
            if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                nodeInfosDTO.setType("accept");
            } else {
                nodeInfosDTO.setType("normal");
            }
            nodeInfos.add(nodeInfosDTO);
        }

        for (LabelEdge e : edgeSet) {
            State source = (State) e.getSource();
            State target = (State) e.getTarget();
            edges.add(new EdgesDTO(source.getId(), target.getId(), e.getLabel() + ""));
        }

        if (nodeInfos.size() != 1) {
            edges.add(new EdgesDTO(-1, firstId, ""));
        }

        stateInfosDTO.setNodeInfos(nodeInfos);
        mergeSameEdges(edges);
        stateInfosDTO.setEdges(edges);

        return stateInfosDTO;
    }

    public static StateInfosDTO getStateDiagramFromDFA(RDFA rdfa) {
        rdfa.renumberSID();
        StateInfosDTO stateInfosDTO = new StateInfosDTO();
        ArrayList<NodeInfosDTO> nodeInfos = new ArrayList<>();
        ArrayList<EdgesDTO> edges = new ArrayList<>();

        Set<State> vertexSet = rdfa.getTransitTable().vertexSet();
        Set<LabelEdge> edgeSet = rdfa.getTransitTable().edgeSet();
        int firstId = rdfa.getStartState().getId();
        nodeInfos.add(new NodeInfosDTO(-1, "start", "text"));
        for (State s : vertexSet) {
            boolean mark = false;
            for (LabelEdge e : edgeSet) {
                State source = (State) e.getSource();
                State target = (State) e.getTarget();
                if (source.getId() == s.getId() || target.getId() == s.getId()) {
                    mark = true;
                    break;
                }
            }
            //insure vertex in edge
            if (!mark) continue;

            NodeInfosDTO nodeInfosDTO = new NodeInfosDTO();
            nodeInfosDTO.setId(s.getId());
            nodeInfosDTO.setLabel(String.valueOf(s.getSid()));
            if (s.getType() == State.ACCEPT || s.getType() == State.ACCEPTANDSTART) {
                nodeInfosDTO.setType("accept");
            } else {
                nodeInfosDTO.setType("normal");
            }
            nodeInfos.add(nodeInfosDTO);
        }

        for (LabelEdge e : edgeSet) {
            State source = (State) e.getSource();
            State target = (State) e.getTarget();
            edges.add(new EdgesDTO(source.getId(), target.getId(), e.getLabel() + ""));
        }


        if (nodeInfos.size() != 1) {
            edges.add(new EdgesDTO(-1, firstId, ""));
        }

        stateInfosDTO.setNodeInfos(nodeInfos);
        mergeSameEdges(edges);
        stateInfosDTO.setEdges(edges);

        return stateInfosDTO;
    }

    public static void mergeSameEdges(ArrayList<EdgesDTO> edges) {
        for (int i = 0; i < edges.size(); i++) {
            for (int j = 0; j < edges.size(); j++) {
                if (edges.get(i).getFrom() == edges.get(j).getFrom() && edges.get(i).getTo() == edges.get(j).getTo() && edges.get(i).getLabel() != edges.get(j).getLabel()) {
                    edges.get(i).setLabel(edges.get(i).getLabel() + "," + edges.get(j).getLabel());
                    edges.get(j).setFrom(-100);
                }
            }
        }
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).getFrom() == -100) {
                edges.remove(i);
                i--;
            }

        }
    }
}
