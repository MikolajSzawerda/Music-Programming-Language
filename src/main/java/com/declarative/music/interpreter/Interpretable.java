package com.declarative.music.interpreter;

public interface Interpretable {
    void accept(Visitor visitor);
}
