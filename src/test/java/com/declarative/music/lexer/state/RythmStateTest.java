package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

class RythmStateTest {
    @ParameterizedTest
    @ValueSource(strings = {"dl", "w", "h ", "w_d", "dl_t"})
    void shouldParseRhythm(final String code) throws IOException {
        // given
        final var tested = new RythmState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_RHYTHM, new Position(0, 0), code.strip());

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"db", "w_", "w_c", "d_l", "dl1", "dl_t1"})
    void shouldReturnNull(final String code) throws IOException {
        // given
        final var tested = new RythmState(new LexerContextMock(code));

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertNull(token);
    }
}