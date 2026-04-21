package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.util.tree.DefaultTreeNode;

import java.io.Serial;

public class RegexTreeNode extends DefaultTreeNode {
    @Serial
    private static final long serialVersionUID = 8199272493386097880L;
    public static int Node_ID = 0; //assign Node_ID to a state

    /**
     * Every state has a unique id which can not be modified.
     */
    private final int id;//state id
    private Character value;
    private int type; 
    // 0-基础字符（如a、b）；1-连接运算符（如ab的连接）；
    // 2-并运算符（如a|b的|）；3-克林闭包（*）；
    // 4-左括号；5-右括号
    private boolean isFull = false; // 标记该节点的右子树是否已完成

    // 构造方法1：仅初始化节点值和类型
    public RegexTreeNode(Character ch, int t) {
        super();
        this.value = ch;
        this.type = t;
        id = Node_ID++;
    }

    // 构造方法2：初始化节点值、类型，并指定第一个子节点和下一个兄弟节点
    public RegexTreeNode(char v, int type, RegexTreeNode firstChild, RegexTreeNode nextSibling){
        super(firstChild, nextSibling);
        this.value = v;
        this.type = type;
        id = Node_ID++;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public Character getValue() {
        return value;
    }

    public Integer getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    // 获取该节点的最后一个子节点
    public RegexTreeNode getLastChild() {
        RegexTreeNode theNode = (RegexTreeNode) this.getFirstChild();
        if (theNode != null) { //the firstChild is not the last child.
            while (theNode.getNextSibling() != null) {
                theNode = (RegexTreeNode) theNode.getNextSibling();
            }
        }
        return theNode;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    @Override
    public String toString() {
        return this.value + ":" + this.type;
    }
}
