package com.declarative.music.interpreter.tree;

import java.util.ArrayList;
import java.util.List;


public class SequenceNode<T> extends AbstractNode<T> {

    public SequenceNode(final List<Node<T>> nodes) {
        super(nodes);
    }

    public SequenceNode() {
        super(new ArrayList<>());
    }

    @Override
    public List<Node<T>> getChildren() {
        return nodes;
    }
}
