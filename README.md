### 项目介绍

本项目为编译技术实验课源码，其中缺乏部分代码需同学们自行补全。
建议使用 Jetbrains 公司的编辑器 IntelliJ IDEA 打开。
下载地址为<https://www.jetbrains.com/zh-cn/idea/download/?section=windows>，下拉可以找到免费开源的社区版。
> 本项目根据 GNU 通用公共许可证（GLP 许可证）条款使用。其仅用于教育目的，其发布不附带任何保证。
### 目录结构

- .idea
  > IDEA 编辑器自动生成的配置文件，无需查看或修改。
- \*.iml
  > 在项目的根目录或子目录中可能存在 iml 文件，此文件为 IDEA 自动生成的对应目录的配置文件，无需查看或修改。
- src
  > 编译技术实验课的源代码，以下目录为 java 源码的规范项目结构(maven 规范)。
    - main
      > 存放源代码及其配置文件的文件夹。
        - java
          > 用于存放源代码，应在 IDEA 中标记为“Sources Root”。搜索文件内容"//Add your implementation"即可找到需要实现部分的位置
        - resources
          > 用于存放源代码的相关配置文件，如果没有可以为空或删除。在本实验中无相关配置文件。
    - test
      > 存放测试代码及其配置文件的文件夹。
        - java
          > 用于存放测试代码，应在 IDEA 中标记为“Test Sources Root”。
            - org.qogir.compiler.grammar.regularGrammar.scanner.ScannerTest
              > 正则表达式解析器的测试用例。前面没被注释的部分是第一次实验课要测试的内容；后面被注释的部分是后续实验课要测试的内容。
            - org.qogir.compiler.grammar.regularGrammar.scanner.ScannerWithNFATest
              > 在 ScannerTest 的基础上，添加额外的输入来测试 NFA 的运行。
        - resources
          > 用于存放测试代码的相关配置文件，在本实验中无相关配置文件，故可以为空或删除本文件夹。
- target
  > 存放 Java 源代码编译后的字节码，无需查看或修改。  
  > 扩展：Java 是一个运行时语言，但也有编译的过程。首先 Java 编译器(javac)将 Java 源代码文件(.java 文件)编译为字节码文件(.class 文件)，再使用 Java 虚拟机(jvm)运行字节码文件。由于该过程 IDEA 已经帮我们实现，因此无需手动操作，了解即可。
- pom.xml
  > Maven的配置文件，用于配置项目依赖项和基本参数。首次使用时需要从中央仓库下载依赖项，如果下载失败需要切换仓库源
  > 扩展：Maven是Java最流行的依赖项和构建管理器之一，通过xml格式的语法声明项目所需要的外部依赖项。
### 项目配置

1. 使用 IntelliJ IDEA 打开本项目，设置 JDK。JDK 版本应为 14 或以上(测试 17 可用)。
2. 设置 JDK 后，若文件不可运行，说明 IDEA 未识别目录结构，需手动设置目录文件的类型。在 IDEA 的项目列表中右键指定文件夹，点击“Mark Directory As"(“标记目录为”)，设置为指定类型。要设置的文件夹及其类型如下：
    - src/main/java -> Sources Root
    - src/test/java -> Test Sources Root
      > 扩展：在常规 Java 项目中，src/main/resources 和 src/test/resources 也应标记，但由于本项目没有资源文件，所以无需设置。
