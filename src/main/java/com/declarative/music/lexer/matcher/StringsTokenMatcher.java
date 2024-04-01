package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

//TODO add max length
public class StringsTokenMatcher extends AbstractTokenMatcher {
    public StringsTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        if (!checkFirst()) {
            return null;
        }
        reader.skip(1);
        reader.mark(1024);
        int readLen = 0;
        int currentChar;
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

    private boolean checkFirst() throws IOException {
        reader.mark(1);
        int currentChar = reader.read();
        reader.reset();
        return currentChar != -1 && (char) currentChar == '"';
    }
}
