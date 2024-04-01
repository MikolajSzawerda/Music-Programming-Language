package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;

//TODO handle windows line
public class CommentTokenMatcher extends AbstractTokenMatcher {
    public CommentTokenMatcher(BufferedReader reader) {
        super(reader);
    }

    @Nullable
    @Override
    public Token matchNextToken() throws IOException {
        if (!checkFirst()) {
            return null;
        }
        reader.skip(2);
        reader.mark(1024);
        int currentChar;
        int readLen = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while ((currentChar = reader.read()) != -1) {
            readLen++;
            var readChar = (char) currentChar;
            if (checkLast(readChar)) {
                reader.reset();
                reader.skip(readLen + 1);
                break;
            }
            stringBuilder.append(readChar);
        }
        return new Token(TokenType.T_COMMENT, new Position(0, 0), stringBuilder.toString());
    }

    private boolean checkFirst() throws IOException {
        reader.mark(2);
        int currentChar = reader.read();
        if (currentChar == -1 || (char) currentChar != '/') {
            reader.reset();
            return false;
        }
        currentChar = reader.read();
        reader.reset();
        return currentChar != -1 && (char) currentChar == '/';
    }

    private boolean checkLast(char currentChar) {
        return currentChar == '\n' || currentChar == '\r';
    }
}
