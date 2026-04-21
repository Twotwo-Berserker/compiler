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
    private int type; //0-basic；1-concatenation；2-union； 3-kleene closure; 4-leftParenthesis; 5-rightParenthesis

    public RegexTreeNode(Character ch, int t) {
        super();
        value = ch;
        type = t;
        id = Node_ID++;
    }

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

    public RegexTreeNode getLastChild() {

        RegexTreeNode theNode = (RegexTreeNode) this.getFirstChild();
        if (theNode != null) { //the firstChild is not the last child.
            while (theNode.getNextSibling() != null) {
                theNode = (RegexTreeNode) theNode.getNextSibling();
            }
        }
        return theNode;
    }


    @Override
    public String toString() {
        return this.value + ":" + this.type;
    }
}
