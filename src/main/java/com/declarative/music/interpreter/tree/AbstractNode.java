package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.ModifierVisitor;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public abstract class AbstractNode<T> implements Node<T> {
    public final List<Node<T>> nodes;
    private ModifierVisitor<T> modifier;

    @Override
    public ModifierVisitor<T> modifier() {
        return modifier;
    }

    @Override
    public void setModifier(ModifierVisitor<T> visitor) {
        this.modifier = visitor;
        for (var node : nodes) {
            node.setModifier(modifier);
        }
    }
}
