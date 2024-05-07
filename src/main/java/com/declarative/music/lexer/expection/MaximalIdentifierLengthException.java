package com.declarative.music.lexer.expection;

import com.declarative.music.lexer.token.Position;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MaximalIdentifierLengthException extends RuntimeException {
    private final Position errorPosition;

}
