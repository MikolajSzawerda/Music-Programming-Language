package com.declarative.music.interpreter.tree;

public interface NodeAppenderVisitor<T>
{
    void visit(SequenceNode<T> node);

    void visit(GroupNode<T> node);

    void visit(SimpleNode<T> tSimpleNode);
}
