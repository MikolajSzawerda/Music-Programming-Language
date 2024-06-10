package com.declarative.music.interpreter.tree.modifier;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;

public interface ModifierVisitor<T> {
    SimpleNode<T> visit(SimpleNode<T> node);

    GroupNode<T> visit(GroupNode<T> node);

    SequenceNode<T> visit(SequenceNode<T> node);

}
