package org.qogir.compiler.grammar.regularGrammar;

import java.util.ArrayDeque;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * 正则表达式解析器：将正则表达式字符串转换为正则表达式树（RegexTree）
 * 核心思路：基于栈的语法分析，按运算符优先级（括号>闭包*>连接->或|）构建树形结构
 * @param regex a regular expression
 */
public class ParseRegex {
    private final ArrayDeque<Character> queue = new ArrayDeque<>();

    /**
     * 构造方法：初始化输入队列，将正则表达式拆分为字符序列，末尾加%标识结束
     * @param regex 封装后的Regex对象（包含表达式字符串）
     */
    public ParseRegex(Regex regex) {
        char[] ch = regex.getRegex().toCharArray();
        for (char c : ch) {
            this.queue.add(c);
        }

        // %-end of the regex
        if (!this.queue.isEmpty())
            this.queue.add('%');
    }

    /**
     * 将正则表达式转换为正则表达式树
     * 1) 设置一个栈来保存正则表达式树的节点
     * 2) 变量“look”保存下一个输入字符
     * 3) 根据“look”的类型，当“look”的类型为基本情况（类型为字母或 ε）时，创建一个新的节点，
     * 或者当“look”的类型为运算符（类型为 * 、( 、 ) 或
     * |）时，从栈中弹出几个节点，将它们合并为一个连接节点，并将该连接节点作为其子节点，然后将新节点推入栈中。*
     * 
     * @返回一个正则表达式树
     * @author xuyang
     */
    public RegexTree parse() {
        // 空正则校验
        if (this.queue.isEmpty()) {
            System.out.println("Error: 空正则表达式！");
            return null;
        }

        RegexTree tree = new RegexTree();
        RegexTreeNode.Node_ID = 0; // 重置节点ID
        Stack<RegexTreeNode> stack = new Stack<>();
        int bracketCount = 0; // 跟踪括号计数，校验是否闭合

        // 取第一个字符（lookahead）
        char look = this.queue.poll();
        // 首字符非法校验：必须是字母/数字/ε/(，不能是*|)
        if (look == '%') {
            System.out.println("Error: 空正则表达式！");
            return null;
        }
        if (!Character.isLetterOrDigit(look) && look != '(' && look != 'ε') {
            System.out.println("Error: 正则表达式首字符非法！必须以字母/数字/ε/(`(`)开头");
            return null;
        }

        // 初始化第一个节点
        int nodeType = (Character.isLetterOrDigit(look) || look == 'ε') ? 0 : 4; // 0=基础节点，4=左括号
        RegexTreeNode firstNode = new RegexTreeNode(look, nodeType, null, null);
        stack.push(firstNode);
        if (look == '(')
            bracketCount++; // 左括号计数+1

        // 循环解析剩余字符（直到%结束符）
        look = this.queue.poll();
        while (look != '%') {
            try {
                switch (look) {
                    case '*': // 闭包运算符
                        handleKleeneStar(stack);
                        break;
                    case '(': // 左括号
                        handleLeftBracket(stack, bracketCount);
                        bracketCount++;
                        break;
                    case ')': // 右括号
                        bracketCount = handleRightBracket(stack, bracketCount);
                        if (bracketCount < 0) { // 右括号多于左括号
                            System.out.println("Error: 右括号未匹配（多余的`)`）！");
                            return null;
                        }
                        break;
                    case '|': // 或运算符
                        if (!handleUnion(stack)) {
                            return null;
                        }
                        break;
                    case 'ε': // 空字符
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
                        handleBasicChar(stack, look);
                        break;
                    default: // 字母
                        if (Character.isLetter(look)) {
                            handleBasicChar(stack, look);
                        } else {
                            System.out.println("Error: 非法字符 '" + look + "'！仅支持字母/数字/ε/*|()");
                            return null;
                        }
                        break;
                }
            } catch (EmptyStackException e) {
                System.out.println("Error: 正则表达式语法错误（栈空时操作运算符）！");
                return null;
            }
            // 取下一个字符
            look = this.queue.poll();
        }

        // 解析结束后校验：括号未闭合
        if (bracketCount != 0) {
            System.out.println("Error: 括号未闭合！剩余未匹配的括号数：" + bracketCount);
            return null;
        }

        // 合并栈中剩余节点为最终语法树
        RegexTreeNode root = mergeFinalStack(stack);
        if (root == null) {
            System.out.println("Error: 正则表达式语法错误（解析后无有效节点）！");
            return null;
        }
        tree.setRoot(root);
        return tree;
    }

    // 辅助方法：处理闭包运算符*
    private void handleKleeneStar(Stack<RegexTreeNode> stack) {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        RegexTreeNode top = stack.peek();
        int topType = top.getType();

        // *的前置节点必须是基础节点/连接/或节点，不能是左括号或连续的*
        if (topType != 0 && topType != 1 && topType != 2) {
            System.out.println("Error: 闭包运算符*前置非法！（当前前置：" + top.getValue() + "）");
            throw new IllegalArgumentException();
        }

        // 构建闭包节点（类型3）
        RegexTreeNode kleeneNode;

        // 或节点场景
        if (topType == 2) {
            // 如果|节点的右子树已完成（如(a|b)*），则闭包连接到|节点本身
            if(top.isFull()) {
                kleeneNode = new RegexTreeNode('*', 3, stack.pop(), null);
                stack.push(kleeneNode);
                // printStack(stack);;
                return;
            }

            // 未完成的|节点：闭包连接到|的右子树
            RegexTreeNode right = (RegexTreeNode) top.getFirstChild().getNextSibling();
            if (right == null) {
                System.out.println("Error: 闭包运算符*前置非法！|后无有效表达式！");
                throw new IllegalArgumentException();
            }

            while (right.getLastChild() != null) {
                right = (RegexTreeNode) right.getLastChild();
            }
            kleeneNode = new RegexTreeNode('*', 3, right, null);
            // 将闭包节点连接到|的右子树
            RegexTreeNode forthNode = (RegexTreeNode) top.getFirstChild();
            while (forthNode.getNextSibling() != null) {
                if (forthNode.getNextSibling().getFirstChild() != null) {
                    forthNode = (RegexTreeNode) forthNode.getNextSibling().getFirstChild();
                } else {
                    break;
                }
            }
            forthNode.setNextSibling(kleeneNode);
        }
        
        else { // 基础节点场景
            kleeneNode = new RegexTreeNode('*', 3, stack.pop(), null);
            stack.push(kleeneNode);
        }
        // printStack(stack);;
    }

    // 辅助方法：处理左括号(
    private void handleLeftBracket(Stack<RegexTreeNode> stack, int currentBracketCount) {
        // 压入左括号标记（仅用于栈分界，最终树会丢弃）
        RegexTreeNode leftBracketNode = new RegexTreeNode('(', 4, null, null);
        stack.push(leftBracketNode);

        // 只有队列不为空时，才检查下一个字符
        if (!this.queue.isEmpty()) {
            char next = this.queue.peek();
            // 非法情况：( 后直接跟 * | 
            if (next == '*' || next == '|') {
                System.out.println("Error: 左括号后非法字符！不能直接跟 *或|（当前字符：" + next + "）");
                throw new IllegalArgumentException();
            }
        }
    }

    // 辅助方法：处理右括号)
    private int handleRightBracket(Stack<RegexTreeNode> stack, int currentBracketCount) {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        Stack<RegexTreeNode> tempStack = new Stack<>();

        // 弹出括号内的节点，直到左括号
        while (!stack.isEmpty() && stack.peek().getType() != 4) { // 4=左括号类型
            RegexTreeNode node = stack.pop();
            tempStack.push(node);
        }
        // 左括号不存在（未匹配）
        if (stack.isEmpty()) {
            return -1;
        }
        stack.pop(); // 弹出左括号
        currentBracketCount--;

        if (tempStack.isEmpty()) {
            System.out.println("Error: 空括号()非法！");
            throw new IllegalArgumentException();
        }

        
        // 右括号前不能是|
        RegexTreeNode lastNode = tempStack.peek(); // 紧邻右括号的前置节点
        if (lastNode.getValue() == '|' && lastNode.getFirstChild().getNextSibling() == null) {
            System.out.println("Error: 右括号前非法字符！不能是|");
            throw new IllegalArgumentException();
        }

        RegexTreeNode bracketExprNode = mergeTempStack(tempStack);
        // 如果节点是或节点，标记其右子树已完成（避免后续字符错误连接到|的右子树）
        if (bracketExprNode.getType() == 2)
            bracketExprNode.setFull(true);
        // 将括号内表达式节点重新推入栈中
        stack.push(bracketExprNode);
        // printStack(stack);;
        return currentBracketCount;
    }

    // 辅助方法：处理或运算符|
    private boolean handleUnion(Stack<RegexTreeNode> stack) {
        if (stack.isEmpty()) {
            System.out.println("Error: 或运算符|前置无有效表达式！");
            return false;
        }

        RegexTreeNode top = stack.peek();
        // |不能出现在开头/左括号后/连续|
        if (top == null || top.getType() == 4 || (top.getValue() == '|' && top.getFirstChild().getNextSibling() == null)) { // 4=左括号类型
            System.out.println("Error: 或运算符|非法！");
            return false;
        }

        // |不能出现在结尾
        if (this.queue.isEmpty() || this.queue.peek() == '%') {
            System.out.println("Error: 或运算符|非法！");
            return false;
        }

        Stack<RegexTreeNode> tempStack = new Stack<>();
        // 弹出|左侧的有效节点
        while (!stack.isEmpty() && stack.peek().getType() != 4) {
            tempStack.push(stack.pop());
        }
        if (tempStack.isEmpty()) {
            System.out.println("Error: 或运算符|左侧无有效表达式！");
            return false;
        }

        // 构建或节点（类型2）
        RegexTreeNode unionNode = new RegexTreeNode('|', 2, null, null);
        RegexTreeNode leftExpr = mergeTempStack(tempStack);
        unionNode.setFirstChild(leftExpr);
        stack.push(unionNode);
        // printStack(stack);;
        return true;
    }

    // 辅助方法：处理基础字符（字母/数字/ε）
    private void handleBasicChar(Stack<RegexTreeNode> stack, char ch) {
        RegexTreeNode basicNode = new RegexTreeNode(ch, 0, null, null);
        // 栈顶是基础节点/闭包/右括号 → 构建连接节点（隐式连接运算）
        if (!stack.isEmpty()) {
            RegexTreeNode top = stack.peek();
            // 这里出问题了！！！如何区分a|bc和(a|b)c
            // 栈顶是 | → 连接到|的右子树
            if (top.getType() == 2) {
                // 如果|节点的右子树已完成（如(a|b)后接c），则新字符连接到|节点本身，而不是右子树
                if (top.isFull()) {
                    RegexTreeNode concat = new RegexTreeNode('-', 1, stack.pop(), null);
                    concat.getFirstChild().setNextSibling(basicNode);
                    stack.push(concat);
                    // printStack(stack);;
                    return;
                }

                // 未完成的|节点：连接到|的右子树
                // 取出当前右子树
                RegexTreeNode right = (RegexTreeNode) top.getFirstChild().getNextSibling(); // |的右子树

                if (right == null) {
                    // 第一次：直接作为右子树
                    top.getFirstChild().setNextSibling(basicNode);
                } else {
                    // 后面来的字符：和右子树连接，更新右子树
                    RegexTreeNode concat = new RegexTreeNode('-', 1, right, null);
                    concat.getFirstChild().setNextSibling(basicNode);
                    top.getFirstChild().setNextSibling(concat);
                }
                // System.out.println("|的右子树：" + top.getFirstChild().getNextSibling().toString());
                // printStack(stack);;
                return;
            }

            // 栈顶是基础节点/连接/闭包 → 构建连接节点
            if (top.getType() == 0 || top.getType() == 1 || top.getType() == 3 ) {
                RegexTreeNode concatNode = new RegexTreeNode('-', 1, stack.pop(), null);
                concatNode.getFirstChild().setNextSibling(basicNode);
                stack.push(concatNode);
                // printStack(stack);;
                return;
            }
        }
        // 其他情况：直接推入基础节点
        stack.push(basicNode);
        // printStack(stack);;
    }

    // 辅助方法：合并临时栈为单个节点（处理连接运算）
    private RegexTreeNode mergeTempStack(Stack<RegexTreeNode> tempStack) {
        if (tempStack.isEmpty())
            return null;
     
        // 一个节点
        if (tempStack.size() == 1)
            return tempStack.pop();

        // 多个节点(肯定是因为有嵌套括号)
        RegexTreeNode first = tempStack.pop();
        RegexTreeNode current = first;
        while (!tempStack.isEmpty())
        {
            RegexTreeNode next = tempStack.pop(); // current左，next右
            
            // |-与||与|*处理
            if (current.getType() == 2 && (next.getType() == 1 || next.getType() == 2 || next.getType() == 3)) { // 如果第一个节点是|，则后续节点都连接到|的右子树
                if (!current.isFull()) { // next连接到current右子树
                    RegexTreeNode right = (RegexTreeNode) current.getFirstChild().getNextSibling();
                    if (right == null) {
                        System.out.println("Error: 闭包运算符*前置非法！|后无有效表达式！");
                        throw new IllegalArgumentException();
                    }
                    RegexTreeNode concat = new RegexTreeNode('-', 1, right, null);
                    concat.getFirstChild().setNextSibling(next);
                    current.getFirstChild().setNextSibling(concat);
                }
            }

            // 其他节点直接连接
            else { 
                RegexTreeNode concatNode = new RegexTreeNode('-', 1, null, null);
                concatNode.setFirstChild(current);
                current.setNextSibling(next);
                current = concatNode;
            }
        }
        return current;
    }

    // 辅助方法：解析结束后合并栈为最终根节点
    private RegexTreeNode mergeFinalStack(Stack<RegexTreeNode> stack) {
        Stack<RegexTreeNode> temp = new Stack<>();

        // 过滤掉残留的括号节点
        while (!stack.isEmpty()) {
            RegexTreeNode node = stack.pop();
            if (node.getType() != 4 && node.getType() != 5) { // 排除左/右括号
                temp.push(node);
            }
        }

        // 无有效节点
        if (temp.isEmpty()) {
            // System.out.println("无有效节点！");
            return null;
        }
            
        // 多个节点 → 合并为连接节点
        if (temp.size() > 1) {
            // System.out.println("多个节点 → 合并为连接节点");
            return mergeTempStack(temp);
        } else {
            return temp.pop();
        }
    }

    // 辅助方法：打印当前栈内容（调试用）
    private void printStack(Stack<RegexTreeNode> stack) {
        System.out.println("======= 当前栈内容 =======");
        if (stack.isEmpty()) {
            System.out.println("栈空");
        } else {
            // 从栈底到栈顶打印
            for (int i = 0; i < stack.size(); i++) {
                RegexTreeNode node = stack.get(i);
                System.out.println(
                    "位置 " + i +
                    " | 字符：" + node.getValue() +
                    " | 类型：" + node.getType()
                );
            }
        }
        System.out.println("=========================\n");
    }
}
