package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

public class NumberTokenMatcher extends AbstractTokenMatcher {
    public NumberTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        reader.mark(1024);
        int currentChar;
        int readLen = 0;
        currentChar = reader.read();
        if (currentChar == -1 || !Character.isDigit((char) currentChar)) {
            reader.reset();
            return null;
        }
        readLen++;
        int number = Character.digit((char) currentChar, 10);
        while ((currentChar = reader.read()) != -1) {
            readLen++;
            var readChar = (char) currentChar;
            if (!Character.isDigit(readChar)) {
                reader.reset();
                reader.skip(readLen);
                break;
            }
            number = number * 10 + Character.digit(readChar, 10);
        }
        return new Token(TokenType.T_NUMBER, new Position(0, 0), number);
    }
}
