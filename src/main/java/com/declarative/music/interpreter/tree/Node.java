package com.declarative.music.interpreter.tree;

import java.util.List;


public interface Node<T> {
    List<Node<T>> getSiblings();

    List<Node<T>> getChildren();
}
