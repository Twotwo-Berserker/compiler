package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.util.tree.DefaultTree;

import java.util.ArrayDeque;

/**
 * 正则表达式语法树类，继承自通用默认树 DefaultTree<RegexTreeNode>
 * 管理整个正则表达式的语法树结构，提供树的打印、导出等功能
 */
public class RegexTree extends DefaultTree<RegexTreeNode> {

    public RegexTree(){
        super();
    }

    @Override
    public String toString() {

        if(this.root == null)
            return null;

        StringBuilder treeStr = new StringBuilder();
        ArrayDeque<RegexTreeNode> queue = new ArrayDeque<>();
        queue.add(this.root);
        RegexTreeNode node = queue.poll();

        // BFS核心循环：遍历所有节点
        while(node != null){
            // 拼接当前节点的字符串（值:类型）
            treeStr.append("(").append(node.toString()).append(")\n");
            
            // 处理当前节点的所有子节点
            RegexTreeNode childnode = (RegexTreeNode) node.getFirstChild();
            if(childnode != null) {
                // 第一个子节点标注「firstChild」
                treeStr.append("\t" + "firstChild:(").append(childnode.toString()).append(")\n");
                queue.add(childnode); // 子节点入队（后续遍历）
                childnode = (RegexTreeNode) childnode.getNextSibling();

                // 遍历剩余的兄弟节点
                while (childnode != null) {
                    treeStr.append("\t(").append(childnode.toString()).append(")\n");
                    queue.add(childnode); // 兄弟节点入队
                    childnode = (RegexTreeNode) childnode.getNextSibling();
                }
            }
            node = queue.poll(); // 取出下一个节点继续遍历
        }
        
        // 拼接最终的树描述字符串
        StringBuilder str = new StringBuilder();
        str.append("The regex tree:\n").append(treeStr.toString());
        return str.toString();
    }

    public String export(){
        String treeJson="";
        return treeJson;
    }
}
