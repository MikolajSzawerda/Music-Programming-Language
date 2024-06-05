package com.declarative.music.interpreter.tree;

import java.util.ArrayList;
import java.util.List;


public class SequenceNode<T> extends AbstractNode<T>
{

    public SequenceNode(final List<Node<T>> nodes)
    {
        super(nodes);
    }

    public SequenceNode()
    {
        super(new ArrayList<>());
    }

    @Override
    public List<Node<T>> getSiblings()
    {
        return List.of(this);
    }

    @Override
    public List<Node<T>> getChildren()
    {
        return nodes;
    }

    @Override
    public void accept(final NodeAppenderVisitor<T> visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void visit(final SequenceNode<T> node)
    {
        node.nodes.addAll(getChildren());
    }

    @Override
    public void visit(final GroupNode<T> node)
    {
        node.nodes.add(this);
    }

    @Override
    public void visit(final SimpleNode<T> tSimpleNode)
    {
        nodes.add(tSimpleNode);
    }
}
