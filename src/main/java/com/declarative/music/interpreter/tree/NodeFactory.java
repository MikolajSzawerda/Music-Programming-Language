package com.declarative.music.interpreter.tree;

public interface NodeFactory<T>
{
    GroupNode<T> createGroup();

    SequenceNode<T> createSequence();

    SimpleNode<T> createSimpleNode(T value);

}
