package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.util.StringUtil;

import java.util.ArrayList;

/**
 * 正则文法（Regular Grammar）核心类
 * 支持的符号：大小写字母（A-Za-z）、数字、空串ε；
 * 支持的正则表达式语法：
 *   - 或运算：a|b
 *   - 连接运算：ab
 *   - 闭包运算：a*
 *   - 括号分组：(a)
 * 
 * 实例化该类时，仅接受 "正则名 := 正则表达式" 格式的输入，例如：
 *   regex1 := c(a|b)* （regex1是词法分析中token的类型名，右侧是正则表达式）
 *   regex2 := a|ε     （支持空串ε）
 */
public class RegularGrammar {
    // 正则文法的字母表（存储所有出现的字母/数字符号，不含ε、运算符等）
    public ArrayList<Character> symbols = new ArrayList<Character>();
    // 存储所有正则表达式（Regex对象列表）
    private final ArrayList<Regex> patterns = new ArrayList<>();

    /**
     * 构造方法：解析输入的正则表达式数组，初始化字母表和正则表达式列表
     * @param regexes 格式为 "name := regex" 的字符串数组
     */
    public RegularGrammar(String[] regexes){
        for(String r: regexes){
            // 分割"正则名"和"正则表达式"（按最后一个":="分割，避免表达式内有":="的极端情况）
            String name = r.substring(0, r.lastIndexOf(":=") - 1);
            String regex = r.substring(r.lastIndexOf(":=") + 2);

            // 工具类去除首尾空格（保证名称和表达式的纯净性）
            StringUtil stringUtil = new StringUtil();
            name = stringUtil.trim(name);
            regex = stringUtil.trim(regex);

            // 创建Regex对象并加入列表（第三个参数0暂时无业务含义，可能是预留优先级/ID）
            Regex p = new Regex(name, regex, 0);
            this.patterns.add(p);

            // 提取正则表达式中的合法符号，加入字母表（去重）
            for(Character ch : regex.toCharArray()){
                // 仅保留字母/数字，排除ε、运算符（|、*、()）等
                if ((Character.isDigit(ch) || Character.isLetter(ch)) && ch != 'ε' && !symbols.contains(ch)) {
                    this.symbols.add(ch);
                }
            }
        }
    }

    public ArrayList<Character> getSymbols() {
        return this.symbols;
    }
    
    public ArrayList<Regex> getPatterns() {
        return patterns;
    }

    @Override
    public String toString() {
        return "Regular Grammar\n" + "Alphabet:" + symbols.toString() + "\n" + "Regexes:\n" + patterns;
    }
}
