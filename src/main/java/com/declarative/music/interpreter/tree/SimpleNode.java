package com.declarative.music.interpreter.tree;

import java.util.List;

import com.declarative.music.interpreter.values.music.NoteModifier;

import lombok.Getter;


@Getter
public abstract class SimpleNode<T> implements Node<T>
{
    protected final T value;
    //TODO abstract
    public NoteModifier modifier;

    protected SimpleNode(T value)
    {
        this.value = value;
    }

    @Override
    public List<Node<T>> getSiblings()
    {
        throw new UnsupportedOperationException("This is simple node!");
    }

    public abstract T getValue();

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
