package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class OperatorStateTest {

    @Test
    void shouldParseSingleOperator() throws IOException {
        // given
        final var code = "+";
        final var tested = new OperatorOrUnknownState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_OPERATOR, new Position(0, 0), code);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParseDoubleOperator() throws IOException {
        // given
        final var code = "+=";
        final var tested = new OperatorOrUnknownState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_OPERATOR, new Position(0, 0), code);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParsePunctuation() throws IOException {
        // given
        final var code = "{";
        final var tested = new OperatorOrUnknownState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_L_CURL_PARENTHESIS, new Position(0, 0), null);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}