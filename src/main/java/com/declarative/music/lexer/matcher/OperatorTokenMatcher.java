package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

public class OperatorTokenMatcher extends AbstractTokenMatcher {
    public OperatorTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        reader.mark(2);
        var text = (char) reader.read();
        if (text == '=') {
            return new Token(TokenType.T_OPERATOR, new Position(0, 0), text);
        }
        reader.reset();
        return null;
    }
}
