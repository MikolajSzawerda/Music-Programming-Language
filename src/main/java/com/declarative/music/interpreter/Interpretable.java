package com.declarative.music.interpreter;

import com.declarative.music.lexer.token.Position;

public interface Interpretable {
    void accept(Visitor visitor);

    Position position();
}
