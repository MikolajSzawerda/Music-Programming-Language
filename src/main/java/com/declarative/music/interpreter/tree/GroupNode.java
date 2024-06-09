package com.declarative.music.interpreter.tree;

import java.util.ArrayList;
import java.util.List;


public class GroupNode<T> extends AbstractNode<T> {
    public GroupNode(final List<Node<T>> nodes) {
        super(nodes);
    }

    public GroupNode() {
        super(new ArrayList<>());
    }

    @Override
    public List<Node<T>> getChildren() {
        return nodes;
    }
}
