package com.declarative.music.interpreter.tree;

import java.util.List;

import com.declarative.music.interpreter.values.music.NoteModifier;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AbstractNode<T> implements Node<T>
{
    public final List<Node<T>> nodes;
    public NoteModifier modifier;
}
