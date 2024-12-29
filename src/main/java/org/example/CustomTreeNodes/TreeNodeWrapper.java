package org.example.CustomTreeNodes;

import syntax_tree.ast.AbstractSyntaxTreeNode;

public class TreeNodeWrapper extends AbstractSyntaxTreeNode {
    @Override
    public String getDisplayValue() {
        return "WRAPPER";
    }

    @Override
    protected String getSources() {
        return "";
    }

}
