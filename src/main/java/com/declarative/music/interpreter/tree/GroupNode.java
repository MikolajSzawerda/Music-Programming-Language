package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.ModifierVisitor;

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

    @Override
    public GroupNode<T> getModified() {
        if (modifier() == null) {
            return this;
        }
        return this.accept(modifier());
    }

    @Override
    public GroupNode<T> accept(ModifierVisitor<T> visitor) {
        var newNode = new GroupNode<T>();
        for (var child : nodes) {
            newNode.nodes.add(child.accept(visitor));
        }
        return newNode;
    }
}
