package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

class NoteOrIdentifierStateTest {
    @ParameterizedTest
    @ValueSource(strings = {"A ", "A#", "A# "})
    void shouldParsePitch(final String code) throws IOException {
        // given
        final var lexer = new LexerContextMock(code);
        lexer.stateTransition(new NoteOrIdentifierState(lexer));
        final var expectedToken = new Token(TokenType.T_PITCH, new Position(0, 0), code.strip());

        // when
        Token token = null;
        while (token == null) {
            token = lexer.getState().processNext();
        }

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"dl", "h ", "w_d"})
    void shouldParseRhythm(final String code) throws IOException {
        // given
        final var lexer = new LexerContextMock(code);
        lexer.stateTransition(new NoteOrIdentifierState(lexer));
        final var expectedToken = new Token(TokenType.T_RHYTHM, new Position(0, 0), code.strip());

        // when
        Token token = null;
        while (token == null) {
            token = lexer.getState().processNext();
        }

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"dlb", "h_", "dl_t1", "abcd1", "A#1 "})
    void shouldParseIdentifier(final String code) throws IOException {
        // given
        final var lexer = new LexerContextMock(code);
        lexer.stateTransition(new NoteOrIdentifierState(lexer));
        final var expectedToken = new Token(TokenType.T_IDENTIFIER, new Position(0, 0), code.strip());

        // when
        Token token = null;
        while (token == null) {
            token = lexer.getState().processNext();
        }

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}