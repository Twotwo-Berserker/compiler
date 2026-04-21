package org.qogir.simulation.util;

import org.qogir.compiler.grammar.regularGrammar.RegexTreeNode;
import org.qogir.simulation.logger.dto.diagramsInfos.TreeInfosDTO;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class TraverseTree {
    //层序遍历
    public static ArrayList<TreeInfosDTO> traverse(RegexTreeNode root, Integer colorNodeId) {
        if (root == null) return null;
        Queue<RegexTreeNode> queue = new LinkedList<>();
        ArrayList<TreeInfosDTO> treeInfos = new ArrayList<>();
        queue.add(root);
        treeInfos.add(getTreeInfosDTO(root, null, colorNodeId));
        while (!queue.isEmpty()) {
            RegexTreeNode node = queue.poll();

            if (node.getFirstChild() != null) {
                RegexTreeNode child = (RegexTreeNode) node.getFirstChild();
                queue.add(child);
                treeInfos.add(getTreeInfosDTO(child, node, colorNodeId));

                while (child.getNextSibling() != null) {
                    child = (RegexTreeNode) child.getNextSibling();
                    queue.add(child);
                    treeInfos.add(getTreeInfosDTO(child, node, colorNodeId));
                }
            }
        }

        return treeInfos;

    }

    public static TreeInfosDTO getTreeInfosDTO(RegexTreeNode node, RegexTreeNode parent, Integer colorNodeId) {
        TreeInfosDTO treeInfosDTO = new TreeInfosDTO();
        treeInfosDTO.setKey(node.getId());
        if (parent != null) treeInfosDTO.setParent(parent.getId());
        treeInfosDTO.setName(node.getValue() + "");
        treeInfosDTO.setType(node.getType());
        if (node.getType() == 0) treeInfosDTO.setCategory("basic");
        else treeInfosDTO.setCategory("character");
        if (Objects.equals(node.getId(), colorNodeId)) treeInfosDTO.setColor("red");
        return treeInfosDTO;
    }
}
