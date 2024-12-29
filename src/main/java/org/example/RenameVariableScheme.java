package org.example;

import lexer.Token;
import lexer.exceptions.LexerParseException;
import main.GpModifiableAST;
import selectors.data.AliasSelector;
import syntax_tree.ast.AbstractSyntaxTreeNode;
import syntax_tree.ast.QueryResult;
import syntax_tree.ast.StringTreeNode;
import syntax_tree.ast.TokenTreeNode;
import syntax_tree.ast.exceptions.ReplacingUnconnectedNode;

import java.io.File;
import java.io.IOException;

/**
 * Example file on how to refactor variable names from underscore to camelCase
 */
public class RenameVariableScheme {
    public static void run(GpModifiableAST parser) throws LexerParseException, IOException, ReplacingUnconnectedNode {
        // At first, we have to create a file instance referencing the file to modify
        File input = new File("minijava_src/RenameVariables.mjava");
        File output = new File("minijava_src/RenameVariables.out.mjava");

        // Afterward, we generate the abstract syntax tree from the file
        AbstractSyntaxTreeNode ast = parser.createAst(input);

        // As the ast is given, we can now perform searches on it.
        // For this example, we only need to find all nodes that have the varName
        // alias, as the grammar defines already which nodes are variable names.
        QueryResult variableNames = ast.query(new AliasSelector("varName"));

        // After we found all variable name nodes, we can loop over them and replace each
        // one with their camelCase alternative
        for(AbstractSyntaxTreeNode node: variableNames) {

            // Receive the variable name itself, for this we need to cast the node to a
            // TokenTreeNode. This is possible without an instanceof check, as the grammar
            // uses the varName alias only on terminal symbols.
            // However, these casts need to be done carefully, queries could yield mixed
            // types of tree nodes.
            TokenTreeNode tokenNode = (TokenTreeNode) node;
            String name = tokenNode.getValue();

            // Now we can replace the node with a plain text string, which is the variable name
            // converted to camelCase.
            tokenNode.replace(new StringTreeNode(toCamelCase(name)));
        }

        // The ast has been fully processed, we can save it now back to the input file, finishing the transformation.
        parser.saveToFile(output, ast);
    }

    /**
     * Small util function to convert variable names to camelCase
     * @param in the variable name to be converted
     * @return the converted name
     */
    private static String toCamelCase(String in) {
        String[] parts = in.split("_");
        StringBuilder finalName = new StringBuilder(parts[0]);
        for(int i = 1; i < parts.length; i++) {
            finalName.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return finalName.toString();
    }
}
