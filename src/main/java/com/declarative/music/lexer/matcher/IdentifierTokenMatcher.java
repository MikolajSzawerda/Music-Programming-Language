package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

//TODO handle language ids
//TODO handle music ids
public class IdentifierTokenMatcher extends AbstractTokenMatcher {
    public IdentifierTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    private static boolean checkLast(char currentChar) {
        return !Character.isDigit(currentChar) && !Character.isLetter(currentChar);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        if (!checkFirst()) {
            return null;
        }
        reader.mark(1024);
        int readLen = 0;
        int currentChar = reader.read();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((char) currentChar);
        while ((currentChar = reader.read()) != -1) {
            readLen++;
            var readChar = (char) currentChar;
            if (checkLast(readChar)) {
                reader.reset();
                reader.skip(readLen);
                break;
            }
            stringBuilder.append(readChar);
        }
        return new Token(TokenType.T_IDENTIFIER, new Position(0, 0), stringBuilder.toString());
    }

    private boolean checkFirst() throws IOException {
        reader.mark(1);
        int currentChar = reader.read();
        reader.reset();
        return currentChar != -1 && Character.isLetter((char) currentChar);
    }
}
