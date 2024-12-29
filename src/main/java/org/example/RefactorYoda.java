package org.example;

import config_reader.ConfigReaderException;
import language_definitions.PredefinedLanguages;
import lexer.exceptions.LexerParseException;
import main.GpModifiableAST;
import org.example.Selectors.FunctionCallSelector;
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

/**
 * Example file on how to refactor function calls based on external data
 */
public class RefactorYoda {
    public static void run() throws LexerParseException, IOException, ReplacingUnconnectedNode, AddingConnectedNode, ConfigReaderException {
        GpModifiableAST gpModifiableAST = new GpModifiableAST();
        gpModifiableAST.load(PredefinedLanguages.MINIJAVA_EXTENDED);

        // At first, we have to create a file instance referencing the file to modify
        File input = new File("minijava_src/RefactorYodaConditions.exmjava");
        File output = new File("minijava_src/RefactorYodaConditions.out.exmjava");

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
            boolean leftIsLiteral = isLiteralValue(left);
            boolean rightIsLiteral = isLiteralValue(right);
            if(leftIsLiteral && !rightIsLiteral) {
                left.replace(right.deepClone());
                right.replace(left.deepClone());

                String newCompOp = compop.getValue();
                switch (compop.getValue()) {
                    case "<":
                        newCompOp = ">";
                        break;
                    case "<=":
                        newCompOp = ">=";
                        break;
                    case ">":
                        newCompOp = "<";
                        break;
                    case ">=":
                        newCompOp = "<=";
                        break;
                }
                compop.replace(new StringTreeNode(newCompOp));
            }
            System.out.println(TreePrettyPrinter.print(comparison));
        }

        // The ast has been fully processed, we can save it now back to the input file, finishing the transformation.
        gpModifiableAST.saveToFile(output, ast);
    }

    private static boolean isLiteralValue(AbstractSyntaxTreeNode treeNode) {
        boolean isLiteral = false;
        if(treeNode instanceof TokenTreeNode convertedTreeNode) {
            if(convertedTreeNode.getToken().getLexerDefinition().getName().equals("integer_literal")) {
                isLiteral = true;
            }
        }
        return isLiteral;
    }
}
