package com.declarative.music.interpreter.tree.modifier;

public interface ModifableNode<T extends ModifableNode<T, R>, R> {
    T accept(ModifierVisitor<R> visitor);
}
