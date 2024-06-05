package com.declarative.music.interpreter.values.template;

import com.declarative.music.interpreter.tree.SimpleNode;


public class IndexNode extends SimpleNode<Integer>
{
    public IndexNode(final Integer value)
    {
        super(value);
    }

    @Override
    public Integer getValue()
    {
        return null;
    }
}
