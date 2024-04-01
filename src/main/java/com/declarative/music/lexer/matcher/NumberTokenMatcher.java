package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

//TODO handle double
//TODO add max length
public class NumberTokenMatcher extends AbstractTokenMatcher {
    public NumberTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    private static boolean checkLast(char currentChar) {
        return !Character.isDigit(currentChar);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        if (!checkFirst()) {
            return null;
        }

        reader.mark(1024);
        int currentChar = reader.read();
        int readLen = 1;
        int number = Character.digit((char) currentChar, 10);
        while ((currentChar = reader.read()) != -1) {
            readLen++;
            var readChar = (char) currentChar;
            if (checkLast(readChar)) {
                reader.reset();
                reader.skip(readLen);
                break;
            }
            number = number * 10 + Character.digit(readChar, 10);
        }
        return new Token(TokenType.T_NUMBER, new Position(0, 0), number);
    }

    private boolean checkFirst() throws IOException {
        reader.mark(1);
        int currentChar = reader.read();
        reader.reset();
        return currentChar != -1 && Character.isDigit((char) currentChar);
    }
}
