package org.example.Selectors;

import selectors.BaseSelector;
import syntax_tree.ast.AbstractSyntaxTreeNode;

public class HiddenSelector extends BaseSelector {

    @Override
    public boolean matches(AbstractSyntaxTreeNode abstractSyntaxTreeNode) {
        return !abstractSyntaxTreeNode.isVisible();
    }
}
