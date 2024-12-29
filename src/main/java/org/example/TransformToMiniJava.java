package org.example;

import config_reader.ConfigReaderException;
import language_definitions.PredefinedLanguages;
import lexer.exceptions.LexerParseException;
import main.GpModifiableAST;
import selectors.data.AliasSelector;
import selectors.data.ProductionSelector;
import selectors.data.TokenSelector;
import syntax_tree.TreePrettyPrinter;
import syntax_tree.ast.AbstractSyntaxTreeNode;
import syntax_tree.ast.QueryResult;
import syntax_tree.ast.StringTreeNode;
import syntax_tree.ast.TokenTreeNode;
import syntax_tree.ast.exceptions.AddingConnectedNode;
import syntax_tree.ast.exceptions.ReplacingUnconnectedNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Example file on how to refactor function calls based on external data
 */
public class TransformToMiniJava {
    public static void run() throws LexerParseException, IOException, ReplacingUnconnectedNode, AddingConnectedNode, ConfigReaderException {
        GpModifiableAST gpModifiableAST = new GpModifiableAST();
        gpModifiableAST.load(PredefinedLanguages.MINIJAVA_EXTENDED);

        // At first, we have to create a file instance referencing the file to modify
        File input = new File("minijava_src/RefactorYodaConditions.exmjava");
        File output = new File("minijava_src/RefactorYodaConditions.out.mjava");

        // Afterward, we generate the abstract syntax tree from the file
        AbstractSyntaxTreeNode ast = gpModifiableAST.createAst(input);

        // As the ast is given, we can now perform searches on it.
        // For this example, we need to find all nodes which are a function call for the "translate" function.
        // In reality, this should have a check if it is called on a Translator object, however this will
        // require a symbol table which is not implemented for minijava yet.
        QueryResult comparisons = ast.query(new ProductionSelector("COMPARE_EXPRESSION"));

        // After we found all relevant function call nodes, we can loop over them and replace each
        // one with their camelCase alternative
        for(AbstractSyntaxTreeNode comparison: comparisons) {
            AbstractSyntaxTreeNode left = comparison.queryImmediateChildren(new AliasSelector("left")).getResult().get(0);
            TokenTreeNode compop = (TokenTreeNode)comparison.queryImmediateChildren(new TokenSelector("compop")).getResult().get(0);
            AbstractSyntaxTreeNode right = comparison.queryImmediateChildren(new AliasSelector("right")).getResult().get(0);

            switch (compop.getValue()) {
                case "<":
                    // Nothing to do, already supported by minijava
                    break;
                case "<=":
                    compop.replace(new StringTreeNode("<"));
                    right.replace(List.of(
                            new StringTreeNode("("),
                            right.deepClone(),
                            new StringTreeNode(" + 1)")
                    ));
                    break;
                case ">":
                    compop.replace(new StringTreeNode("<"));
                    left.replace(right.deepClone());
                    right.replace(left.deepClone());
                    break;
                case ">=":
                    compop.replace(new StringTreeNode("<"));
                    left.replace(right.deepClone());
                    right.replace(List.of(
                            new StringTreeNode("("),
                            left.deepClone(),
                            new StringTreeNode(" + 1)")
                    ));
                    break;
                case "==":
                    comparison.replace(List.of(
                            new StringTreeNode("("),
                            left.deepClone(),
                            new StringTreeNode(" < ("),
                            right.deepClone(),
                            new StringTreeNode(" + 1)"),
                            new StringTreeNode(") && ("),
                            right.deepClone(),
                            new StringTreeNode(" < ("),
                            left.deepClone(),
                            new StringTreeNode(" + 1)"),
                            new StringTreeNode(")")
                    ));
                    break;
            }
        }

        // The ast has been fully processed, we can save it now back to the input file, finishing the transformation.
        gpModifiableAST.saveToFile(output, ast);

        // At first, we need to prepare the parser and load a specific language.
        GpModifiableAST gpModifiableAST_minijava = new GpModifiableAST();
        gpModifiableAST_minijava.load(PredefinedLanguages.MINIJAVA);

        gpModifiableAST_minijava.createAst(output);
    }
}
