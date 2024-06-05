package com.declarative.music.interpreter.tree;

import java.util.List;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AbstractNode<T> implements Node<T>, NodeAppenderVisitor<T>
{
    public final List<Node<T>> nodes;
}
