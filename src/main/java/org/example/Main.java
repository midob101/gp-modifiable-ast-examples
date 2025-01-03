package org.example;

import config_reader.ConfigReaderException;
import lexer.exceptions.LexerParseException;
import syntax_tree.ast.exceptions.AddingConnectedNode;
import syntax_tree.ast.exceptions.ReplacingUnconnectedNode;

import java.io.IOException;

public class Main {

    /**
     * Runner for the examples.
     */
    public static void main(String[] args) throws ConfigReaderException, IOException, LexerParseException {

        // Afterward, we can start to execute our own modifications
        try {
            RefactorYoda.run();
            TransformToMiniJava.run();
            RenameVariableScheme.run();
        } catch (ReplacingUnconnectedNode | AddingConnectedNode e) {
            throw new RuntimeException(e);
        }
    }
}