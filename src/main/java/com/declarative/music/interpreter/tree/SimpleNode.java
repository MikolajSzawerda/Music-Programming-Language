package com.declarative.music.interpreter.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class SimpleNode<T> implements Node<T>, NodeAppenderVisitor<T>
{
    private T value;

    @Override
    public List<Node<T>> getSiblings()
    {
        throw new UnsupportedOperationException("This is simple node!");
    }

    @Override
    public List<Node<T>> getChildren()
    {
        throw new UnsupportedOperationException("This is simple node!");
    }

    @Override
    public void accept(final NodeAppenderVisitor<T> visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void visit(final SequenceNode<T> node)
    {
        node.nodes.add(this);
    }

    @Override
    public void visit(final GroupNode<T> node)
    {
        node.nodes.add(this);
    }

    @Override
    public void visit(final SimpleNode<T> tSimpleNode)
    {

    }
}
