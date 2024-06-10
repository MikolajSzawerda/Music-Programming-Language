package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.ModifierVisitor;

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

    @Override
    public SequenceNode<T> getModified() {
        if (modifier() == null) {
            return this;
        }
        return this.accept(modifier());
    }

    @Override
    public SequenceNode<T> accept(ModifierVisitor<T> visitor) {
        var newNode = new SequenceNode<T>();
        for (var child : nodes) {
            newNode.nodes.add(child.accept(visitor));
        }
        return newNode;
    }
}
