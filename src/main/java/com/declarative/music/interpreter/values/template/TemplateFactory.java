package com.declarative.music.interpreter.values.template;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.NodeFactory;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;


public class TemplateFactory implements NodeFactory<Integer>
{
    @Override
    public GroupNode<Integer> createGroup()
    {
        return new GroupNode<>();
    }

    @Override
    public SequenceNode<Integer> createSequence()
    {
        return new SequenceNode<>();
    }

    @Override
    public SimpleNode<Integer> createSimpleNode(final Integer value)
    {
        return new IndexNode(value);
    }
}
