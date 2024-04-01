package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

public class StringsTokenMatcher extends AbstractTokenMatcher {
    public StringsTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        reader.mark(1024);
        int currentChar;
        int readLen = 0;
        currentChar = reader.read();
        if (currentChar == -1 || (char) currentChar != '"') {
            reader.reset();
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        while ((currentChar = reader.read()) != -1) {
            readLen++;
            var readChar = (char) currentChar;
            if (readChar == '"') {
                reader.reset();
                reader.skip(readLen + 1);
                break;
            }
            stringBuilder.append(readChar);
        }
        return new Token(TokenType.T_STRING, new Position(0, 0), stringBuilder.toString());
    }
}
