package com.declarative.music.lexer;

import com.declarative.music.lexer.token.Position;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

@RequiredArgsConstructor
public class LexerStreamReader implements Closeable {
    private final Reader reader;
    int prevChar = -2;
    boolean skipFeed = false;
    private int currentLine = 0;
    private int currentLineChar = -1;

    private void increaseLine() {
        ++currentLine;
        currentLineChar = -1;
    }

    public Position getCurrentPosition() {
        return new Position(currentLine, currentLineChar);
    }

    public int read() throws IOException {
        if (prevChar == -1) {
            return -1;
        }
        ++currentLineChar;
        if (skipFeed) {
            skipFeed = false;
            return prevChar;
        }
        int currentChar = reader.read();
        if (currentChar == (int) '\r') {
            increaseLine();
            currentChar = reader.read();
            if (currentChar != (int) '\n') {
                prevChar = currentChar;
                skipFeed = true;
                return '\n';
            }
            prevChar = currentChar;
            return currentChar;
        }
        if (currentChar == (int) '\n') {
            increaseLine();
        }
        prevChar = currentChar;
        return currentChar;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
