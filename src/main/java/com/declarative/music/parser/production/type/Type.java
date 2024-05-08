package com.declarative.music.parser.production.type;

import com.declarative.music.interpreter.Interpretable;
import com.declarative.music.lexer.token.Position;

public interface Type extends Interpretable {
    Position position();
}
