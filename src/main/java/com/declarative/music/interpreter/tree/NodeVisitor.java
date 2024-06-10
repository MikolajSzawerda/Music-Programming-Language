package com.declarative.music.interpreter.tree;

public interface NodeVisitor<T> {
    SimpleNode<T> visit(SimpleNode<T> node);

    GroupNode<T> visit(GroupNode<T> node);

    SequenceNode<T> visit(SequenceNode<T> node);
}
