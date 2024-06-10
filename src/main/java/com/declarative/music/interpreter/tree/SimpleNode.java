package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.ModifierVisitor;
import lombok.Getter;

import java.util.List;


@Getter
public class SimpleNode<T> implements Node<T> {
    @Getter
    protected final T value;
    //TODO abstract
    private ModifierVisitor<T> modifier;

    public SimpleNode(T value) {
        this.value = value;
    }

    @Override
    public List<Node<T>> getChildren() {
        throw new UnsupportedOperationException("This is simple node!");
    }

    @Override
    public ModifierVisitor<T> modifier() {
        return modifier;
    }

    @Override
    public void setModifier(ModifierVisitor<T> visitor) {
        this.modifier = visitor;
    }

    @Override
    public SimpleNode<T> getModified() {
        if (modifier == null) {
            return this;
        }
        return this.accept(modifier);
    }

    @Override
    public SimpleNode<T> accept(ModifierVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
