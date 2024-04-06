package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

class PitchStateTest {
    @ParameterizedTest
    @ValueSource(strings = {"A", "A#", "B# "})
    void shouldParsePitch(final String code) throws IOException {
        // given
        final var tested = new PitchState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_PITCH, new Position(0, 0), code.strip());

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParsePitchWithComma() throws IOException {
        // given
        final var code = "C,4";
        final var tested = new PitchState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_PITCH, new Position(0, 0), "C");

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"AA", "A#b"})
    void shouldReturnNullAndChangeState(final String code) throws IOException {
        // given
        final var tested = new PitchState(new LexerContextMock(code));

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertNull(token);
    }
}