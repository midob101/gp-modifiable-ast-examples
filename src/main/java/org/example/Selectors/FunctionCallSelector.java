package org.example.Selectors;

import selectors.BaseSelector;
import selectors.data.AliasSelector;
import selectors.data.ProductionSelector;
import selectors.data.TokenValueSelector;
import selectors.logical.AndSelector;
import selectors.structural.HasImmediateChildSelector;
import syntax_tree.ast.AbstractSyntaxTreeNode;

public class FunctionCallSelector extends BaseSelector {
    BaseSelector selector;

    public FunctionCallSelector(String name) {
        selector = new AndSelector(
                new ProductionSelector("MESSAGE_SEND"),
                new HasImmediateChildSelector(
                        new AndSelector(
                                new AliasSelector("functionName"),
                                new TokenValueSelector(name)
                        )
                )
        );
    }

    @Override
    public boolean matches(AbstractSyntaxTreeNode abstractSyntaxTreeNode) {
        return selector.matches(abstractSyntaxTreeNode);
    }
}
