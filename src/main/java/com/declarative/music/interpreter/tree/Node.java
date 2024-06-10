package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.ModifableNode;
import com.declarative.music.interpreter.tree.modifier.ModifierVisitor;

import java.util.List;


public interface Node<T> extends ModifableNode<Node<T>, T> {
    List<Node<T>> getChildren();

    ModifierVisitor<T> modifier();

    void setModifier(ModifierVisitor<T> visitor);

    Node<T> getModified();

}
