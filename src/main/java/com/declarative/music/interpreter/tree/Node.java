package com.declarative.music.interpreter.tree;

import java.util.List;


public interface Node<T> extends NodeAppenderVisitor<T>
{
    List<Node<T>> getSiblings();

    List<Node<T>> getChildren();

    void accept(NodeAppenderVisitor<T> visitor);
}
